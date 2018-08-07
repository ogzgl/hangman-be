package models

import org.slf4j
import org.slf4j.LoggerFactory

class Word(val word: String,val category: String) {
    val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[ Word ])
    var hiddenWord: String = word.replaceAll("[a-z]","*")
    var hiddenCategory: String = "*" * 10

    def updateHiddenWord(guessedLetter: Char): Boolean = {
        val temp = hiddenWord.toCharArray
        for (i <- 0 until word.length)
            if (word(i) == guessedLetter)
                temp(i) = word(i)
        if (hiddenWord equals new String(temp)) {
            logger.info(s"Incorrect guess with $guessedLetter.")
            false
        }
        else {
            logger.info(s"Correct guess with $guessedLetter")
            hiddenWord = new String(temp)
            true
        }
    }

    def isPositionHidden(pos: Int): Boolean = if (hiddenWord(pos) equals '*') true else false

    def usePos(pos: Int): Unit = {
        val temp = hiddenWord.toCharArray
        temp(pos) = word(pos)
        hiddenWord = new String(temp)
    }

    def isAllRevealed: Boolean = !hiddenWord.contains('*')
}