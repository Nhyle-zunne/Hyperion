package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hyperion", appName)
  }

  @Test
  fun `test practice session payload serialization with Robolectric`() {
    val original = com.example.data.model.PracticeSessionPayload(
      questionIds = listOf(101L, 102L, 103L),
      answers = mapOf(101L to 2, 102L to 0),
      results = mapOf(101L to true, 102L to false),
      filterMode = "ALL",
      studyMode = "TUTOR",
      competencyCode = "C1",
      partNumber = 1,
      itemLimit = 25,
      isRandomized = true,
      shuffleOptions = false,
      timeLimitSecondsPerItem = null,
      randomSeed = 42
    )
    val json = original.toJson()
    val restored = com.example.data.model.PracticeSessionPayload.fromJson(json)

    assertEquals(original.questionIds, restored.questionIds)
    assertEquals(original.answers, restored.answers)
    assertEquals(original.results, restored.results)
    assertEquals(original.filterMode, restored.filterMode)
    assertEquals(original.studyMode, restored.studyMode)
    assertEquals(original.competencyCode, restored.competencyCode)
    assertEquals(original.partNumber, restored.partNumber)
    assertEquals(original.itemLimit, restored.itemLimit)
    assertEquals(original.isRandomized, restored.isRandomized)
    assertEquals(original.randomSeed, restored.randomSeed)
  }

  @Test
  fun `test exam session payload serialization with Robolectric`() {
    val original = com.example.data.model.ExamSessionPayload(
      questionIds = listOf(201L, 202L, 203L, 204L),
      userAnswers = mapOf(201L to 1, 203L to 3),
      flaggedQuestionIds = listOf(202L),
      timeLimitSeconds = 5400L,
      timeRemainingSeconds = 4200L,
      examType = "OFFICIAL_SIMULATION"
    )
    val json = original.toJson()
    val restored = com.example.data.model.ExamSessionPayload.fromJson(json)

    assertEquals(original.questionIds, restored.questionIds)
    assertEquals(original.userAnswers, restored.userAnswers)
    assertEquals(original.flaggedQuestionIds, restored.flaggedQuestionIds)
    assertEquals(original.timeLimitSeconds, restored.timeLimitSeconds)
    assertEquals(original.timeRemainingSeconds, restored.timeRemainingSeconds)
    assertEquals(original.examType, restored.examType)
  }
}
