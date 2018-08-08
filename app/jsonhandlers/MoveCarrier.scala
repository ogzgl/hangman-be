package jsonhandlers

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{maxLength, minLength}
import play.api.libs.json.{JsPath, Reads}

//case class object that is carried through game service and game. it holds guessed letter selected card, and position
// for json in request.
case class MoveCarrier(guessedLetter: Option[ String ],selectedCard: Option[ String ],pos: Option[ Int ]) {
    var temp: Option[ Char ] = None

    def getAsChar: Option[ Char ] = {
        if (guessedLetter.isDefined)
            temp = Some(guessedLetter.get.charAt(0))
        temp
    }
}

object MoveCarrier {
    implicit val moveReads: Reads[ MoveCarrier ] = (
      (JsPath \ "letter").readNullable[ String ](maxLength[ String ](1) keepAnd minLength[ String ](1)) and
        (JsPath \ "card").readNullable[ String ] and
        (JsPath \ "pos").readNullable[ Int ]
      ) (MoveCarrier.apply _)
}