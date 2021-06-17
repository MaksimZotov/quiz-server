package data

import java.io.Serializable

interface Data : Serializable

class Name(val name: String): Data
class RefusalTheName(val name: String): Data
class AcceptingTheName(val name: String): Data
class Invitation(val name: String): Data
class IncorrectInvitation(val name: String): Data
class InvitedPlayerIsWaitingForAcceptingTheInvitationFromAnotherPlayer(val name: String): Data
class ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(val name: String): Data
class AcceptingTheInvitation(val name: String): Data
class PlayTheGame(): Data
class RefusalTheInvitation(val whoInvited: String): Data
class Question(val question: String, val answers: List<String>, val indexOfCorrectAnswer: Int): Data
class RemainingTime(val time: Double): Data
class Score(val player: Int, val anotherPlayer: Int): Data
class Answer(val indexOfAnswer: Int): Data
class LeavingTheGame(): Data
class FinishTheGame(): Data
class RequestToPlayAgain(): Data
class RefusalToPlayAgain(): Data
class Exit(): Data

