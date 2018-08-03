package models

object Enums {
    object LevelEnum extends Enumeration{
        type LevelEnum = Value
        val EASY, MEDIUM, HARD, INVALID = Value
    }
    object GameState extends Enumeration{
        type GameState = Value
        val CONTINUE, LOST, WON = Value
    }
    object CardType extends Enumeration {
        type CardType = Value
        val RISK, CONSOLATION, DISCOUNT, BUYLETTER, REVEALCATEGORY = Value
    }

    object MoveType extends Enumeration{
        type MoveType = Value
        val ONLYLETTER, LETTERCARD, ONLYCARD, CARDPOS, INVALID = Value
    }
}