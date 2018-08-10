package test

import models.Enums.{CardType,GameState}
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ConsolationCardTest extends HangmanTestBuilder {
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        45,
        GameState.CONTINUE
    ))
    "Consolation usable test" must {
        "make the move if there is quota" in {
            val json = """{"card" : "consolation", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "Consolation not usable case" must {
        "give exception message" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            gameService.currentGame.currentCards(CardType.CONSOLATION) = 0
            val json = """{"card" : "consolation", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("usage limit exceeded")
        }
    }

    "In consolation sufficient points case" must {
        "make the move" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val json = """{"card" : "consolation", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }

    "In consolation insufficient points case" must {
        "give an exception message" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                1,
                GameState.CONTINUE
            )
            val json = """{"card" : "consolation", "letter" : "a"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
        }
    }
    "In consolation usage with incorrect guess point check" must {
        "incorrect guess with consolation card" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "consolation", "letter" : "x"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint
              - configuration.underlying.getInt("consolation.cost")
              - configuration.underlying.getDouble("alphabetCost.x"))
        }
        "cut down half cost after consolation usage with incorrect guess" in {
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "w"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - (configuration.underlying.getInt("alphabetCost.w") / 2))
        }
    }
    "In consolation usage with correct guess point check" must {
        "incorrect guess with consolation card" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "consolation", "letter" : "e"}"""
            val moveRequest = FakeRequest(POST,"/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse: Future[ Result ] = route(app,moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("consolation.cost"))
        }
        "cut down half cost after consolation usage with correct guess" in {
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
}
