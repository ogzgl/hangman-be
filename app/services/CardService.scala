package services
import javax.inject.Inject
import models._
import models.Enums.CardType
import org.slf4j
import org.slf4j.LoggerFactory
import scala.collection.immutable
import models.Enums.CardType.CardType

class CardService @Inject()(risk: Risk, discount: Discount, buyLetter: BuyLetter, revealCategory: RevealCategory,consolation: Consolation){
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[CardService])
    def getCards: immutable.HashMap[ CardType, Cards ] ={
        val currentCards: immutable.HashMap[ CardType,Cards ] = immutable.HashMap[ CardType, Cards ](
        CardType.DISCOUNT -> discount,
        CardType.BUYLETTER -> buyLetter,
        CardType.REVEALCATEGORY -> revealCategory,
        CardType.RISK -> risk,
        CardType.CONSOLATION -> consolation
        )
        currentCards
    }
    def getOneCard(cardName:  Option[String]): Option[Cards] ={
        cardName match {
            case Some("risk") => Some(risk)
            case Some("discount") => Some(discount)
            case Some("buy") => Some(buyLetter)
            case Some("category") => Some(revealCategory)
            case Some("consolation") => Some(consolation)
            case _ =>
                if (cardName.isDefined) {
                    logger.error("Invalid card name to use.")
                    throw new Error(s"Given card name: $cardName is invalid. Use: risk,discount,buy,category,consolation.")
                    None
                }
                else None
        }
    }
}
