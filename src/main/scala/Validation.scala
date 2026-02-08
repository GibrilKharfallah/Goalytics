/**
 * Validates and cleans raw decoded data.
 *
 * This module is responsible for the "Transform (cleaning)" part of the pipeline:
 *   - convert PlayerRaw into Player (typed position + required fields)
 *   - reject invalid entries using Either to keep reasons explicit
 *   - compute invalid counts
 *   - remove duplicates
 *
 * Scalability:
 *   - Business rules are centralized in validate().
 *   - The deduplication strategy is a single function (dedupeById) that can be swapped
 *     without impacting the rest of the pipeline.
 */
object Validation {

    /**
    * Allowed positions for the dataset.
    * Using a map keeps the conversion to the Position ADT explicit and testable.
    */
    private val validPositions: Map[String, Position] = Map(
        "Goalkeeper" -> Goalkeeper,
        "Defender"   -> Defender,
        "Midfielder" -> Midfielder,
        "Forward"    -> Forward
    )

    /**
    * Validates a raw decoded player and converts it to the validated Player model.
    *
    * Validation rules:
    *   - position must belong to the allowed set
    *   - age must be in the expected range
    *   - goals must be non-negative
    *   - matchesPlayed must be strictly positive (protects against division by zero later)
    *   - club must be present (required for reporting)
    *
    * @param raw decoded input row
    * @return Right(Player) if valid, Left(reason) otherwise
    */
    def validate(raw: PlayerRaw): Either[String, Player] = {
        for {
        pos  <- validPositions.get(raw.position).toRight("invalid position")
        _    <- Either.cond(raw.age >= 16 && raw.age <= 45, (), "invalid age")
        _    <- Either.cond(raw.goalsScored >= 0, (), "invalid goals")
        _    <- Either.cond(raw.matchesPlayed > 0, (), "invalid matches")
        club <- raw.club.toRight("missing club")
        } yield Player(
        id = raw.id,
        name = raw.name,
        age = raw.age,
        nationality = raw.nationality,
        position = pos,
        club = club,
        league = raw.league,
        goalsScored = raw.goalsScored,
        assists = raw.assists,
        matchesPlayed = raw.matchesPlayed,
        yellowCards = raw.yellowCards,
        redCards = raw.redCards,
        marketValue = raw.marketValue,
        salary = raw.salary
        )
    }

    /**
    * Validates a list of raw players and returns:
    *   - the list of validated players
    *   - the count of invalid entries (Left values)
    *
    * @param raws decoded input rows
    */
    def partitionValidated(raws: List[PlayerRaw]): (List[Player], Int) = {
        val validated: List[Either[String, Player]] = raws.map(validate)
        val valids = validated.collect { case Right(p) => p }
        val invalidCount = validated.count(_.isLeft)
        (valids, invalidCount)
    }

    /**
    * Removes duplicates based on player id.
    *
    * This function is deterministic and uses an immutable Set to keep the implementation
    * purely functional. If the dataset changes and id is no longer a stable key, replace
    * this function with another keying strategy without changing other modules.
    *
    * @param players validated players
    * @return (deduplicatedPlayers, duplicatesRemoved)
    */
    def dedupeById(players: List[Player]): (List[Player], Int) = {
        val (keptRev, _, removed) =
        players.foldLeft((List.empty[Player], Set.empty[Int], 0)) {
            case ((acc, seen, dupCount), p) =>
            if (seen.contains(p.id)) (acc, seen, dupCount + 1)
            else (p :: acc, seen + p.id, dupCount)
        }

        (keptRev.reverse, removed)
    }
}
