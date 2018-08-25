package services
import customexceptions.InvalidInput
import javax.inject._
import models._
import models.Enums.CardType
import play.api.Logger

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
            case Some("buyletter") => Some(buyLetter.asInstanceOf[BuyLetter])
            case Some("revealcategory") => Some(revealCategory.asInstanceOf[RevealCategory])
            case Some("consolation") => Some(consolation.asInstanceOf[Consolation])
            case _ =>
                if (cardName.isDefined) {
                    Logger.error("Invalid card name to use.")
                    throw new InvalidInput(s"Given card name: $cardName is invalid. Use: risk,discount,buy,category,consolation.")
                }
                else None
        }
    }
}
