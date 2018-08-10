package test

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import services.{CardService,GameService}


class HangmanTestBuilder extends PlaySpec with GuiceOneAppPerSuite {
    val gameService: GameService = app.injector.instanceOf[ GameService ]
    val cardService: CardService = app.injector.instanceOf[ CardService ]
    val configuration: Configuration = app.injector.instanceOf[ Configuration ]


}
