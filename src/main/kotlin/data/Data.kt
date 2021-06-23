package data

import java.io.Serializable

interface Data : Serializable

class Name(val name: String): Data
class RefusalTheName(val name: String): Data
class AcceptingTheName(val name: String): Data
class NameChange(): Data
class Invitation(val name: String): Data
class InvitationMyself(): Data
class IncorrectInvitation(val name: String): Data
class InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(val name: String): Data
class ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(val name: String): Data
class AcceptingTheInvitation(val name: String): Data
class IncorrectAcceptingTheInvitation(val name: String): Data
class PlayTheGame(): Data
class RefusalTheInvitation(val name: String): Data
class IncorrectRefusalTheInvitation(val name: String): Data
class Question(val question: String, val answers: List<String>, val indexOfCorrectAnswer: Int): Data
class RemainingTime(val time: Int): Data
class Score(val player: Int, val anotherPlayer: Int): Data
class Answer(val indexOfAnswer: Int): Data
class LeavingTheGame(): Data
class FinishTheGame(): Data
class RequestToPlayAgain(): Data
class RefusalToPlayAgain(): Data
class HardRemovalOfThePlayer(): Data
class Ping() : Data
class Pong() : Data