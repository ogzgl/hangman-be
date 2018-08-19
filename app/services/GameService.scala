package services

import customexceptions.{GameNotCreatedYet,InvalidInput,MoveForFinishedGame}
import javax.inject._
import jsonhandlers.{GameResponse, MoveCarrier, MoveResponse}
import models.Enums.GameState
import models.{Enums, Game}
import play.api.{Configuration,Logger}

import scala.collection.mutable

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

    def buildAlphabetCost: mutable.HashMap[ Char,Int ] = {
        val alphabetCost: mutable.HashMap[ Char,Int ] = mutable.HashMap[ Char,Int ]()
        val keys = configuration.underlying.getObject("alphabet.alphabetCost").keySet().toArray()
        for (elem <- keys) {
            alphabetCost.put(elem.toString.charAt(0),configuration.underlying.getInt(s"alphabet.alphabetCost.$elem"))
        }
        alphabetCost
    }

    def makeMove(moveCarrier: MoveCarrier): Either[ MoveResponse,GameResponse ] = {
        if (moveCarrier.giveUp.isDefined) {
            currentGame.gameState = GameState.LOST
            Right(GameResponse("You gave up.",GameState.LOST.toString))
        } else {
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