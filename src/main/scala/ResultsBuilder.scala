/**
 * Builds the final Results object from cleaned data.
 *
 * This is the "Transform" stage output: it aggregates all computed statistics in the
 * exact structure expected by results.json.
 */
object ResultsBuilder {

  /**
   * Computes all required (and optional) statistics from validated players and embeds
   * parsing/cleaning metadata.
   *
   * @param clean validated rows + meta counts
   * @return Results ready to be serialized to JSON and rendered in a text report
   */
  def build(clean: CleanData): Results = {
    val ps = clean.players
    val m  = clean.meta

    val stats = ParsingStats(
      total_players_parsed = m.totalParsed,
      total_players_valid = m.totalValid,
      parsing_errors = m.parsingErrors,
      duplicates_removed = m.duplicatesRemoved
    )

    Results(
      statistics = stats,
      top_10_scorers = Stats.topScorers(ps),
      top_10_assisters = Stats.topAssisters(ps),
      most_valuable_players = Stats.mostValuable(ps),
      highest_paid_players = Stats.highestPaid(ps),
      players_by_league = Stats.playersByLeague(ps),
      players_by_position = Stats.playersByPosition(ps),
      average_age_by_position = Stats.averageAgeByPosition(ps),
      average_goals_by_position = Stats.averageGoalsByPosition(ps),
      discipline_statistics = Stats.discipline(ps),
      top_goal_contribution_per_match = Stats.topGoalContributionPerMatch(ps),
      top_discipline_risk_per_match = Stats.topDisciplineRiskPerMatch(ps),
      best_value_for_money = Stats.bestValueForMoney(ps)
    )
  }
}
