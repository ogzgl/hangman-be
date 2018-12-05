package controllers

import javax.inject.{Inject, Singleton}
import jsonhandlers.{GameResponse, MoveCarrier, MoveResponse}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.GameService

@Singleton
class GameController @Inject()(cc: ControllerComponents,gameService: GameService) extends AbstractController(cc) {
    // in case of get method to /play url it just returns the last response message.
    def getRequestHandler = Action {
        Ok(Json.toJson(gameService.moveResponse()))
    }
    //make move function tries to create a move carrier object and validates it.
    // if it is suitable, passes it to game service.
    // if not, or there is an exception during the move. returns the necessary response message.
    def makeMove: Action[ JsValue ] = Action(parse.json) { request =>
        val tempMove = request.body.validate[ MoveCarrier ]
        tempMove.fold(
            errors => {
                Logger.error(errors.toString())
                NotAcceptable(Json.obj(
                    "status" -> "KO","message" ->
                      Json.toJson("Given input was not valid:" +
                        "use 1 letter for guess, use risk, discount, buy,category,consolation for cards, " +
                        "use integer for position.")))

            },
            move => {
                try {
                    val response: Either[ MoveResponse,GameResponse ] = gameService.makeMove(move)
                    response match {
                        case Left(moveResponseCheck) =>
                            Ok(Json.obj(
                                "status" -> "OK",
                                "message" -> Json.toJson(moveResponseCheck)))
                        case Right(gameResponseCheck) =>
                            Ok(Json.obj(
                                "status" -> "DONE",
                                "message" -> Json.toJson(gameResponseCheck)))
                    }
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
