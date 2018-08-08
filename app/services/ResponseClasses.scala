package services

case class MoveResponse(userPoint : Int,hiddenWord : String ,category : String, gameState : String, isSuccess: String)
case class GameResponse (message: String, gameState: String)
