package models
import Enums.MoveType
import Enums.MoveType.MoveType
import org.slf4j
import org.slf4j.LoggerFactory

class Move(val guessedLetter: Option[ Char ],val selectedCard: Option[ Cards ],val selectedPosition: Option[ Int ]) {
    def logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ Move ])
    val moveType: MoveType = determineMoveType()
    var success: Boolean = false
    var moveCost: Int = _

    private def determineMoveType(): MoveType = {
        if (guessedLetter.isDefined && selectedCard.isDefined && selectedPosition.isEmpty) MoveType.LETTERCARD
        else if (guessedLetter.isDefined && selectedCard.isEmpty && selectedPosition.isEmpty) MoveType.ONLYLETTER
        else if (guessedLetter.isEmpty && selectedCard.isDefined && selectedPosition.isDefined) MoveType.CARDPOS
        else if (guessedLetter.isEmpty && selectedCard.isDefined && selectedPosition.isEmpty) MoveType.ONLYCARD
        else MoveType.INVALID
    }

    def updateSuccess(result: Boolean): Unit = success = result

    def isSuccess: Boolean = success
}