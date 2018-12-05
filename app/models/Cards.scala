package models

import javax.inject.Inject
import models.Enums.CardType
import models.Enums.CardType.CardType
import play.api.Configuration

trait Cards {
    val cost: Int
    val description: String
    val cardType: CardType
    val lowerLimit: Int
    val upperLimit: Int
    var usageLimit: Int

    def isCardAffordable(userPoints: Int): Boolean
}

case class BuyLetter @Inject()(configuration: Configuration) extends Cards {
  override val cost: Int = configuration.underlying.getInt("cards.buyletter.cost")
    override val cardType: CardType = CardType.BUYLETTER
  override val description: String = configuration.underlying.getString("cards.buyletter.description")
  override val lowerLimit: Int = configuration.underlying.getInt("cards.buyletter.lowerLimit")
  override val upperLimit: Int = configuration.underlying.getInt("cards.buyletter.upperLimit")
  override var usageLimit: Int = configuration.underlying.getInt("cards.buyletter.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = {
        if (userPoints >= lowerLimit && userPoints <= upperLimit) true
        else false}
}

case class RevealCategory @Inject()(configuration: Configuration) extends Cards {
  override val cost: Int = configuration.underlying.getInt("cards.revealcategory.cost")
    override val cardType: CardType = CardType.REVEALCATEGORY
  override val description: String = configuration.underlying.getString("cards.revealcategory.description")
  override val lowerLimit: Int = configuration.underlying.getInt("cards.revealcategory.lowerLimit")
  override val upperLimit: Int = configuration.underlying.getInt("cards.revealcategory.upperLimit")
  override var usageLimit: Int = configuration.underlying.getInt("cards.revealcategory.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = {
        if (userPoints >= lowerLimit && userPoints <= upperLimit) true
        else false}
}

case class Risk @Inject()(configuration: Configuration) extends Cards {
  override val cost: Int = configuration.underlying.getInt("cards.risk.cost")
    override val cardType: CardType = CardType.RISK
  override val description: String = configuration.underlying.getString("cards.risk.description")
  override val lowerLimit: Int = configuration.underlying.getInt("cards.risk.lowerLimit")
  override val upperLimit: Int = configuration.underlying.getInt("cards.risk.upperLimit")
  override var usageLimit: Int = configuration.underlying.getInt("cards.risk.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = {
        if (userPoints >= lowerLimit && userPoints <= upperLimit) true
        else false}

}

case class Consolation @Inject()(configuration: Configuration) extends Cards {
  override val cost: Int = configuration.underlying.getInt("cards.consolation.cost")
    override val cardType: CardType = CardType.CONSOLATION
  override val description: String = configuration.underlying.getString("cards.consolation.description")
  override val lowerLimit: Int = configuration.underlying.getInt("cards.consolation.lowerLimit")
  override val upperLimit: Int = configuration.underlying.getInt("cards.consolation.upperLimit")
  override var usageLimit: Int = configuration.underlying.getInt("cards.consolation.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = {
        if (userPoints >= lowerLimit && userPoints <= upperLimit) true
        else false}
}

case class Discount @Inject()(configuration: Configuration) extends Cards {
  override val cost: Int = configuration.underlying.getInt("cards.discount.cost")
    override val cardType: CardType = CardType.DISCOUNT
  override val description: String = configuration.underlying.getString("cards.discount.description")
  override val lowerLimit: Int = configuration.underlying.getInt("cards.discount.lowerLimit")
  override val upperLimit: Int = configuration.underlying.getInt("cards.discount.upperLimit")
  override var usageLimit: Int = configuration.underlying.getInt("cards.discount.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = {
        if (userPoints >= lowerLimit && userPoints <= upperLimit) true
        else false}
}