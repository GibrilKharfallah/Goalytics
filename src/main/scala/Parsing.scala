import java.io.{BufferedReader, FileReader}
import scala.util.{Try, Using}

import io.circe.Decoder
import io.circe.parser.{decode, parse}

/**
 * File reading and JSON decoding utilities for the input dataset.
 *
 * The module provides two parsing modes:
 *   - parsePlayers: decodes the full array in one shot (fails fast)
 *   - parsePlayersWithErrors: decodes entry by entry to tolerate dirty data and count failures
 */
object Parsing {

    /**
    * Circe decoder for PlayerRaw.
    *
    * The field order must match PlayerRaw.apply parameter order.
    * Optional fields (club, marketValue, salary) allow dirty inputs with missing/null values.
    */
    given Decoder[PlayerRaw] = Decoder.forProduct14(
        "id",
        "name",
        "age",
        "nationality",
        "position",
        "club",
        "league",
        "goalsScored",
        "assists",
        "matchesPlayed",
        "yellowCards",
        "redCards",
        "marketValue",
        "salary"
    )(PlayerRaw.apply)

    /**
    * Reads a whole file as a single String.
    *
    * @param path filesystem path to the JSON file
    * @return Right(content) or Left(errorMessage) if the file cannot be read
    */
    def readFile(path: String): Either[String, String] = {
        Try {
        val sb = new StringBuilder
        Using.resource(new BufferedReader(new FileReader(path))) { reader =>
            var line = reader.readLine()
            while (line != null) {
            sb.append(line)
            line = reader.readLine()
            }
        }
        sb.toString()
        }.toEither.left.map(e => s"Failed to read file: ${e.getMessage}")
    }

    /**
    * Decodes the entire JSON payload as a List[PlayerRaw].
    * This is convenient for clean data but fails if any entry is invalid.
    */
    def parsePlayers(jsonString: String): Either[String, List[PlayerRaw]] =
        decode[List[PlayerRaw]](jsonString).left.map(err => s"Parsing error: ${err.getMessage}")

    /**
    * Robust decoding: parses the root JSON array and decodes each element separately.
    *
    * This allows:
    *   - keeping valid rows even if some items are malformed
    *   - counting decoding failures as parsing errors
    *
    * @return Right((decodedRows, parsingErrors)) or Left(errorMessage) for invalid root JSON
    */
    def parsePlayersWithErrors(jsonString: String): Either[String, (List[PlayerRaw], Int)] = {
        for {
        json <- parse(jsonString).left.map(err => s"Invalid JSON: ${err.message}")
        arr  <- json.asArray.toRight("Expected a JSON array at root")
        } yield {
        val (valids, errors) = arr.foldLeft((List.empty[PlayerRaw], 0)) {
            case ((acc, errCount), itemJson) =>
            itemJson.as[PlayerRaw] match {
                case Right(p) => (p :: acc, errCount)
                case Left(_)  => (acc, errCount + 1)
            }
        }
        (valids.reverse, errors)
        }
    }
}
