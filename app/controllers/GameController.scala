package controllers

import javax.inject.{Inject,Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController,Action,ControllerComponents}
import services.{GameResponse,GameService,MoveResponse}
import org.slf4j
import org.slf4j.LoggerFactory
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

@Singleton
class GameController @Inject()(cc: ControllerComponents,gameService: GameService) extends AbstractController(cc) {
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ GameController ])

    //writes for move response, move response is for response json it holds necessary information.
    implicit val moveWrites: Writes[ MoveResponse ] = (
      (JsPath \ "userPoint").write[ Int ] and
        (JsPath \ "hiddenWord").write[ String ] and
        (JsPath \ "category").write[ String ] and
        (JsPath \ "gameState").write[ String ] and
        (JsPath \ "isSuccess").write[ String ]
      ) (unlift(MoveResponse.unapply))
    //writes for game response, work at the end of the game, response json that holds necessary information.
    implicit val gameWrites: Writes[ GameResponse ] = (
      (JsPath \ "info").write[ String ] and
        (JsPath \ "state").write[ String ]
      ) (unlift(GameResponse.unapply))
    //reads for move carrier.
    implicit val moveReads: Reads[ MoveCarrier ] = (
      (JsPath \ "letter").readNullable[ String ](maxLength[ String ](1) keepAnd minLength[ String ](1)) and
        (JsPath \ "card").readNullable[ String ] and
        (JsPath \ "pos").readNullable[ Int ]
      ) (MoveCarrier.apply _)

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
                logger.error(errors.toString())
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
                                "status" -> "OK",
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
