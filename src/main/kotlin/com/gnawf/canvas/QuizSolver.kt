package com.gnawf.canvas

import com.gnawf.Environment
import com.gnawf.Environment.token
import com.gnawf.okHttpClient
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.awt.Desktop
import java.net.URI
import kotlin.collections.isNotEmpty
import kotlin.collections.set

class QuizSolver constructor(private val location: HttpUrl) : Runnable {

  private val answers = HashMap<Int, String>()

  private var attempt = 0

  override fun run() {
    val quiz = parseQuiz()

    quiz ?: throw IllegalStateException("Unable to parse quiz")

    // Hardcode non-multichoice answers
    answers[478132] = "248832"
    answers[478134] = "119"
    answers[478136] = "5"

    bruteforce(quiz)
  }

  private fun bruteforce(q: Quiz) {
    var quiz = q

    // While we don't have an answer for every question
    while (answers.size < quiz.questions.size) {
      // Safeguard - usually shouldn't have more than 6 options
      if (attempt > 6) {
        break
      }

      // Submit answers
      submit(quiz)

      // If we've answered everything correctly, quit
      if (answers.size >= quiz.questions.size) {
        Desktop.getDesktop().browse(URI("$location"))
        break
      }

      // Retake the quiz & resubmit answers in the next iteration
      quiz = quiz.copy(attempt = retakeQuiz())

      // Move onto the next answer to try
      attempt++
    }
  }

  private fun form(quiz: Quiz): FormBody.Builder {
    return FormBody.Builder()
      .add("utf8", "✓")
      .add("attempt", "${quiz.attempt}")
      .add("validation_token", quiz.token)
      .add("authenticity_token", Canvas.csrfToken)
  }

  private fun submit(quiz: Quiz) {
    val form = form(quiz)

    // Populate form with question data
    quiz.questions.forEach { question ->
      // Answer the question
      if (answers.contains(question.id)) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        form.add("question_${question.id}", answers[question.id])
      } else if (attempt < question.answers.size) {
        form.add("question_${question.id}", "${question.answers[attempt]}")
        println("question_${question.id} => ${question.answers[attempt]}")
      }
    }

    val url = location.newBuilder()
      .addPathSegment("submissions")
      .addQueryParameter("user_id", Environment.userId)
      .build()

    val request = Request.Builder()
      .url(url)
      .post(form.build())
      .build()

    val response = okHttpClient
      .newCall(request)
      .execute()

    val body = response.body()?.string() ?: return

    val document = Jsoup.parse(body)

    document.select(".display_question").forEach { element ->
      val question = parseQuestion(element)
      val correct = !element.hasClass("incorrect")
      // Mark down the correct answer
      if (correct && !answers.contains(question.id)) {
        answers[question.id] = "${question.selected}"
      }
    }

    println("Answers at this stage $answers")
  }

  private fun retakeQuiz(): Int {
    val data = FormBody.Builder()
      .add("_method", "POST")
      .add("authenticity_token", Canvas.csrfToken)
      .build()

    val request = Request.Builder()
      .url(location.newBuilder()
        .addPathSegment("take")
        .setQueryParameter("user_id", Environment.userId)
        .build())
      .post(data)
      .build()

    val response = okHttpClient
      .newCall(request)
      .execute()

    val body = response.body()?.string() ?: throw IllegalStateException("Fuck")

    val document = Jsoup.parse(body)

    val attempt: Int = try {
      document
        .select("input[name=attempt]")
        .attr("value")
        .toInt()
    } catch (ignored: NumberFormatException) {
      throw IllegalStateException("Failed to retake quiz, quitting…", ignored)
    }

    println("Retaking quiz with token $token")

    return attempt
  }

  private fun parseQuiz(): Quiz? {
    val courseId = location.pathSegments()[1].toInt()
    val quizId = location.pathSegments()[3].toInt()

    val request = Request.Builder()
      .url(location.newBuilder()
        .addPathSegment("take")
        .build())
      .build()

    val response = okHttpClient
      .newCall(request)
      .execute()

    val body = response.body()?.string() ?: return null

    val document = Jsoup.parse(body)

    // If the user is given the option to retake the quiz then action that button first
    if (document.selectFirst(".take_quiz_button") != null) {
      retakeQuiz()
      return parseQuiz()
    }

    val attempt = document
      .select("input[name=attempt]")
      .attr("value")
      .toInt()

    val token = document
      .select("input[name=validation_token]")
      .attr("value")

    val questions = document
      .select(".display_question")
      .map(this::parseQuestion)

    return Quiz(
      id = quizId,
      courseId = courseId,
      attempt = attempt,
      questions = questions,
      token = token
    )
  }

  private fun parseQuestion(question: Element): Question {
    fun Element.qid(): Int {
      return id().removePrefix("question_").toInt()
    }

    fun Element.qtext(): String {
      return select(".question_text").html()
    }

    fun Element.qselected(): Int? {
      return select("input[checked]").singleOrNull()?.id()?.removePrefix("answer-")?.toIntOrNull()
    }

    fun Element.qanswers(): List<Int> {
      // There are two tries because the answers & questions page display the options differently
      return select(".answer.answer_for_").mapNotNull { element ->
        element.id().removePrefix("answer_").toIntOrNull()
      }.takeIf(List<Int>::isNotEmpty)
        ?: select("input[value]").mapNotNull {
          // Assuming everything is an integer, hopefully this doesn't blow up
          it.attr("value").toIntOrNull()
        }
    }

    return Question(
      id = question.qid(),
      text = question.qtext(),
      selected = question.qselected(),
      answers = question.qanswers()
    )
  }

}
