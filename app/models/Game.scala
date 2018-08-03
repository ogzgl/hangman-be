package models

import models.Enums.{CardType, GameState, MoveType}
import models.Enums.CardType.CardType
import org.slf4j
import org.slf4j.LoggerFactory

import scala.collection.{immutable, mutable}
import scala.collection.mutable.ArrayBuffer

class Game(
            val word: Word,
            currentCards: immutable.HashMap[ CardType,Cards ],
            alphabetCost: immutable.HashMap[ Char,Int ]
          ) {
    private val remainingLetters: mutable.HashMap[ Char,Int ] = mutable.HashMap[ Char,Int ]('e' -> 1,'a' -> 1,'r' -> 1,'i' -> 1,'o' -> 1,'t' -> 1,'n' -> 1,'s' -> 1,'l' -> 1,'c' -> 1,'u' -> 1,'d' -> 1,'p' -> 1,'m' -> 1,'h' -> 1,'g' -> 1,'b' -> 1,'f' -> 1,'w' -> 1,'y' -> 1,'k' -> 1,'v' -> 1,'x' -> 1,'z' -> 1,'j' -> 1,'q' -> 1)
    val moveList: ArrayBuffer[ Move ] = ArrayBuffer(new Move(None,None,None))
    var userPoint: Int = 100
    var gameState: Enums.GameState.Value = Enums.GameState.CONTINUE

    def logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ Game ])


    //returns the state of the game.
    def stateOfGame: Enums.GameState.GameState = gameState

    //just returns the available letters which can be used.
    def notUsedLetters(): String = {
        var letterList = ""
        for (elem <- remainingLetters.toSeq.sortBy(_._1)) {
            if (elem._2 == 1) letterList += (elem._1 + " ")
        }
        letterList
    }


    // Make a move function takes the input from interaction class,
    // creates the move object
    // forwards it to preCheck.
    def makeAMove(letter: Option[ Char ],card: Option[ Cards ],position: Option[ Int ]): Unit = {
        val newMove = new Move(letter,card,position)
        preCheck(newMove)
    }

    //preCheck runs validations on the given input. Such as if the card is usable, or letter is usable
    // or checks if the given letter input actually a letter.
    private def preCheck(move: Move): Unit = {
        if (move.selectedCard.isDefined) {
            if (!isCardUsable(move.selectedCard.get)) {
                logger.info(word.hiddenWord)
            }
            else moveSeparator(move)
        }
        else if (move.guessedLetter.isDefined) {
            if (!isLetterUsable(move.guessedLetter.get))
                logger.info(word.hiddenWord)
            else moveSeparator(move)
        }
        else if (move.selectedPosition.isDefined) {
            if (move.selectedPosition.get > word.word.length || move.selectedPosition.get < 0) {
                logger.error("Given position is out of range.")
                throw new Error(s"Position range can not be smaller than 0 and can not be larger that ${word.word.length}")
            }
            else moveSeparator(move)
        }
        else moveSeparator(move)
    }

    //moveSeparator, according to match result of the moveType,
    //calls the necessary internal functions.
    private def moveSeparator(move: Move): Unit = {
        move.moveType match {
            case MoveType.ONLYLETTER => processOnlyLetter(move)
            case MoveType.ONLYCARD => processOnlyCard(move)
            case MoveType.CARDPOS => processCardPos(move)
            case MoveType.LETTERCARD => processLetterCard(move)
            case _ =>
                logger.error("Invalid, you can't do these combinations on input.")
                throw new Error("Invalid input combination.")
        }
    }

    //below 4 functions are similar to each other but each of them checks the
    //internal characteristics of the move and forward them to postProcess.
    private def processOnlyLetter(move: Move): Unit = {
        if (word.updateHiddenWord(move.guessedLetter.get)) {
            move.updateSuccess(true)
        }
        else {
            move.updateSuccess(false)
        }
        postProcess(move)
    }

    private def processOnlyCard(move: Move): Unit = {
        if (move.selectedCard.get.cardType equals CardType.REVEALCATEGORY) {
            if (isCardUsable(move.selectedCard.get)) {
                logger.info(word.category)
                currentCards(CardType.REVEALCATEGORY).usageLimit -= 1
                move.updateSuccess(false)
                postProcess(move)
            }
        }
        else {
            logger.error("This card can not be used standalone.")
            throw new Error(s"${move.selectedCard.get.cardType.toString} can not be used standalone.")
        }
    }

    private def processCardPos(move: Move): Unit = {
        if (move.selectedCard.get.cardType equals CardType.BUYLETTER)
            if (isCardUsable(move.selectedCard.get))
                if (word.isPositionHidden(move.selectedPosition.get)) {
                    word.usePos(move.selectedPosition.get)
                    move.updateSuccess(false)
                    postProcess(move)
                }
                else {
                    logger.error("Given posision was revealed before.")
                    throw new Error("Position is already revealed.")
                }
            else {
                logger.error("This card can not be used with position.")
                throw new Error("This card can not be used with position.")
            }
    }

    private def processLetterCard(move: Move): Unit = {
        if ((move.selectedCard.get.cardType equals CardType.DISCOUNT)
          || (move.selectedCard.get.cardType equals CardType.RISK)
          || (move.selectedCard.get.cardType equals CardType.CONSOLATION)) {
            if (isCardUsable(move.selectedCard.get))
                if (isLetterUsable(move.guessedLetter.get)) {
                    if (word.updateHiddenWord(move.guessedLetter.get)) {
                        move.updateSuccess(true)
                    }
                    else {
                        move.updateSuccess(false)
                    }
                    postProcess(move)
                }
        }
        else {
            logger.info(s"${move.selectedCard.get.cardType.toString} usage with letter")
            throw new Error(s"This ${move.selectedCard.get.cardType.toString} can not be used with letter")
        }
    }

    //postProcess makes updates on userPoint, card usage limit and the gameState.
    private def postProcess(move: Move): Unit = {
        if (!move.isSuccess) userPoint -= moveCostCalc(move)
        else {
            if (move.selectedCard.isDefined) {
                userPoint -= currentCards(move.selectedCard.get.cardType).cost
            }
        }
        if (move.moveType == MoveType.LETTERCARD || move.moveType == MoveType.ONLYLETTER) {
            remainingLetters(move.guessedLetter.get) = 0
        }
        if (move.selectedCard.isDefined)
            currentCards(move.selectedCard.get.cardType).usageLimit -= 1
        logger.info(s"Remaining user points: $userPoint")
        logger.info(s"Secret Word: ${word.hiddenWord}")
        moveList.append(move)
        updateGameState()
    }

    //helper function for postProcess, calculates the cost of the move,
    //with required functionality for instance risk card usage
    private def moveCostCalc(move: Move): Int = {
        val lastCard = lastCardCheck
        var moveCost = 0
        if (!lastCard._1) {
            move.moveType match {
                case MoveType.CARDPOS => moveCost = currentCards(move.selectedCard.get.cardType).cost
                case MoveType.LETTERCARD =>
                    if (move.selectedCard.get.cardType equals CardType.DISCOUNT) {
                        moveCost = currentCards(move.selectedCard.get.cardType).cost + alphabetCost(move.guessedLetter.get) / 4
                    }
                    else moveCost = currentCards(move.selectedCard.get.cardType).cost + alphabetCost(move.guessedLetter.get)
                case MoveType.ONLYCARD => moveCost = currentCards(move.selectedCard.get.cardType).cost
                case MoveType.ONLYLETTER => moveCost = alphabetCost(move.guessedLetter.get)
                case _ =>
                    logger.error("Unexpected Error!")
                    throw new Error("Move cost calculation for invalid move.")

            }
            moveCost

        }
        else {
            if (lastCard._2.get == CardType.RISK) {
                moveCost = 0

            }
            if (lastCard._2.get == CardType.CONSOLATION) {
                moveCost = alphabetCost(moveList.last.guessedLetter.get) / 2
            }
            moveCost
        }
    }

    //gameState updater, updates the game according to given conditions.
    private def updateGameState(): Unit = {
        if (userPoint < 0) gameState = GameState.LOST
        if (word.isAllRevealed) gameState = GameState.WON
    }

    //helper function checks if letter is usable, conditions that
    //are considered:
    //  + is letter used before?
    //  + is user has enough points to use that letter?
    //  + is letter actually a letter?
    private def isLetterUsable(letter: Char): Boolean = {
        if (remainingLetters(letter) > 0)
            if (userPoint > alphabetCost(letter))
                if (letter.isLetter)
                    true
                else {
                    logger.error("Given input was not letter use A-Z")
                    throw new Error(s"Given input $letter is not a A-Z letter.")
                    false
                }
            else {
                logger.error(s"Insufficient points to use letter: $letter.")
                throw new Error("Insufficient points to use letter: ${letter}.")
                false
            }
        else {
            logger.error(s"Usage of letter : $letter which used before.")
            throw new Error(s"This letter: $letter already used.")
            false
        }
    }

    //helper function, checks if card is usable, conditions that
    //are considered:
    // + is card affordable?
    // + is there any enabled card such as not successive move with risk card usage?
    // + did card reached to usage limit?
    private def isCardUsable(card: Cards): Boolean = {
        val temp = lastCardCheck
        if (card.isCardAffordable(userPoint))
            if (temp._1 equals false)
                if (currentCards(card.cardType).usageLimit > 0)
                    true
                else {
                    logger.error(s"Try to use ${card.cardType.toString} which reached its limit.")
                    throw new Error("Card's usage limit exceeded.")
                    false
                }
            else {
                logger.error(s"Try to use a card while there is an active ${temp._2.toString} card")
                throw new Error(s"There is enabled ${temp._2} card you can't make this move.")
                false
            }
        else {
            logger.error(s"Insufficient point to use card: ${card.cardType.toString}")
            throw new Error("Insufficient points to use card.")
            false
        }
    }

    //helper function, checks if the last move has enabled card
    // that effects the current move such as,
    // not successive move with risk card usage.
    private def lastCardCheck: (Boolean,Option[ CardType ]) = {
        if (moveList.last.selectedCard.isDefined) {
            if ((moveList.last.selectedCard.get.cardType equals CardType.RISK) && moveList.last.isSuccess)
                (true,Some(CardType.RISK))
            else if ((moveList.last.selectedCard.get.cardType equals CardType.CONSOLATION) && !moveList.last.isSuccess)
                (true,Some(CardType.CONSOLATION))
            else
                (false,None)
        }
        else (false,None)
    }

}
