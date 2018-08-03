package services

import javax.inject.{Inject, Singleton}
import models.{Enums, Word}
import repos.WordRepo

import scala.util.Random

@Singleton
class WordService @Inject()(wr: WordRepo, word: Word) {

    def getRandWord(level: Enums.LevelEnum.LevelEnum): Word = {
        val rand = Random
        wr.wordList(level)(rand.nextInt(wr.wordList(level).length))
    }

}
