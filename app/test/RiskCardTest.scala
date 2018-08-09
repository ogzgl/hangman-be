package test

import models.Enums.{CardType,GameState}
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RiskCardTest extends HangmanTestBuilder {
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        45,
        GameState.CONTINUE
    ))
    "In risk usable case" must {
        "make the move" in {
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "In risk not usable case" must {
        "give an exception message" in {
            gameService.currentGame.currentCards(CardType.RISK) = 0
            gameService.currentGame.userPoint = 45
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("usage limit exceeded")
        }
    }
    "In risk sufficient points case" must {
        "make the move" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }

    "In risk insufficient points case" must {
        "give an exception message" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                1,
                GameState.CONTINUE
            )
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
        }
    }

    "In risk usage with wrong guess point test" must {
        "wrong guess with risk card" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint
              - configuration.underlying.getInt("alphabetCost.a")
              - configuration.underlying.getInt("risk.cost"))
        }

        "decrease points from user after risk usage with wrong guess" in {
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "c"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("alphabetCost.c"))
        }
    }

    "In risk usage with correct guess point check" must {
        "correct guess with risk card" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "risk", "letter" : "d"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("risk.cost"))
        }
        "invariable points after risk usage with correct guess" in {
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "x"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint)
        }
    }


    "Trying to use another card after using risk card" must {
        "preparation for next move" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "risk", "letter" : "d"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("risk.cost"))

        }

        "throw an exception that indicates enabled card" in {
            val json = """{"letter" : "r" , "card" : "discount"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("There is enabled")
        }
    }

}
