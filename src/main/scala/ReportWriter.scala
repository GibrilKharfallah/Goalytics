import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.util.Try

/**
 * Produces a human-readable text report for a single pipeline run.
 *
 * The report includes:
 *   - parsing/cleaning counters (parsed, parsing errors, invalid rows, duplicates, valid)
 *   - mandatory dataset statistics (top scorers, assisters, etc.)
 *   - optional/bonus statistics (if present in Results)
 *
 * This file is intentionally independent from JSON output to allow different formatting
 * without affecting results.json.
 */
object ReportWriter {

  private def formatTopScorers(xs: List[ScorerEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      s"${i + 1}. ${x.name} : ${x.goals} buts en ${x.matches} matchs"
    }.mkString("\n")

  private def formatTopAssisters(xs: List[AssisterEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      s"${i + 1}. ${x.name} : ${x.assists} passes en ${x.matches} matchs"
    }.mkString("\n")

  private def formatValuable(xs: List[ValuableEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      f"${i + 1}. ${x.name} : ${x.marketValue}%.1f M"
    }.mkString("\n")

  private def formatSalary(xs: List[SalaryEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      f"${i + 1}. ${x.name} : ${x.salary}%.1f M/an"
    }.mkString("\n")

  private def formatCountMap(title: String, m: Map[String, Int]): String = {
    val body = m.toList.map { case (k, c) => s"- $k : $c joueurs" }.mkString("\n")
    title + "\n" + body
  }

  private def formatAvgMap(title: String, m: Map[String, Double], unit: String): String = {
    val body = m.toList.map { case (k, v) => f"- $k : $v%.2f $unit" }.mkString("\n")
    title + "\n" + body
  }

  private def formatContribution(xs: List[ContributionEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      f"${i + 1}. ${x.name} : ${x.goals}%d + ${x.assists}%d en ${x.matches}%d (=${x.contribution_per_match}%.3f / match)"
    }.mkString("\n")

  private def formatRisk(xs: List[DisciplineRiskEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      f"${i + 1}. ${x.name} : ${x.yellow}%d jaunes, ${x.red}%d rouges en ${x.matches}%d (risk=${x.risk_per_match}%.3f / match)"
    }.mkString("\n")

  private def formatValue(xs: List[ValueForMoneyEntry]): String =
    xs.zipWithIndex.map { case (x, i) =>
      f"${i + 1}. ${x.name} : (G+A)=${x.goals + x.assists}%d, salaire=${x.salary}%.2f M -> ${x.contrib_per_million}%.3f contrib/M"
    }.mkString("\n")

  /**
   * Renders a full report as a String.
   *
   * @param results computed statistics for the dataset
   * @param meta cleaning metadata (counts of invalid rows, duplicates, etc.)
   * @param durationSeconds processing time in seconds for this run
   * @param entriesPerSecond throughput computed from parsed entries and duration
   */
  def toText(
    results: Results,
    meta: MetaInfo,
    durationSeconds: Double,
    entriesPerSecond: Double
  ): String = {
    val d = results.discipline_statistics

    "===============================================\n"
      + "   RAPPORT D'ANALYSE - JOUEURS DE FOOTBALL\n"
      + "===============================================\n\n"
      + "STATISTIQUES DE PARSING\n"
      + "---------------------------\n"
      + s"- Total entrees (JSON)      : ${meta.totalParsed}\n"
      + s"- Erreurs de parsing        : ${meta.parsingErrors}\n"
      + s"- Objets invalides          : ${meta.invalidCount}\n"
      + s"- Doublons supprimes        : ${meta.duplicatesRemoved}\n"
      + s"- Total valides             : ${meta.totalValid}\n\n"
      + "TOP 10 - BUTEURS\n"
      + "-------------------\n"
      + formatTopScorers(results.top_10_scorers) + "\n\n"
      + "TOP 10 - PASSEURS\n"
      + "---------------------\n"
      + formatTopAssisters(results.top_10_assisters) + "\n\n"
      + "TOP 10 - VALEUR MARCHANDE\n"
      + "-----------------------------\n"
      + formatValuable(results.most_valuable_players) + "\n\n"
      + "TOP 10 - SALAIRES\n"
      + "--------------------\n"
      + formatSalary(results.highest_paid_players) + "\n\n"
      + "REPARTITION PAR LIGUE\n"
      + "-------------------------\n"
      + formatCountMap("", results.players_by_league) + "\n\n"
      + "REPARTITION PAR POSTE\n"
      + "------------------------\n"
      + formatCountMap("", results.players_by_position) + "\n\n"
      + "MOYENNES PAR POSTE\n"
      + "----------------------\n"
      + formatAvgMap("AGE MOYEN :", results.average_age_by_position, "ans") + "\n\n"
      + formatAvgMap("BUTS PAR MATCH (moyenne) :", results.average_goals_by_position, "buts") + "\n\n"
      + "DISCIPLINE\n"
      + "--------------\n"
      + s"- Total cartons jaunes      : ${d.total_yellow_cards}\n"
      + s"- Total cartons rouges      : ${d.total_red_cards}\n"
      + s"- Poste le plus discipline  : ${d.most_disciplined_position}\n"
      + s"- Poste le moins discipline : ${d.least_disciplined_position}\n\n"
      + "BONUS STATS\n"
      + "--------------------------\n"
      + "TOP 10 - GOAL CONTRIBUTION / MATCH (buts + passes)\n"
      + formatContribution(results.top_goal_contribution_per_match) + "\n\n"
      + "TOP 10 - DISCIPLINE RISK / MATCH (jaunes + 3*rouges)\n"
      + formatRisk(results.top_discipline_risk_per_match) + "\n\n"
      + "TOP 10 - VALUE FOR MONEY ((buts+passes)/salaire)\n"
      + formatValue(results.best_value_for_money) + "\n\n"
      + "PERFORMANCE\n"
      + "---------------\n"
      + f"- Temps de traitement       : $durationSeconds%.3f secondes\n"
      + f"- Entrees/seconde           : $entriesPerSecond%.2f\n\n"
      + "===============================================\n"
  }

  /**
   * Writes the report to disk, creating the parent folder if it does not exist.
   *
   * @param results computed statistics
   * @param meta cleaning metadata
   * @param path output file path (e.g. output/clean/report.txt)
   * @return Right(()) on success, Left(errorMessage) on failure
   */
  def writeReport(
    results: Results,
    meta: MetaInfo,
    path: String,
    durationSeconds: Double,
    entriesPerSecond: Double
  ): Either[String, Unit] = {
    val txt = toText(results, meta, durationSeconds, entriesPerSecond)

    Try {
      val p = Paths.get(path)
      if (p.getParent != null) Files.createDirectories(p.getParent)
      Files.write(p, txt.getBytes(StandardCharsets.UTF_8))
    }.toEither.left.map(_.getMessage).map(_ => ())
  }
}
