import io.circe.Json
import io.circe.syntax._

/**
 * Legacy output utilities.
 *
 * This object was an early draft for manually constructing the output JSON and report text.
 * The current pipeline uses ResultsModels + JsonCodecs + ResultsWriter/ReportWriter instead.
 *
 * Keeping this file is optional; if you keep it, clarify that it is not used by Main.
 */
object Output {

    /**
    * Builds the expected output JSON object by assembling pre-computed JSON fragments.
    * Prefer using Results + JsonCodecs for type-safe output generation.
    */
    def writeResultsJson(
        stats: Json,
        topScorers: Json,
        topAssisters: Json,
        topValues: Json,
        topSalaries: Json,
        byLeague: Json,
        byPosition: Json,
        avgAge: Json,
        avgGoals: Json,
        discipline: Json
    ): Json = {
        Json.obj(
        "statistics" -> stats,
        "top_10_scorers" -> topScorers,
        "top_10_assisters" -> topAssisters,
        "most_valuable_players" -> topValues,
        "highest_paid_players" -> topSalaries,
        "players_by_league" -> byLeague,
        "players_by_position" -> byPosition,
        "average_age_by_position" -> avgAge,
        "average_goals_by_position" -> avgGoals,
        "discipline_statistics" -> discipline
        )
    }

    /**
    * Placeholder for a formatted text report.
    * The actual implementation is provided by ReportWriter.
    */
    def buildReportText(/* pass the computed stats */): String =
        "TODO: format exactly like SPECIFICATIONS.md"
}
