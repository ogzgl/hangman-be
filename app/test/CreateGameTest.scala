package test

import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class CreateGameTest extends HangmanTestBuilder {
    "GameCreation controller" should {
        "welcome the visitor" in {
            val greeting = route(app, FakeRequest(GET, "/")).get
            status(greeting) mustBe OK
            contentType(greeting) mustBe Some("application/json")
            contentAsString(greeting) must include("Welcome to Hangman")
        }
    }

    "GameCreation controller" should {
        "create a game" in {
            val json = """{"level" : "easy"}"""
            val gameCreationRequest = FakeRequest(POST, "/")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val creationResponse : Future[Result] = route(app, gameCreationRequest).get
            status(creationResponse) mustBe OK
            contentType(creationResponse) mustBe Some("application/json")
            contentAsString(creationResponse) must include("userPoint")
        }
    }

    "Game controller" must {
        "make a move" in {
            val json = """{"letter" : "e"}"""
            val moveRequest = FakeRequest(POST, "/play")
              .withHeaders("Content-Type" -> "application/json")
              .withBody(json)
            val moveResponse : Future[Result] = route(app, moveRequest).get
            status(moveResponse) mustBe OK
            contentType(moveResponse) mustBe Some("application/json")
            contentAsString(moveResponse) must include("userPoint")
        }
    }
}