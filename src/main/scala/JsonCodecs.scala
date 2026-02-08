import io.circe.syntax._
import io.circe.generic.auto._

/**
 * JSON encoding helpers for output artifacts.
 *
 * Circe generic derivation is used for the Results model. Keeping this module small
 * makes it easier to change encoding strategy later (e.g. custom encoders, snake_case, etc.)
 * without impacting the rest of the pipeline.
 */
object JsonCodecs {

  /**
   * Serializes computed results into a pretty-printed JSON string.
   *
   * @param results fully computed pipeline output
   * @return JSON string formatted with 2-space indentation
   */
  def resultsToJson(results: Results): String =
    results.asJson.spaces2
}
