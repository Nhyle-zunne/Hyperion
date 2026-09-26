package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class PracticeSessionPayload(
    val questionIds: List<Long>,
    val answers: Map<Long, Int>, // questionId -> selectedOptionIndex
    val results: Map<Long, Boolean>, // questionId -> wasCorrect
    val filterMode: String,
    val studyMode: String,
    val competencyCode: String?,
    val partNumber: Int?,
    val itemLimit: Int?,
    val isRandomized: Boolean,
    val shuffleOptions: Boolean,
    val timeLimitSecondsPerItem: Int?,
    val randomSeed: Int
) {
    fun toJson(): String {
        val obj = JSONObject()
        val qArray = JSONArray()
        questionIds.forEach { qArray.put(it) }
        obj.put("questionIds", qArray)

        val ansObj = JSONObject()
        answers.forEach { (k, v) -> ansObj.put(k.toString(), v) }
        obj.put("answers", ansObj)

        val resObj = JSONObject()
        results.forEach { (k, v) -> resObj.put(k.toString(), v) }
        obj.put("results", resObj)

        obj.put("filterMode", filterMode)
        obj.put("studyMode", studyMode)
        if (competencyCode != null) obj.put("competencyCode", competencyCode)
        if (partNumber != null) obj.put("partNumber", partNumber)
        if (itemLimit != null) obj.put("itemLimit", itemLimit)
        obj.put("isRandomized", isRandomized)
        obj.put("shuffleOptions", shuffleOptions)
        if (timeLimitSecondsPerItem != null) obj.put("timeLimitSecondsPerItem", timeLimitSecondsPerItem)
        obj.put("randomSeed", randomSeed)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): PracticeSessionPayload {
            val obj = if (jsonStr.isNotBlank()) JSONObject(jsonStr) else JSONObject()
            val qList = mutableListOf<Long>()
            val qArray = obj.optJSONArray("questionIds")
            if (qArray != null) {
                for (i in 0 until qArray.length()) {
                    qList.add(qArray.getLong(i))
                }
            }
            val answers = mutableMapOf<Long, Int>()
            val ansObj = obj.optJSONObject("answers")
            if (ansObj != null) {
                val keys = ansObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    key.toLongOrNull()?.let { id ->
                        answers[id] = ansObj.getInt(key)
                    }
                }
            }
            val results = mutableMapOf<Long, Boolean>()
            val resObj = obj.optJSONObject("results")
            if (resObj != null) {
                val keys = resObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    key.toLongOrNull()?.let { id ->
                        results[id] = resObj.getBoolean(key)
                    }
                }
            }
            return PracticeSessionPayload(
                questionIds = qList,
                answers = answers,
                results = results,
                filterMode = obj.optString("filterMode", "ALL"),
                studyMode = obj.optString("studyMode", "TUTOR"),
                competencyCode = if (obj.has("competencyCode")) obj.optString("competencyCode") else null,
                partNumber = if (obj.has("partNumber")) obj.optInt("partNumber") else null,
                itemLimit = if (obj.has("itemLimit")) obj.optInt("itemLimit") else null,
                isRandomized = obj.optBoolean("isRandomized", false),
                shuffleOptions = obj.optBoolean("shuffleOptions", false),
                timeLimitSecondsPerItem = if (obj.has("timeLimitSecondsPerItem")) obj.optInt("timeLimitSecondsPerItem") else null,
                randomSeed = obj.optInt("randomSeed", 1)
            )
        }
    }
}

data class ExamSessionPayload(
    val questionIds: List<Long>,
    val userAnswers: Map<Long, Int>,
    val flaggedQuestionIds: List<Long>,
    val timeLimitSeconds: Long,
    val timeRemainingSeconds: Long,
    val examType: String
) {
    fun toJson(): String {
        val obj = JSONObject()
        val qArray = JSONArray()
        questionIds.forEach { qArray.put(it) }
        obj.put("questionIds", qArray)

        val ansObj = JSONObject()
        userAnswers.forEach { (k, v) -> ansObj.put(k.toString(), v) }
        obj.put("userAnswers", ansObj)

        val flagArray = JSONArray()
        flaggedQuestionIds.forEach { flagArray.put(it) }
        obj.put("flaggedQuestionIds", flagArray)

        obj.put("timeLimitSeconds", timeLimitSeconds)
        obj.put("timeRemainingSeconds", timeRemainingSeconds)
        obj.put("examType", examType)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): ExamSessionPayload {
            val obj = if (jsonStr.isNotBlank()) JSONObject(jsonStr) else JSONObject()
            val qList = mutableListOf<Long>()
            val qArray = obj.optJSONArray("questionIds")
            if (qArray != null) {
                for (i in 0 until qArray.length()) {
                    qList.add(qArray.getLong(i))
                }
            }
            val answers = mutableMapOf<Long, Int>()
            val ansObj = obj.optJSONObject("userAnswers")
            if (ansObj != null) {
                val keys = ansObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    key.toLongOrNull()?.let { id ->
                        answers[id] = ansObj.getInt(key)
                    }
                }
            }
            val flagged = mutableListOf<Long>()
            val flagArray = obj.optJSONArray("flaggedQuestionIds")
            if (flagArray != null) {
                for (i in 0 until flagArray.length()) {
                    flagged.add(flagArray.getLong(i))
                }
            }
            return ExamSessionPayload(
                questionIds = qList,
                userAnswers = answers,
                flaggedQuestionIds = flagged,
                timeLimitSeconds = obj.optLong("timeLimitSeconds", 0L),
                timeRemainingSeconds = obj.optLong("timeRemainingSeconds", 0L),
                examType = obj.optString("examType", "OFFICIAL_SIMULATION")
            )
        }
    }
}
