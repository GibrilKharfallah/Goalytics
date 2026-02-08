import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.util.Try

/**
 * Writes results.json to disk.
 *
 * This module is the ETL "Load" step for JSON output. It creates parent directories
 * as needed and returns errors as Either rather than throwing exceptions.
 */
object ResultsWriter {

  /**
   * Serializes Results to JSON and writes it to the given path.
   *
   * @param results computed statistics
   * @param path output file path (e.g. output/clean/results.json)
   * @return Right(()) on success, Left(errorMessage) on failure
   */
  def writeResults(results: Results, path: String): Either[String, Unit] = {
    val json = JsonCodecs.resultsToJson(results)

    Try {
      val p = Paths.get(path)
      if (p.getParent != null) Files.createDirectories(p.getParent)
      Files.write(p, json.getBytes(StandardCharsets.UTF_8))
    }.toEither.left.map(_.getMessage).map(_ => ())
  }
}
