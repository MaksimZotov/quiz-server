package questionspool

interface QuestionsPool {
    fun getQuestion(numberOfQuestion: Int): Triple<String, List<String>, Int>
}