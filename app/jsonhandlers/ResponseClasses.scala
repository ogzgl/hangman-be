package jsonhandlers

import play.api.libs.functional.syntax._
import play.api.libs.json._

/*
* This file holds the necessary case classes for json operations in controller.
* */
case class LevelOfGame(level: String)

case class MoveResponse(userPoint: Int,hiddenWord: String,category: String,gameState: String,isSuccess: String)

case class GameResponse(message: String,gameState: String)


object MoveResponse {
    //writes for move response, move response is for response json it holds necessary information.
    implicit val moveWrites: Writes[ MoveResponse ] = (
      (JsPath \ "userPoint").write[ Int ] and
        (JsPath \ "hiddenWord").write[ String ] and
        (JsPath \ "category").write[ String ] and
        (JsPath \ "gameState").write[ String ] and
        (JsPath \ "isSuccess").write[ String ]
      ) (unlift(MoveResponse.unapply))
}

object GameResponse {
    //writes for game response, work at the end of the game, response json that holds necessary information.
    implicit val gameWrites: Writes[ GameResponse ] = (
      (JsPath \ "info").write[ String ] and
        (JsPath \ "state").write[ String ]
      ) (unlift(GameResponse.unapply))
}

