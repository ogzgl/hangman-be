package test

import models.Enums.{CardType,GameState}
import models.{Game,Move,Word}
import play.api.mvc.Result
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
            val moveResponse: Future[ Result ] = sendPost(json)
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
            val moveResponse: Future[ Result ] = sendPost(json)
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
            val moveResponse: Future[ Result ] = sendPost(json)
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
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
        }
    }
    "In consolation usage with incorrect guess point check" must {
        "cut down half cost after consolation usage with incorrect guess" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val temporaryMove: Move = new Move(
                None,
                cardService.getOneCard(Some("consolation")),
                None)
            temporaryMove.updateSuccess(false)
            gameService.currentGame.moveList.append(temporaryMove)
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "w"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("enabled card")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - (configuration.underlying.getInt("alphabetCost.w") / 2))
        }
    }
    "In consolation usage with correct guess point check" must {
        "cut down half cost after consolation usage with correct guess" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val temporaryMove: Move = new Move(
                None,
                cardService.getOneCard(Some("consolation")),
                None)
            temporaryMove.updateSuccess(true)
            gameService.currentGame.moveList.append(temporaryMove)
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "c"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("alphabetCost.c"))
        }
    }
    clearGame()
}
