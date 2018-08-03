package services

import javax.inject.{Inject, Singleton}
import models.Enums.GameState
import models.{Enums, Game}
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.collection.immutable

@Singleton
class GameService @Inject()(cardService: CardService,wordService: WordService, configuration: Configuration){
    var currentGame : Game = _
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[GameService])

    def buildAlphabetCost: immutable.HashMap[Char,Int] = {
        val alphList = configuration.underlying.getConfigList("alphabetCost").asInstanceOf[immutable.HashMap[Char, Int]]
        alphList
    }

    def initializeGame(level : String) : Unit = {
            val gameLevel : Enums.LevelEnum.Value = {
                level match{
                    case "easy" => Enums.LevelEnum.EASY
                    case "medium" => Enums.LevelEnum.MEDIUM
                    case "hard" => Enums.LevelEnum.HARD
                    case _ => Enums.LevelEnum.INVALID
                }
            }
            if(gameLevel equals  Enums.LevelEnum.INVALID){
                logger.error("Creation of game with an invalid input.")
            }
            else{
                try{
                    currentGame = new Game(
                        wordService.getRandWord(gameLevel),
                        cardService.getCards,
                        buildAlphabetCost
                    )
                    logger.info(s"Game started successfully with $gameLevel level.")
                    logger.info("Word is: " + currentGame.word)
                }
                catch {
                    case _ : Throwable =>
                        logger.error("Game could not be created due to invalid level entry.")
                        throw new Error("Invalid level.")
                }
            }
    }

    def makeMove(letter: Option[Char], card: Option[String], position : Option[Int]): Unit ={
        try{
            if(currentGame.isInstanceOf[Game] && currentGame.stateOfGame==GameState.CONTINUE){
                if(currentGame.stateOfGame==GameState.CONTINUE){
                    currentGame.makeAMove(letter,cardService.getOneCard(card),position)
                }
                if(currentGame.stateOfGame==GameState.WON)
                    logger.info(s"Game Won with ${currentGame.userPoint} points.")
                else if(currentGame.stateOfGame==GameState.LOST)
                    logger.info("Game Lost.")
                else{
                    logger.info(currentGame.notUsedLetters())
                }

            }
        }
        catch{
            case x : Throwable =>
                logger.error(s"Error occured: ${x.toString}")
                throw new Error(s"Error : ${x.toString}")

        }
    }
}