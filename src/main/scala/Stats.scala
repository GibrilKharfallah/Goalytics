/**
 * Computes all dataset statistics from a list of validated players.
 *
 * This module is the ETL "Transform" step: it aggregates, ranks and summarizes data
 * using higher-order functions (map, filter, groupBy, foldLeft, sortBy).
 *
 * Assumptions:
 *   - Input players are already validated (see Validation.validate), so invariants such as
 *     matchesPlayed > 0 hold for most computations. Where division is used, this module
 *     still guards against division by zero.
 *
 * Scalability:
 *   - To add a new statistic, prefer adding a new pure function here and wiring it in ResultsBuilder.
 *   - Keep functions total (no exceptions) and deterministic to simplify testing.
 */
object Stats {

    /**
    * Extracts a stable club label from a validated player.
    * Isolated in one place to ease future schema changes (e.g. club becomes optional again).
    */
    private def clubName(p: Player): String = p.club

    /**
    * Top 10 scorers, sorted by goals descending then matches ascending (tie-break).
    * Complexity: O(n log n) due to sorting.
    */
    def topScorers(players: List[Player]): List[ScorerEntry] =
        players
        .sortBy(p => (-p.goalsScored, p.matchesPlayed))
        .take(10)
        .map(p => ScorerEntry(p.name, clubName(p), p.goalsScored, p.matchesPlayed))

    /**
    * Top 10 assisters, sorted by assists descending then matches ascending (tie-break).
    * Complexity: O(n log n).
    */
    def topAssisters(players: List[Player]): List[AssisterEntry] =
        players
        .sortBy(p => (-p.assists, p.matchesPlayed))
        .take(10)
        .map(p => AssisterEntry(p.name, clubName(p), p.assists, p.matchesPlayed))

    /**
    * Top 10 market values among players where marketValue is defined.
    * Complexity: O(n log n).
    */
    def mostValuable(players: List[Player]): List[ValuableEntry] = {
        val withValues =
        players
            .map(p => p.marketValue.map(v => (p, v)))
            .collect { case Some(pair) => pair }

        withValues
        .sortBy { case (_, v) => -v }
        .take(10)
        .map { case (p, v) => ValuableEntry(p.name, clubName(p), v.toDouble) }
    }

    /**
    * Top 10 salaries among players where salary is defined.
    * Complexity: O(n log n).
    */
    def highestPaid(players: List[Player]): List[SalaryEntry] = {
        val withSalaries =
        players
            .map(p => p.salary.map(s => (p, s)))
            .collect { case Some(pair) => pair }

        withSalaries
        .sortBy { case (_, s) => -s }
        .take(10)
        .map { case (p, s) => SalaryEntry(p.name, clubName(p), s) }
    }

    /**
    * Counts players per league.
    * Complexity: O(n).
    */
    def playersByLeague(players: List[Player]): Map[String, Int] =
        players.groupBy(_.league).map { case (league, ps) => league -> ps.size }

    /**
    * Counts players per position (Position ADT serialized as String).
    * Complexity: O(n).
    */
    def playersByPosition(players: List[Player]): Map[String, Int] =
        players.groupBy(_.position.toString).map { case (pos, ps) => pos -> ps.size }

    /**
    * Average age per position.
    * Complexity: O(n).
    */
    def averageAgeByPosition(players: List[Player]): Map[String, Double] =
        players.groupBy(_.position.toString).map { case (pos, ps) =>
        val totalAge = ps.foldLeft(0)(_ + _.age)
        pos -> (totalAge.toDouble / ps.size)
        }

    /**
    * Average goals per match per position.
    * Computation uses totals to avoid averaging individual ratios.
    * Complexity: O(n).
    */
    def averageGoalsByPosition(players: List[Player]): Map[String, Double] =
        players.groupBy(_.position.toString).map { case (pos, ps) =>
        val totalGoals = ps.foldLeft(0)(_ + _.goalsScored)
        val totalMatches = ps.foldLeft(0)(_ + _.matchesPlayed)
        val avg = if (totalMatches == 0) 0.0 else totalGoals.toDouble / totalMatches
        pos -> avg
        }

    /**
    * Discipline summary:
    *   - total yellow and red cards
    *   - most/least disciplined position based on total cards (yellow + red)
    *
    * Complexity: O(n).
    */
    def discipline(players: List[Player]): DisciplineStats = {
        val totalsByPosition =
        players.groupBy(_.position.toString).map { case (pos, ps) =>
            val yellows = ps.foldLeft(0)(_ + _.yellowCards)
            val reds    = ps.foldLeft(0)(_ + _.redCards)
            pos -> (yellows + reds)
        }

        val mostDisciplined = totalsByPosition.minBy(_._2)._1
        val leastDisciplined = totalsByPosition.maxBy(_._2)._1

        val totalYellow = players.foldLeft(0)(_ + _.yellowCards)
        val totalRed    = players.foldLeft(0)(_ + _.redCards)

        DisciplineStats(
        total_yellow_cards = totalYellow,
        total_red_cards = totalRed,
        most_disciplined_position = mostDisciplined,
        least_disciplined_position = leastDisciplined
        )
    }

    /**
    * Top players by goal contribution per match.
    * Contribution is defined as (goalsScored + assists) / matchesPlayed.
    *
    * This statistic is designed to be comparable across players with different play time.
    * Complexity: O(n log n).
    */
    def topGoalContributionPerMatch(players: List[Player], n: Int = 10): List[ContributionEntry] =
        players
        .filter(_.matchesPlayed > 0)
        .map { p =>
            val perMatch = (p.goalsScored + p.assists).toDouble / p.matchesPlayed
            ContributionEntry(
            name = p.name,
            club = clubName(p),
            goals = p.goalsScored,
            assists = p.assists,
            matches = p.matchesPlayed,
            contribution_per_match = perMatch
            )
        }
        .sortBy(e => -e.contribution_per_match)
        .take(n)

    /**
    * Top players by discipline risk per match.
    * Risk is defined as (yellowCards + 3 * redCards) / matchesPlayed.
    *
    * The weight on red cards is a deliberate heuristic documented in the report.
    * Complexity: O(n log n).
    */
    def topDisciplineRiskPerMatch(players: List[Player], n: Int = 10): List[DisciplineRiskEntry] =
        players
        .filter(_.matchesPlayed > 0)
        .map { p =>
            val risk = (p.yellowCards + 3 * p.redCards).toDouble / p.matchesPlayed
            DisciplineRiskEntry(
            name = p.name,
            club = clubName(p),
            yellow = p.yellowCards,
            red = p.redCards,
            matches = p.matchesPlayed,
            risk_per_match = risk
            )
        }
        .sortBy(e => -e.risk_per_match)
        .take(n)

    /**
    * Top "value for money" players, measured as goal contribution per salary unit.
    * Score is defined as (goalsScored + assists) / salary.
    *
    * Only players with salary defined are included.
    * Complexity: O(n log n).
    */
    def bestValueForMoney(players: List[Player], n: Int = 10): List[ValueForMoneyEntry] =
        players
        .flatMap { p =>
            p.salary.map { s =>
            val score = (p.goalsScored + p.assists).toDouble / s
            ValueForMoneyEntry(
                name = p.name,
                club = clubName(p),
                goals = p.goalsScored,
                assists = p.assists,
                salary = s,
                contrib_per_million = score
            )
            }
        }
        .sortBy(e => -e.contrib_per_million)
        .take(n)
}
