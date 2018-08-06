package services

import exceptions.InvalidInput
import javax.inject.{Inject, Singleton}
import models.Enums.{CardType, GameState}
import models.{Enums, Game}
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.collection.immutable

@Singleton
class GameService @Inject()(cardService: CardService,wordService: WordService,configuration: Configuration) {
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ GameService ])
    var currentGame: Game = _

    def initializeGame(level: String): Unit = {
        val gameLevel: Enums.LevelEnum.Value = {
            level.toUpperCase() match {
                case "EASY" => Enums.LevelEnum.EASY
                case "MEDIUM" => Enums.LevelEnum.MEDIUM
                case "HARD" => Enums.LevelEnum.HARD
                case x =>
                    logger.error(x)
                    Enums.LevelEnum.INVALID
            }
        }
        if (gameLevel == Enums.LevelEnum.INVALID) {
            logger.error("Creation of game with an invalid input.")
            throw new InvalidInput("Creation of game with an invalid input.")
        }
        else {
            try {
                currentGame = new Game(
                    wordService.getRandWord(gameLevel),
                    cardService.getCards,
                    buildAlphabetCost
                )
                logger.info(s"Game started successfully with $gameLevel level.")
                logger.info("Word is: " + currentGame.word)
            }
            catch {
                case x: Throwable =>
                    logger.error(x.getMessage)
                    throw new InvalidInput("Invalid level.")
            }
        }
    }

    def buildAlphabetCost: immutable.HashMap[ Char,Int ] = {
        val alphabetCost: immutable.HashMap[ Char,Int ] = immutable.HashMap[ Char,Int ]('e' -> 20,'a' -> 18,'r' -> 16,'i' -> 16,'o' -> 15,'t' -> 15,'n' -> 15,'s' -> 14,'l' -> 13,'c' -> 12,'u' -> 11,'d' -> 10,'p' -> 10,'m' -> 10,'h' -> 10,'g' -> 9,'b' -> 8,'f' -> 8,'w' -> 6,'y' -> 8,'k' -> 6,'v' -> 6,'x' -> 5,'z' -> 5,'j' -> 5,'q' -> 5)
        alphabetCost
    }

    def makeMove(letter: Option[ Char ],card: Option[ String ],position: Option[ Int ]): (Int, String, String, Boolean) = {
        try {
            if (currentGame.isInstanceOf[ Game ] && currentGame.stateOfGame == GameState.CONTINUE) {
                if (currentGame.stateOfGame == GameState.CONTINUE) currentGame.makeAMove(letter,cardService.getOneCard(card),position)
                if (currentGame.stateOfGame == GameState.WON)
                    logger.info(s"Game Won with ${currentGame.userPoint} points.")
                else if (currentGame.stateOfGame == GameState.LOST)
                    logger.info("Game Lost.")
                else logger.info(s"Game continues with ${currentGame.userPoint}")
            }
            responseInfo()
        }
        catch {
            case x: Throwable =>
                logger.error(s"Error occurred: ${x.toString}")
                throw new Error(s"Error : ${x.toString}")

        }
    }

    case class MoveResponse()

    private def responseInfo(): (Int, String, String, Boolean)={
        if(currentGame.moveList.last.isSuccess)
            if((currentGame.moveList.last.selectedCard.get.cardType equals CardType.REVEALCATEGORY)
                    && currentGame.moveList.last.isSuccess)
                (currentGame.userPoint, currentGame.word.category, currentGame.gameState.toString,currentGame.moveList.last.isSuccess)



//        if(currentGame.moveList.last.selectedCard.isDefined)
//            if((currentGame.moveList.last.selectedCard.get.cardType equals CardType.REVEALCATEGORY)
//            && currentGame.moveList.last.isSuccess)
//                (currentGame.userPoint, currentGame.word.category, currentGame.gameState.toString,currentGame.moveList.last.isSuccess)
//        (currentGame.userPoint, currentGame.word.hiddenWord,currentGame.gameState.toString,currentGame.moveList.last.isSuccess)
    }
}