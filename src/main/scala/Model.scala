/**
 * Domain model for the Football Players dataset.
 *
 * PlayerRaw matches the JSON input schema as closely as possible. Optional fields are
 * represented with Option to support missing/null values in dirty datasets.
 *
 * Player represents validated data used by the transformation layer (Stats).
 * It uses a typed Position instead of a free-form String.
 */

sealed trait Position
case object Goalkeeper extends Position
case object Defender extends Position
case object Midfielder extends Position
case object Forward extends Position

/**
 * Raw input row as decoded from JSON.
 *
 * This type aims to be stable across clean/dirty/large files. It should not include
 * dataset-specific "fixes"; instead, correctness is ensured during validation.
 */
final case class PlayerRaw(
    id: Int,
    name: String,
    age: Int,
    nationality: String,
    position: String,
    club: Option[String],
    league: String,
    goalsScored: Int,
    assists: Int,
    matchesPlayed: Int,
    yellowCards: Int,
    redCards: Int,
    marketValue: Option[Int],
    salary: Option[Double]
)

/**
 * Validated player used for statistics.
 *
 * Invariants after validation:
 *   - 16 <= age <= 45
 *   - goalsScored >= 0
 *   - matchesPlayed > 0
 *   - position is one of the allowed values
 *   - club is defined and non-null (enforced by validation step)
 */
final case class Player(
    id: Int,
    name: String,
    age: Int,
    nationality: String,
    position: Position,
    club: String,
    league: String,
    goalsScored: Int,
    assists: Int,
    matchesPlayed: Int,
    yellowCards: Int,
    redCards: Int,
    marketValue: Option[Int],
    salary: Option[Double]
)

/**
 * Processing metadata for one pipeline run.
 *
 * Counts are intended to support reporting and debugging on dirty datasets.
 */
final case class MetaInfo(
    totalParsed: Int,
    totalValid: Int,
    parsingErrors: Int,
    invalidCount: Int,
    duplicatesRemoved: Int
)

/**
 * Output of the cleaning stage: validated rows + metadata.
 */
final case class CleanData(
    players: List[Player],
    meta: MetaInfo
)
