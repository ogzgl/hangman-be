package test

import models.Enums.{CardType,GameState}
import models.{Game,Word}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class CategoryCardTest extends HangmanTestBuilder {
    gameService.createTestableGame(new Game(
        new Word("deneme","kategori"),
        cardService.getCards,
        gameService.buildAlphabetCost,
        100,
        GameState.CONTINUE
    ))
    "Category usage quota tests" must {
        "reveal the category if quota available" in {
            val beforeUserPoint = gameService.currentGame.userPoint
            val json = """{"card" : "category"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            val category: String = (contentAsJson(moveResponse) \ "message" \ "category").as[ String ]
            category.contains('*') mustBe false
            val afterUserPoint: Int = (contentAsJson(moveResponse) \ "message" \ "userPoint").as[ Int ]
            afterUserPoint must equal(beforeUserPoint - configuration.underlying.getInt("revealcategory.cost"))
        }

        "throw an exception message if there is no quota" in {
            gameService.currentGame.currentCards(CardType.REVEALCATEGORY) = 0
            val beforeUserPoint: Int = gameService.currentGame.userPoint
            val json = """{"card" : "category"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("usage limit exceeded")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            beforeUserPoint must equal(afterUserPoint)
        }
    }

    "Category card points test" must {
        "do not reveal the category if there is insufficient points" in {
            gameService.currentGame = new Game(
                new Word("deneme","kategori"),
                cardService.getCards,
                gameService.buildAlphabetCost,
                1,
                GameState.CONTINUE
            )
            val beforeUserPoint = gameService.currentGame.userPoint
            val json = """{"card" : "category"}"""
            val moveResponse: Future[ Result ] = sendPost(json)
            status(moveResponse) mustBe EXPECTATION_FAILED
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("Insufficient points")
            val afterUserPoint: Int = gameService.currentGame.userPoint
            afterUserPoint must equal(beforeUserPoint)
        }
    }
}
