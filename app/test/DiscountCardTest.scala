package test

import models.Enums.{CardType, GameState}
import models.{Game, Word}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class DiscountCardTest extends HangmanTestBuilder {
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        35,
        GameState.CONTINUE
    ))

    "In discount usable case" must {
        "make the move" in {
            val json = """{"letter" : "z", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "In discount not usable case" must {
        "throw an exception" in {
            gameService.currentGame.currentCards(CardType.DISCOUNT) = 0
            val json = """{"letter" : "z", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
        }
    }

    "In user has enough points to use discount case" must {
        "make the move" in {
            gameService.currentGame.currentCards(CardType.DISCOUNT) = 2
            val json = """{"letter" : "t", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "In user has not enough points to use discount" must {
        "make the move" in {
            gameService.currentGame.userPoint = 1
            val json = """{"letter" : "z", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
        }
    }

    "In word does not contains letter" must {
        "make the move" in {
            gameService.currentGame.userPoint = 35
            gameService.currentGame.currentCards(CardType.DISCOUNT) = 2
            val tempHidden = gameService.currentGame.word.hiddenWord
            val tempUserPoint = gameService.currentGame.userPoint
            val json = """{"letter" : "k", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val newUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            newUserPoint must equal (tempUserPoint
              - configuration.underlying.getInt("alphabet.alphabetCost.k") / 4
              - configuration.underlying.getInt("cards.discount.cost"))
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustBe 0
        }
    }

    "In letter exist at one location" must {
        "make the move" in {
            gameService.currentGame.userPoint = 35
            gameService.currentGame.currentCards(CardType.DISCOUNT) = 2
            val tempHidden = gameService.currentGame.word.hiddenWord
            val tempUserPoint = gameService.currentGame.userPoint
            val json = """{"letter" : "d", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val newUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            newUserPoint must equal (tempUserPoint
              - configuration.underlying.getInt("cards.discount.cost"))
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustBe 1
        }
    }

    "In letter exist at multiple location" must {
        "make the move" in {
            gameService.currentGame.userPoint = 35
            gameService.currentGame.currentCards(CardType.DISCOUNT) = 2
            val tempHidden = gameService.currentGame.word.hiddenWord
            val tempUserPoint = gameService.currentGame.userPoint
            val json = """{"letter" : "e", "card" : "discount"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val newUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            newUserPoint must equal (tempUserPoint
              - configuration.underlying.getInt("cards.discount.cost"))
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustNot equal(1)
            tempHidden.diff(realHiddenWord.toString()).length mustNot equal(0)
        }
    }
    clearGame()
}
