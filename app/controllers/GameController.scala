package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.{GameService, MoveResponse}

case class MoveCarrier(guessedLetter: Option[ String ],selectedCard: Option[ String ],pos: Option[ Int ]){
    var temp: Option[Char] = None
    def getAsChar: Option[Char] ={
        if(guessedLetter.isDefined)
            temp = Some(guessedLetter.get.charAt(0))
        temp
    }
}

@Singleton
class GameController @Inject()(cc: ControllerComponents,gameService: GameService) extends AbstractController(cc) {
    implicit val moveWrites: Writes[ MoveResponse ] = (
      (JsPath \ "userPoint").write[ Int ] and
        (JsPath \ "hiddenWord").write[ String ] and
        (JsPath \ "category").write[ String ] and
        (JsPath \ "gameState").write[ String ] and
        (JsPath \ "isSuccess").write[ String ]
      ) (unlift(MoveResponse.unapply))

    implicit val moveReads: Reads[ MoveCarrier ] = (
      (JsPath \ "letter").readNullable[ String ](maxLength[ String ](1) keepAnd minLength[ String ](1)) and
        (JsPath \ "card").readNullable[ String ] and
        (JsPath \ "pos").readNullable[ Int ]
      ) (MoveCarrier.apply _)

    def getRequestHandler = Action {
        Ok(Json.toJson(gameService.responseInfo()))
    }

    def makeMove: Action[ JsValue ] = Action(parse.json) { request =>
        val tempMove = request.body.validate[ MoveCarrier ]
        tempMove.fold(
            errors => {
                NotAcceptable(Json.obj(
                    "status" -> "KO","message" ->
                      Json.toJson("Given input was not valid:" +
                        "use 1 letter for guess, use risk, discount, buy,category,consolation for cards, " +
                        "use integer for position.")))
            },
            move => {
                try {
                    val moveResponse: MoveResponse = gameService.makeMove(move)
                    Ok(Json.obj(
                        "status" -> "OK",
                        "message" -> Json.toJson(moveResponse)))
                } catch {
                    case exc: Exception => ExpectationFailed(Json.obj(
                        "status" -> "KO",
                        "message" -> Json.toJson(exc.getMessage)))
                    case _: Throwable => InternalServerError(Json.obj(
                        "status" -> "KO",
                        "message" -> Json.toJson("Internal Server Error.")))
                }
            }
        )

    }
}
