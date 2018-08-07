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
    override val cost: Int = configuration.underlying.getInt("buyletter.cost")
    override val cardType: CardType = CardType.BUYLETTER
    override val description: String = configuration.underlying.getString("buyletter.description")
    override val lowerLimit: Int = configuration.underlying.getInt("buyletter.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("buyletter.upperLimit")
    override var usageLimit: Int = configuration.underlying.getInt("buyletter.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = if (userPoints >= lowerLimit && userPoints <= upperLimit) true else false
}

case class RevealCategory @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("revealcategory.cost")
    override val cardType: CardType = CardType.REVEALCATEGORY
    override val description: String = configuration.underlying.getString("revealcategory.description")
    override val lowerLimit: Int = configuration.underlying.getInt("revealcategory.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("revealcategory.upperLimit")
    override var usageLimit: Int = configuration.underlying.getInt("revealcategory.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = if (userPoints >= lowerLimit && userPoints <= upperLimit) true else false
}

case class Risk @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("risk.cost")
    override val cardType: CardType = CardType.RISK
    override val description: String = configuration.underlying.getString("risk.description")
    override val lowerLimit: Int = configuration.underlying.getInt("risk.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("risk.upperLimit")
    override var usageLimit: Int = configuration.underlying.getInt("risk.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = if (userPoints >= lowerLimit && userPoints <= upperLimit) true else false

}

case class Consolation @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("consolation.cost")
    override val cardType: CardType = CardType.CONSOLATION
    override val description: String = configuration.underlying.getString("consolation.description")
    override val lowerLimit: Int = configuration.underlying.getInt("consolation.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("consolation.upperLimit")
    override var usageLimit: Int = configuration.underlying.getInt("consolation.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = if (userPoints >= lowerLimit && userPoints <= upperLimit) true else false
}

case class Discount @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("discount.cost")
    override val cardType: CardType = CardType.DISCOUNT
    override val description: String = configuration.underlying.getString("discount.description")
    override val lowerLimit: Int = configuration.underlying.getInt("discount.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("discount.upperLimit")
    override var usageLimit: Int = configuration.underlying.getInt("discount.usageLimit")

    override def isCardAffordable(userPoints: Int): Boolean = if (userPoints >= lowerLimit && userPoints <= upperLimit) true else false
}