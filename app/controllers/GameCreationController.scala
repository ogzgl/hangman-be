package controllers

import customexceptions.InvalidInput
import javax.inject._
import jsonhandlers.{LevelOfGame, MoveResponse}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import services.GameService


@Singleton
class GameCreationController @Inject()(cc: ControllerComponents,gameService: GameService) extends AbstractController(cc) {
    def greeting = Action {
        Ok(Json.obj("status"->"OK", "message" -> Json.toJson("Welcome to Hangman")))
    }

    implicit val moveWrites: Writes[ MoveResponse ] = (
      (JsPath \ "userPoint").write[ Int ] and
        (JsPath \ "hiddenWord").write[ String ] and
        (JsPath \ "category").write[ String ] and
        (JsPath \ "gameState").write[ String ] and
        (JsPath \ "isSuccess").write[ String ]
      ) (unlift(MoveResponse.unapply))

    implicit val levelReads: Reads[ LevelOfGame ] = (JsPath \ "level").read[ String ].map(LevelOfGame.apply)

    def createGame: Action[ JsValue ] = Action(parse.json) { request =>
        val levelResult = request.body.validate[ LevelOfGame ]
        levelResult.fold(
            errors => {
                Logger.error("Try to game create with an invalid input.")
                NotAcceptable(Json.obj("status" -> "KO","message" ->
                  Json.toJson("Given Input was not valid. Use: easy, medium, hard.")))
            },
            lvl => {
                try{
                    val creationResponse: MoveResponse = gameService.initializeGame(lvl.level)
                    Ok(Json.obj("status" -> "OK","message" -> Json.toJson(creationResponse)))
                }catch{
                    case exception: InvalidInput => PreconditionFailed(Json.obj
                    ("status" -> "Waiting to create game",
                    "message" -> Json.toJson(exception.getMessage)))
                    case _ : Throwable => InternalServerError(Json.obj
                    ("status" -> "KO",
                        "message" -> Json.toJson("Internal Server Error.")))
                }
            }
        )
    }

}
