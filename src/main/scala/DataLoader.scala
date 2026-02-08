/**
 * Loads a dataset from disk, parses it, validates entries, removes duplicates and returns
 * a cleaned in-memory representation along with processing metadata.
 *
 * This module is part of the ETL "Extract + Transform (cleaning)" steps:
 *   - Extract: read file + parse JSON into PlayerRaw
 *   - Transform: validate domain rules + deduplicate
 *
 * The design keeps parsing errors separated from validation errors:
 *   - parsingErrors: JSON items that could not be decoded as PlayerRaw
 *   - invalidCount: decoded items that do not satisfy business rules
 */
object DataLoader {

    /**
    * Reads a JSON array file, decodes entries, validates them and deduplicates the result.
    *
    * @param path input JSON file path
    * @return Right(CleanData) on success, Left(errorMessage) on read or structural JSON failure
    *
    * Notes on counts:
    *   - totalParsed counts all JSON array elements (decoded + decoding failures).
    *   - totalValid counts validated and deduplicated players.
    */
    def loadAndClean(path: String): Either[String, CleanData] = {
        for {
        jsonContent <- Parsing.readFile(path)
        parsed      <- Parsing.parsePlayersWithErrors(jsonContent)
        } yield {
        val (rawPlayers, parsingErrors) = parsed

        val (validatedPlayers, invalidCount) = Validation.partitionValidated(rawPlayers)
        val (uniquePlayers, duplicatesRemoved) = Validation.dedupeById(validatedPlayers)

        val meta = MetaInfo(
            totalParsed       = rawPlayers.size + parsingErrors,
            totalValid        = uniquePlayers.size,
            parsingErrors     = parsingErrors,
            invalidCount      = invalidCount,
            duplicatesRemoved = duplicatesRemoved
        )

        CleanData(uniquePlayers, meta)
        }
    }
}
