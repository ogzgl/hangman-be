package services

import customexceptions.{GameNotCreatedYet,InvalidInput,MoveForFinishedGame}
import javax.inject._
import jsonhandlers.{GameResponse, MoveCarrier, MoveResponse}
import models.Enums.GameState
import models.{Enums, Game}
import play.api.{Configuration,Logger}

import scala.collection.immutable

@Singleton
class GameService @Inject()(cardService: CardService,wordService: WordService,configuration: Configuration) {
    var currentGame: Game = _

    def createTestableGame(game: Game): Unit = {
        currentGame = game
    }

    def initializeGame(level: String): MoveResponse = {
        val gameLevel: Enums.LevelEnum.Value = {
            level.toUpperCase() match {
                case "EASY" => Enums.LevelEnum.EASY
                case "MEDIUM" => Enums.LevelEnum.MEDIUM
                case "HARD" => Enums.LevelEnum.HARD
                case x =>
                    Logger.error(x)
                    Enums.LevelEnum.INVALID
            }
        }
        if (gameLevel == Enums.LevelEnum.INVALID) {
            Logger.error("Creation of game with an invalid input.")
            throw new InvalidInput("Creation of game with an invalid input.")
        }
        else {
            try {
                currentGame = new Game(
                    wordService.getRandWord(gameLevel),
                    cardService.getCards,
                    buildAlphabetCost,
                    100,
                    GameState.CONTINUE
                )
                Logger.info(s"Game started successfully with $gameLevel level.")
                Logger.info("Word is: " + currentGame.word)
                moveResponse()
            }
            catch {
                case x: Throwable =>
                    Logger.error(x.getMessage)
                    throw new InvalidInput("Given Input was not valid, use: easy, medium or hard.")
            }
        }
    }

    def buildAlphabetCost: immutable.HashMap[ Char,Int ] = {
        val alphabetCost: immutable.HashMap[ Char,Int ] = immutable.HashMap[ Char,Int ]('e' -> 20,'a' -> 18,
            'r' -> 16,'i' -> 16,'o' -> 15,'t' -> 15,'n' -> 15,'s' -> 14,'l' -> 13,'c' -> 12,'u' -> 11,'d' -> 10,
            'p' -> 10,'m' -> 10,'h' -> 10,'g' -> 9,'b' -> 8,'f' -> 8,'w' -> 6,'y' -> 8,'k' -> 6,'v' -> 6,'x' -> 5,
            'z' -> 5,'j' -> 5,'q' -> 5)
        alphabetCost
    }

    def makeMove(moveCarrier: MoveCarrier): Either[ MoveResponse,GameResponse ] = {
        try {
            if (currentGame.isInstanceOf[ Game ] equals false) throw new GameNotCreatedYet("Game is not created, " +
              "create a game.")
            if (currentGame.stateOfGame == GameState.WON) {
                Logger.info(s"Game Won with ${currentGame.userPoint} points.")
                throw new MoveForFinishedGame(s"You Won with ${currentGame.userPoint} points. Create new game " +
                  s"to continue.")
            }
            else if (currentGame.stateOfGame == GameState.LOST) {
                Logger.info("Game Lost.")
                throw new MoveForFinishedGame("You Lost. Create new game to continue.")
            }
            else {
                currentGame.makeAMove(
                    moveCarrier.getAsChar,
                    cardService.getOneCard(moveCarrier.selectedCard),
                    moveCarrier.pos)
                if (currentGame.gameState != GameState.CONTINUE)
                    Right(gameResponse())
                else Left(moveResponse())
            }
        }
        catch {
            case exc: Exception =>
                Logger.error(s"Exception occurred.${exc.toString}")
                throw exc
            case x: Throwable =>
                Logger.error(s"Error occurred: ${x.toString}")
                throw new Error(s"Error : ${x.toString}")

        }
    }

    //    private def creationResponse()
    def moveResponse(): MoveResponse = {
        val lastCard = currentGame.lastCardCheck
        val currentMoveResponse: MoveResponse = MoveResponse(
            currentGame.userPoint,
            currentGame.word.hiddenWord,
            currentGame.word.hiddenCategory,
            currentGame.gameState.toString,
            if (currentGame.moveList.last.isSuccess) "correct" else "incorrect",
            if (lastCard._1) s"There is enabled card" else "No enabled card"
        )
        currentMoveResponse
    }

    def gameResponse(): GameResponse = {
        var msg: String = ""
        if (currentGame.gameState equals GameState.LOST) msg = "You lost"
        else msg = "You won the game"
        GameResponse(msg,currentGame.gameState.toString)
    }

    def isGameCreated: Boolean = currentGame.isInstanceOf[ Game ]
}