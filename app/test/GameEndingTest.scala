package test

import models.Enums.GameState
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class GameEndingTest extends HangmanTestBuilder {

    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        35,
        GameState.CONTINUE
    ))

    "In revealing all letters case" must {
        "tell to user game is won" in {
            gameService.currentGame.word.hiddenWord = "dene*e"
            val json = """{"letter" : "m"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("won")
        }
    }
    "In user makes wrong guess at last" must {
        "tell to user game is lost" in {
            gameService.createTestableGame(new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                1,
                GameState.CONTINUE
            ))
            gameService.currentGame.userPoint = 5
            val json = """{"letter" : "c"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("lost")
        }
    }
}
