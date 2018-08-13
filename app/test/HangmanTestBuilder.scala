package test

import models.Game
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST,route}
import services.{CardService,GameService}

import scala.concurrent.Future


class HangmanTestBuilder
  extends PlaySpec
    with GuiceOneAppPerSuite
    with BeforeAndAfter {
    val gameService: GameService = app.injector.instanceOf[ GameService ]
    val cardService: CardService = app.injector.instanceOf[ CardService ]
    val configuration: Configuration = app.injector.instanceOf[ Configuration ]

    def sendPost(json: String): Future[ Result ] = {
        val moveRequest = FakeRequest(POST,"/play")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(json)
        route(app,moveRequest).get
    }

    def clearGame(): Unit = {
        after {
            gameService.currentGame = _: Game
        }
    }
}
