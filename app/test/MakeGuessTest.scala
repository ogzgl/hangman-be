package test

import models.Enums.GameState
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class MakeGuessTest extends HangmanTestBuilder {
    def sendPost(json: String): Future[ Result ] = {
        val moveRequest = FakeRequest(POST,"/play")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(json)
        route(app,moveRequest).get
    }
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        100,
        GameState.CONTINUE
    ))

    gameService.currentGame.remainingLetters.add('z')
    "In non usable case" must {
        "throw exception" in {
            val json = """{"letter" : "z"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    "In usable case" must {
        "should make move" in {
            val json = """{"letter" : "a"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
        }
    }
    val tempHidden: String = gameService.currentGame.word.hiddenWord
    "In does not exist case" must {
        "real hidden word must be equal to tempHidden" in {
            val json = """{"letter" : "q"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustBe 0
        }
    }

    "In letter exist at one position case" must {
        "real hidden must differ one position from temp hidden" in {
            val json = """{"letter" : "d"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustBe 1
        }
    }
    "In letter exist at multiple position case" must {
        "real hidden must differ in multiple positions from temp hidden" in {
            val json = """{"letter" : "e"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val realHiddenWord = (contentAsJson(moveResponse) \ "message" \ "hiddenWord").get
            tempHidden.diff(realHiddenWord.toString()).length mustNot equal(1)
            tempHidden.diff(realHiddenWord.toString()).length mustNot equal(0)
        }
    }
}
