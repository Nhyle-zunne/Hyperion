package com.example

import com.example.data.model.CompetencyMetadata
import com.example.ui.AppScreen
import com.example.ui.PracticeFilterMode
import com.example.ui.StudyMode
import com.example.ui.components.LimitPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testStudyModeEnumValues() {
        val modes = StudyMode.values()
        assertTrue(modes.contains(StudyMode.TUTOR))
        assertTrue(modes.contains(StudyMode.FLASHCARD))
        assertTrue(modes.contains(StudyMode.SPEED_RUN))
        assertTrue(modes.contains(StudyMode.DRILL_EXAM))
    }

    @Test
    fun testPracticeArgsDefaultsAndCustomLimits() {
        val defaultArgs = AppScreen.Practice(title = "General Review")
        assertEquals(PracticeFilterMode.ALL, defaultArgs.filterMode)
        assertFalse(defaultArgs.isRandomized)
        assertFalse(defaultArgs.shuffleOptions)
        assertEquals(StudyMode.TUTOR, defaultArgs.studyMode)
        assertEquals(null, defaultArgs.itemLimit)

        val customDrill = AppScreen.Practice(
            title = "C1 Random 25 Blitz",
            filterMode = PracticeFilterMode.ALL,
            competencyCode = "C1",
            itemLimit = 25,
            isRandomized = true,
            shuffleOptions = true,
            studyMode = StudyMode.SPEED_RUN,
            timeLimitSecondsPerItem = 30
        )
        assertEquals(25, customDrill.itemLimit)
        assertTrue(customDrill.isRandomized)
        assertTrue(customDrill.shuffleOptions)
        assertEquals(StudyMode.SPEED_RUN, customDrill.studyMode)
        assertEquals(30, customDrill.timeLimitSecondsPerItem)
    }

    @Test
    fun testCompetencyMetadata() {
        val oicComps = CompetencyMetadata.OIC_NW_COMPETENCIES
        assertTrue(oicComps.isNotEmpty())
        val c1 = oicComps.firstOrNull { it.code == "C1" }
        assertNotNull(c1)
        assertEquals("F1", c1?.functionCode)

        val gmdssComps = CompetencyMetadata.GMDSS_COMPETENCIES
        assertTrue(gmdssComps.isNotEmpty())
        val gmdssC1 = gmdssComps.firstOrNull { it.code == "C1" }
        assertNotNull(gmdssC1)
    }

    @Test
    fun testLimitPresets() {
        assertEquals(null, LimitPreset.ALL.count)
        assertEquals(10, LimitPreset.QUICK_10.count)
        assertEquals(25, LimitPreset.STANDARD_25.count)
        assertEquals(50, LimitPreset.HALF_WATCH_50.count)
        assertEquals(-1, LimitPreset.CUSTOM.count)
    }
}
