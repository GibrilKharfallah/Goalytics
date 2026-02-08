/**
 * Application entry point.
 *
 * Runs the ETL pipeline on one file (if a path is provided as argument) or on the
 * three default datasets (clean/dirty/large). Results are written under:
 *   output/<label>/results.json
 *   output/<label>/report.txt
 *
 * Timing is measured per file. Throughput is computed using the number of parsed
 * JSON items (including decoding failures) divided by runtime.
 */
object Main {

  /**
   * Extracts a stable label from a path to route outputs into a dedicated folder.
   * This keeps the output structure predictable when running multiple datasets.
   */
  private def labelFromPath(path: String): String = {
    val lower = path.toLowerCase
    if (lower.contains("clean")) "clean"
    else if (lower.contains("dirty")) "dirty"
    else if (lower.contains("large")) "large"
    else "custom"
  }

  /**
   * Runs the ETL pipeline:
   *   1) load + clean data
   *   2) compute statistics
   *   3) write JSON results
   *   4) write text report
   *
   * @param args optionally contains a single input path
   */
  def main(args: Array[String]): Unit = {
    val inputs: List[String] =
      if (args.nonEmpty) List(args(0))
      else List(
        "data/data_clean.json",
        "data/data_dirty.json",
        "data/data_large.json"
      )

    val startAll = System.nanoTime()

    inputs.foreach { path =>
      val label = labelFromPath(path)
      val outDir = s"output/$label"

      val start = System.nanoTime()

      val result = for {
        clean   <- DataLoader.loadAndClean(path)
        results  = ResultsBuilder.build(clean)
        _       <- ResultsWriter.writeResults(results, s"$outDir/results.json")
      } yield (clean, results)

      val end = System.nanoTime()
      val durationSeconds = (end - start).toDouble / 1e9

      result match {
        case Right((clean, results)) =>
          val eps =
            if (durationSeconds > 0) results.statistics.total_players_parsed / durationSeconds
            else 0.0

          ReportWriter.writeReport(
            results = results,
            meta = clean.meta,
            path = s"$outDir/report.txt",
            durationSeconds = durationSeconds,
            entriesPerSecond = eps
          ) match {
            case Right(_) =>
              println(
                s"[$label] OK -> $outDir (time=${"%.3f".format(durationSeconds)} s, eps=${"%.1f".format(eps)})"
              )
            case Left(err) =>
              println(s"[$label] ERROR writing report: $err")
          }

        case Left(err) =>
          println(s"[$label] ERROR: $err")
      }
    }

    val endAll = System.nanoTime()
    val totalSeconds = (endAll - startAll).toDouble / 1e9
    println(s"Done. Total time=${"%.3f".format(totalSeconds)} s")
  }
}
