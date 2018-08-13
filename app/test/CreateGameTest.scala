package test

import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class CreateGameTest extends HangmanTestBuilder {
    override def sendPost(json: String): Future[ Result ] = {
        val moveRequest = FakeRequest(POST,"/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(json)
        route(app,moveRequest).get
    }
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
            val creationResponse: Future[ Result ] = sendPost(json)
            status(creationResponse) mustBe OK
            contentType(creationResponse) mustBe Some("application/json")
            contentAsString(creationResponse) must include("userPoint")
        }
    }
}