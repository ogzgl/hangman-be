package jsonhandlers

import models.Enums.CardType.CardType
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.mutable


/*
* This file holds the necessary case classes for json operations in controller.
* */
case class LevelOfGame(level: String)

case class MoveResponse(userPoint: Int,
                        hiddenWord: String,
                        category: String,
                        gameState: String,
                        isSuccess: String,
                        enabledCard: String,
                        cards: mutable.HashMap[CardType, Int])

case class GameResponse(message: String,gameState: String)



object MoveResponse {
    //writes for move response, move response is for response json it holds necessary information.
    implicit val moveWrites: Writes[MoveResponse] = Json.writes[MoveResponse
      ]
}
object GameResponse {
    //writes for game response, work at the end of the game, response json that holds necessary information.
    implicit val gameWrites: Writes[ GameResponse ] = (
      (JsPath \ "info").write[ String ] and
        (JsPath \ "state").write[ String ]
      ) (unlift(GameResponse.unapply))
}

