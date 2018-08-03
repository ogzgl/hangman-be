package models

import Enums.CardType
import Enums.CardType.CardType
import javax.inject.Inject
import play.api.Configuration

trait Cards {
    val cost: Int
    val description: String
    var usageLimit: Int
    val cardType: CardType
    val lowerLimit: Int
    val upperLimit: Int
    def isCardAffordable(userPoints: Int): Boolean
}

class BuyLetter @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("buyletter.cost")
    override var usageLimit: Int = configuration.underlying.getInt("buyletter.usageLimit")
    override val cardType: CardType = CardType.BUYLETTER
    override val description: String = configuration.underlying.getString("buyletter.description")
    override val lowerLimit: Int = configuration.underlying.getInt("buyletter.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("buyletter.upperLimit")
    override def isCardAffordable(userPoints: Int): Boolean = if ((userPoints >= lowerLimit && userPoints<=upperLimit) && usageLimit > 0) true else false
}

class RevealCategory @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("revealcategory.cost")
    override var usageLimit: Int = configuration.underlying.getInt("revealcategory.usageLimit")
    override val cardType: CardType = CardType.REVEALCATEGORY
    override val description: String = configuration.underlying.getString("revealcategory.description")
    override val lowerLimit: Int = configuration.underlying.getInt("revealcategory.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("revealcategory.upperLimit")
    override def isCardAffordable(userPoints: Int): Boolean = if ((userPoints >= lowerLimit && userPoints<=upperLimit) && usageLimit>0) true else false
}

class Risk @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("risk.cost")
    override var usageLimit: Int = configuration.underlying.getInt("risk.usageLimit")
    override val cardType: CardType = CardType.REVEALCATEGORY
    override val description: String = configuration.underlying.getString("risk.description")
    override val lowerLimit: Int = configuration.underlying.getInt("risk.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("risk.upperLimit")
    override def isCardAffordable(userPoints: Int): Boolean = if ((userPoints >= lowerLimit && userPoints<=upperLimit) && usageLimit>0) true else false

}

class Consolation @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("consolation.cost")
    override var usageLimit: Int = configuration.underlying.getInt("consolation.usageLimit")
    override val cardType: CardType = CardType.REVEALCATEGORY
    override val description: String = configuration.underlying.getString("consolation.description")
    override val lowerLimit: Int = configuration.underlying.getInt("consolation.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("consolation.upperLimit")
    override def isCardAffordable(userPoints: Int): Boolean = if ((userPoints >= lowerLimit && userPoints<=upperLimit) && usageLimit>0) true else false
}

class Discount @Inject()(configuration: Configuration) extends Cards {
    override val cost: Int = configuration.underlying.getInt("discount.cost")
    override var usageLimit: Int = configuration.underlying.getInt("discount.usageLimit")
    override val cardType: CardType = CardType.REVEALCATEGORY
    override val description: String = configuration.underlying.getString("discount.description")
    override val lowerLimit: Int = configuration.underlying.getInt("discount.lowerLimit")
    override val upperLimit: Int = configuration.underlying.getInt("discount.upperLimit")
    override def isCardAffordable(userPoints: Int): Boolean = if ((userPoints >= lowerLimit && userPoints<=upperLimit) && usageLimit>0) true else false
}