package services

import com.google.inject
import controllers.MoveCarrier
import exceptions.{InvalidInput, gameNotCreatedYet, moveForFinishedGame}
import javax.inject.Inject
import models.Enums.GameState
import models.{Enums, Game}
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.collection.immutable

@inject.Singleton
class GameService @Inject()(cardService: CardService,wordService: WordService,configuration: Configuration) {
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ GameService ])
    var currentGame: Game = _

    def initializeGame(level: String): MoveResponse = {
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
                moveResponse()
            }
            catch {
                case x: Throwable =>
                    logger.error(x.getMessage)
                    throw new InvalidInput("Given Input was not valid, use: easy, medium or hard.")
            }
        }
    }

    def buildAlphabetCost: immutable.HashMap[ Char,Int ] = {
        val alphabetCost: immutable.HashMap[ Char,Int ] = immutable.HashMap[ Char,Int ]('e' -> 20,'a' -> 18,'r' -> 16,'i' -> 16,'o' -> 15,'t' -> 15,'n' -> 15,'s' -> 14,'l' -> 13,'c' -> 12,'u' -> 11,'d' -> 10,'p' -> 10,'m' -> 10,'h' -> 10,'g' -> 9,'b' -> 8,'f' -> 8,'w' -> 6,'y' -> 8,'k' -> 6,'v' -> 6,'x' -> 5,'z' -> 5,'j' -> 5,'q' -> 5)
        alphabetCost
    }

    def makeMove(moveCarrier: MoveCarrier): Either[MoveResponse, GameResponse]= {
        try {
            if(!currentGame.isInstanceOf[Game]) throw new gameNotCreatedYet("Game is not created, create a game.")
            if (currentGame.stateOfGame == GameState.WON){
                logger.info(s"Game Won with ${currentGame.userPoint} points.")
                throw new moveForFinishedGame(s"You Won with ${currentGame.userPoint} points. Create new game to continue.")
            }
            else if (currentGame.stateOfGame == GameState.LOST){
                logger.info("Game Lost.")
                throw new moveForFinishedGame("You Lost. Create new game to continue.")
            }
            else {
                currentGame.makeAMove(moveCarrier.getAsChar,cardService.getOneCard(moveCarrier.selectedCard),moveCarrier.pos)
                if(currentGame.gameState != GameState.CONTINUE)
                    Right(gameResponse())
                else Left(moveResponse())
            }

        }
        catch {
            case exc : Exception =>
                logger.error(s"Exception occured.${exc.toString}")
                throw exc
            case x: Throwable =>
                logger.error(s"Error occurred: ${x.toString}")
                throw new Error(s"Error : ${x.toString}")

        }
    }

    //    private def creationResponse()
    def moveResponse(): MoveResponse = {
            val currentMoveResponse: MoveResponse = MoveResponse(
                currentGame.userPoint,
                currentGame.word.hiddenWord,
                currentGame.word.hiddenCategory,
                currentGame.gameState.toString,
                if (currentGame.moveList.last.isSuccess) "correct"
                else "incorrect"
            )
            currentMoveResponse
    }

    def gameResponse() : GameResponse = {
        var msg : String = ""
        if(currentGame.gameState equals GameState.LOST) msg ="You lost"
        else msg = "You won the game"
        GameResponse(msg, currentGame.gameState.toString)
    }
}