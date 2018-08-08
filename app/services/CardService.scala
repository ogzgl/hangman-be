package services
import exceptions.InvalidInput
import javax.inject._
import models._
import models.Enums.CardType
import org.slf4j
import org.slf4j.LoggerFactory

import scala.collection.immutable
import models.Enums.CardType.CardType

@Singleton
class CardService @Inject()(
                             risk: Risk,
                             discount: Discount,
                             buyLetter: BuyLetter,
                             revealCategory: RevealCategory,
                             consolation: Consolation)
{
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[CardService])
    def getCards: immutable.HashMap[ CardType, Cards ] = {
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
            case Some("risk") => Some(risk.asInstanceOf[Risk])
            case Some("discount") => Some(discount.asInstanceOf[Discount])
            case Some("buy") => Some(buyLetter.asInstanceOf[BuyLetter])
            case Some("category") => Some(revealCategory.asInstanceOf[RevealCategory])
            case Some("consolation") => Some(consolation.asInstanceOf[Consolation])
            case _ =>
                if (cardName.isDefined) {
                    logger.error("Invalid card name to use.")
                    throw new InvalidInput(s"Given card name: $cardName is invalid. Use: risk,discount,buy,category,consolation.")
                }
                else None
        }
    }
}
