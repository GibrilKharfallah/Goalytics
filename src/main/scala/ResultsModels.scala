/**
 * Output models for results.json.
 *
 * This file represents the contract for exports and should be stable once validated
 * against SPECIFICATIONS.md. Add new fields only if you also update ResultsBuilder,
 * JsonCodecs and ReportWriter accordingly.
 */
case class ParsingStats(
  total_players_parsed: Int,
  total_players_valid: Int,
  parsing_errors: Int,
  duplicates_removed: Int
)

case class ScorerEntry(
  name: String,
  club: String,
  goals: Int,
  matches: Int
)

case class AssisterEntry(
  name: String,
  club: String,
  assists: Int,
  matches: Int
)

case class ValuableEntry(
  name: String,
  club: String,
  marketValue: Double
)

case class SalaryEntry(
  name: String,
  club: String,
  salary: Double
)

case class DisciplineStats(
  total_yellow_cards: Int,
  total_red_cards: Int,
  most_disciplined_position: String,
  least_disciplined_position: String
)

/**
 * Bonus statistics: contribution (goals + assists) per match.
 */
case class ContributionEntry(
  name: String,
  club: String,
  goals: Int,
  assists: Int,
  matches: Int,
  contribution_per_match: Double
)

/**
 * Bonus statistics: discipline risk per match.
 * Reds are typically more impactful than yellows, hence a higher weight.
 */
case class DisciplineRiskEntry(
  name: String,
  club: String,
  yellow: Int,
  red: Int,
  matches: Int,
  risk_per_match: Double
)

/**
 * Bonus statistics: "value for money" based on goal contribution per salary.
 */
case class ValueForMoneyEntry(
  name: String,
  club: String,
  goals: Int,
  assists: Int,
  salary: Double,
  contrib_per_million: Double
)

/**
 * Final JSON output structure for one run.
 */
case class Results(
  statistics: ParsingStats,
  top_10_scorers: List[ScorerEntry],
  top_10_assisters: List[AssisterEntry],
  most_valuable_players: List[ValuableEntry],
  highest_paid_players: List[SalaryEntry],
  players_by_league: Map[String, Int],
  players_by_position: Map[String, Int],
  average_age_by_position: Map[String, Double],
  average_goals_by_position: Map[String, Double],
  discipline_statistics: DisciplineStats,
  top_goal_contribution_per_match: List[ContributionEntry],
  top_discipline_risk_per_match: List[DisciplineRiskEntry],
  best_value_for_money: List[ValueForMoneyEntry]
)
