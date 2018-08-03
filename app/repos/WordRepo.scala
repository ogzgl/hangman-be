package repos

import java.io.File

import javax.inject._
import models.Enums.LevelEnum
import models.{Enums,Word}
import org.slf4j
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


@Singleton
class WordRepo @Inject()(word: Word) {
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ WordRepo ])
    val wordList: Map[ Enums.LevelEnum.Value,ArrayBuffer[ Word ] ] = initializeWordList()

    private def initializeWordList(): immutable.HashMap[ LevelEnum.Value,ArrayBuffer[ Word ] ] = {
        val words = immutable.HashMap[ LevelEnum.Value,ArrayBuffer[ Word ] ](
            LevelEnum.EASY -> ArrayBuffer(),
            LevelEnum.MEDIUM -> ArrayBuffer(),
            LevelEnum.HARD -> ArrayBuffer()
        )
        try {
            val path = "../app/services/hangman_words"
            println(path)
            val filesHere = new java.io.File(path).listFiles
            for (file <- filesHere) processFile(file,words)
            logger.info("Files has been read successfully.")
        } catch {
            case e: Throwable =>
                logger.error(s"Errors that can not be handled: '$e' occurred. Exiting.")
                throw new Error("Internal Error.")
        }
        words
    }

    private def processFile(
                             filename: File,
                             words: immutable.Map[ LevelEnum.Value,ArrayBuffer[ Word ] ]): Unit = {
        try {
            val source = Source.fromFile(filename)
            for (line <- source.getLines()) processLine(filename.getName,line,words)
        }
        catch {
            case e: Throwable =>
                logger.error(s"Errors that can not be handled: '$e' occurred. Exiting.")
                throw new Error("Internal Error.")
        }
    }

    // processes each line if the length is less than 5 its hard,
    // if it is between 5 and 10 its medium,
    // if it is more than 10 its easy.
    private def processLine(filename: String,line: String,words: immutable.Map[ LevelEnum.Value,ArrayBuffer[ Word ] ]): Unit = {
        if (line.length <= 5)
            words(LevelEnum.HARD).append(new Word(line.trim.toLowerCase,filename.replace(".txt","")))
        else if (line.length <= 10)
            words(LevelEnum.MEDIUM).append(new Word(line.trim.toLowerCase,filename.replace(".txt","")))
        else words(LevelEnum.EASY).append(new Word(line.trim.toLowerCase,filename.replace(".txt","")))

    }
}
