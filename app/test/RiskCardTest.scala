package test

import models.Enums.{CardType,GameState}
import models.{Game,Move,Word}
import play.api.mvc.Result
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
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "In risk not usable case" must {
        "give an exception message" in {
            gameService.currentGame.currentCards(CardType.RISK) = 0
            gameService.currentGame.userPoint = 45
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
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
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "risk", "letter" : "a"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            val afterUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            println(contentAsString(moveResponse))
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("alphabet.alphabetCost.a")
              - configuration.underlying.getInt("cards.risk.cost"))
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
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
        }
    }

    "In risk usage with wrong guess point test" must {
        "decrease points from user after risk usage with wrong guess" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val temporaryMove: Move = new Move(
                None,
                cardService.getOneCard(Some("risk")),
                None)
            temporaryMove.updateSuccess(false)
            gameService.currentGame.moveList.append(temporaryMove)
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "c"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("alphabet.alphabetCost.c"))
        }
    }

    "In risk usage with correct guess point check" must {
        "invariable points after risk usage with correct guess" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                45,
                GameState.CONTINUE
            )
            val temporaryMove: Move = new Move(
                None,
                cardService.getOneCard(Some("risk")),
                None)
            temporaryMove.updateSuccess(true)
            gameService.currentGame.moveList.append(temporaryMove)
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"letter" : "x"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("enabled card")
            beforeUserPoint must equal(gameService.currentGame.userPoint)
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
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("cards.risk.cost"))

        }

        "throw an exception that indicates enabled card" in {
            val json = """{"letter" : "r" , "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("There is enabled")
        }
    }
    clearGame()
}
