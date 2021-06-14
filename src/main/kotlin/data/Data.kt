package data

interface Data

class Name(val name: String): Data
class Invitation(val whoIsInvited: String): Data
class AcceptingTheInvitation(val whoInvited: String): Data
class RefusalTheInvitation(val whoInvited: String): Data
class Question(val question: String, val answers: List<String>, val indexOfCorrectAnswer: Int): Data
class RemainingTime(val time: Double): Data
class Score(val player: Int, val anotherPlayer: Int)
class Answer(val indexOfAnswer: Int): Data
class LeavingTheGame(): Data
class RequestToPlayAgain(): Data
class RefusalToPlayAgain(): Data

