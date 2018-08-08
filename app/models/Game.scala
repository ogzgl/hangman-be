package models

import exceptions._
import models.Enums.CardType.CardType
import models.Enums.{CardType,GameState,MoveType}
import org.slf4j
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable,mutable}

class Game(
            val word: Word,
            currentCardsParam: immutable.HashMap[ CardType,Cards ],
            alphabetCost: immutable.HashMap[ Char,Int ]
          ) {
    val currentCards : mutable.HashMap[CardType.CardType, Int] = mutable.HashMap[CardType.CardType, Int]()
    val startingMove : Move = new Move(None,None,None)
    startingMove.success = true
    val moveList: ArrayBuffer[ Move ] = ArrayBuffer(startingMove)
    private val remainingLetters: mutable.HashSet[ Char ] = mutable.HashSet[ Char ]()
    var userPoint: Int = 100
    var gameState: Enums.GameState.Value = Enums.GameState.CONTINUE

    def logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ Game ])

    //creates  a mutable hashmap from the alphabetCost
    createUsableAlphabetCost(currentCardsParam)
    def createUsableAlphabetCost(ac : immutable.HashMap[ CardType,Cards ]): Unit = {
        for(elem <- ac){
            currentCards.put(elem._1,elem._2.usageLimit)
        }
    }

    //returns the state of the game.
    def stateOfGame: Enums.GameState.GameState = gameState

    // Make a move function takes the input from interaction class,
    // creates the move object
    // forwards it to preCheck.
    def makeAMove(letter: Option[ Char ],card: Option[ Cards ],position: Option[ Int ]): Unit = {

        val newMove = new Move(letter,card,position)
        preCheck(newMove)
        moveSeparator(newMove)
    }

    //preCheck runs validations on the given input. Such as if the card is usable, or letter is usable
    // or checks if the given letter input actually a letter.
    private def preCheck(move: Move): Unit = {
        if (move.selectedCard.isDefined) {
            isCardUsable(move.selectedCard.get)
        }
        if (move.guessedLetter.isDefined) {
            isLetterUsable(move.guessedLetter.get)
        }
        if (move.selectedPosition.isDefined) {
            isPositionUsable(move.selectedPosition.get)
        }
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
                throw new InvalidInput("Invalid input combination you can not make choice with all three or none of them or only position.")
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
                logger.info("Category card is used.")
                word.hiddenCategory = word.category
                currentCards(CardType.REVEALCATEGORY) -= 1
                move.updateSuccess(true)
                postProcess(move)
            }
        }
        else {
            logger.error("This card can not be used standalone.")
            throw new InvalidInput(s"${move.selectedCard.get.cardType.toString} can not be used standalone.")
        }
    }

    private def processCardPos(move: Move): Unit = {
        if (move.selectedCard.get.cardType equals CardType.BUYLETTER)
            if (isCardUsable(move.selectedCard.get)) {
                word.usePos(move.selectedPosition.get)
                move.updateSuccess(true)
                postProcess(move)
            }
            else {
                logger.error("This card can not be used with position.")
                throw new InvalidInput(s"This card: ${move.selectedCard.get.cardType} can not be used with position")
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
            throw new InvalidInput(s"This ${move.selectedCard.get.cardType.toString} can not be used with letter")
        }
    }

    //postProcess makes updates on userPoint, card usage limit and the gameState.
    private def postProcess(move: Move): Unit = {
        userPoint -= moveCostCalc(move)
        if (move.moveType == MoveType.LETTERCARD || move.moveType == MoveType.ONLYLETTER) {
            remainingLetters += move.guessedLetter.get
        }
        if (move.selectedCard.isDefined){
            logger.error(move.selectedCard.get.cardType.toString)
            currentCards(move.selectedCard.get.cardType) -= 1
        }
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
                case MoveType.CARDPOS => moveCost = currentCardsParam(move.selectedCard.get.cardType).cost
                case MoveType.LETTERCARD =>
                    if (move.selectedCard.get.cardType equals CardType.DISCOUNT) {
                        moveCost = currentCardsParam(move.selectedCard.get.cardType).cost
                        if(!move.isSuccess)  moveCost += alphabetCost(move.guessedLetter.get) / 4
                    }
                    else moveCost = currentCardsParam(move.selectedCard.get.cardType).cost
                    if(!move.isSuccess)  moveCost+=alphabetCost(move.guessedLetter.get)
                case MoveType.ONLYCARD => moveCost = currentCardsParam(move.selectedCard.get.cardType).cost
                case MoveType.ONLYLETTER => if(!move.isSuccess) moveCost = alphabetCost(move.guessedLetter.get) else moveCost=0
                case _ =>
                    logger.error("Unexpected Error!")
                    throw new InvalidInput("Unexpected Error: Move cost calculation for invalid move.")
            }
            moveCost
        }
        else {
            if (lastCard._2.get == CardType.RISK) {
                moveCost = 0
            }
            if (lastCard._2.get == CardType.CONSOLATION) {
                if(!move.isSuccess)
                    moveCost = alphabetCost(moveList.last.guessedLetter.get) / 2
                else moveCost = 0
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
        if (!remainingLetters.contains(letter))
            if (letter.isLetter)
                true
            else {
                logger.error("Given input was not letter use A-Z")
                throw new InvalidInput(s"Given input $letter is not a A-Z letter.")
            }
        else {
            logger.error(s"Usage of letter : $letter which used before.")
            throw new AlreadyUsedLetter(s"This letter: $letter already used.")
        }
    }

    private def isPositionUsable(pos: Int): Boolean = {
        if (pos < word.word.length || pos > 0) {
            if (word.isPositionHidden(pos))
                true
            else throw new PositionAlreadyRevealed(s"Given position $pos was already revealed.")
        }
        else throw new PositionOutOfRange(s"Position can not be smaller than 0 or can not be larger then word length: ${word.word.length}")


    }

    //helper function, checks if card is usable, conditions that
    //are considered:
    // + is card affordable?
    // + is there any enabled card such as not successive move with risk card usage?
    // + did card reached to usage limit?
    private def isCardUsable(card: Cards): Boolean = {
        val temp = lastCardCheck
        if (card.isCardAffordable(userPoint))
            if (temp._1 equals false){
                if (currentCards(card.cardType) > 0)
                    true
                else {
                    logger.error(s"Try to use ${card.cardType.toString} which reached its limit.")
                    throw new CardUsageReached("Card's usage limit exceeded.")
                }
            }
            else {
                logger.error(s"Try to use a card while there is an active ${temp._2.toString} card")
                throw new EnabledCardExists(s"There is enabled ${temp._2} card you can't make this move.")
            }
        else {
            logger.error(s"Insufficient point to use card: ${card.cardType.toString}")
            throw new InsufficientPoints("Insufficient points to use card.")
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