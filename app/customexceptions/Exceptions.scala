package customexceptions

class PositionOutOfRange(s:String) extends Exception(s)
class PositionAlreadyRevealed(s:String) extends Exception(s)

class NotValidStandaloneCard(s:String) extends Exception(s)
class InvalidInput(s: String) extends Exception(s)

class CardUsageReached(s: String) extends Exception(s)
class EnabledCardExists(s: String) extends Exception(s)

class InsufficientPoints(s: String) extends Exception(s)
class AlreadyUsedLetter(s: String) extends Exception(s)

class GameNotCreatedYet(s:String) extends Exception(s)
class MoveForFinishedGame(s: String) extends Exception(s)


