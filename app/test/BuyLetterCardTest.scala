package test

import models.Enums.GameState
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future


class BuyLetterCardTest extends HangmanTestBuilder {
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        100,
        GameState.CONTINUE
    ))
    "Buy letter usage quota tests" must {
        "reveal the given position if quota available" in {
            val beforeUserPoint = gameService.currentGame.userPoint
            val json = """{"card" : "buy", "pos" : 0}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val resultHiddenWord: String = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").as[ String ]
            val pointAfterUsage: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            resultHiddenWord(0) mustNot equal("*")
            pointAfterUsage must equal(beforeUserPoint - configuration.underlying.getInt("buyletter.cost"))
        }

        "throw an exception message if there is no quota" in {
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "buy", "pos" : 1}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("usage limit exceeded")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            beforeUserPoint must equal(afterUserPoint)
        }
    }

    "Buy a letter point tests" must {
        "allow usage in sufficient points case" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                100,
                GameState.CONTINUE
            )
            val beforeUserPoint = gameService.currentGame.userPoint
            val json = """{"card" : "buy", "pos" : 0}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val resultHiddenWord: String = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").as[ String ]
            val afterUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            resultHiddenWord(0) mustNot equal("*")
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("buyletter.cost"))
        }
        "forbid usage in insufficient points case" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                10,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "buy", "pos" : 0}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
            val afterUsagePoint: Int = gameService.currentGame.userPoint
            beforeUserPoint must equal(afterUsagePoint)
        }
    }
}
