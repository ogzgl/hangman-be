package controllers

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import customexceptions.InvalidInput
import javax.inject._
import jsonhandlers.{LevelOfGame, MoveResponse}
import play.api.Logger
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import services.GameService



@Singleton
class GameCreationController @Inject()(cc: ControllerComponents,gameService: GameService, config: Configuration) extends AbstractController(cc) {
    def greeting = Action {
        val cfgAlphabet = ConfigFactory.load("application.conf").getConfig("alphabet")
        val jsValueAlphabet = Json.parse(cfgAlphabet.root().render(ConfigRenderOptions.concise()))

        val cfgCards = ConfigFactory.load("application.conf").getConfig("cards")
        val jsValueCards = Json.parse(cfgCards.root().render(ConfigRenderOptions.concise()))

        val resultJson = Seq(jsValueAlphabet,jsValueCards)

        Ok(resultJson.foldLeft(Json.obj())((obj, a) => obj.deepMerge(a.as[JsObject])))
    }

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
