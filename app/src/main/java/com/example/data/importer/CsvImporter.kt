package com.example.data.importer

import com.example.data.model.CompetencyMetadata
import com.example.data.model.Question
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvImporter {

    /**
     * Parses a CSV input stream containing one or more reviewer question sections.
     * Supports standard RFC 4180 CSV syntax (quoted fields, commas, escaped quotes, newlines).
     */
    fun processCsv(
        inputStream: InputStream,
        forcedReviewer: String = "GMDSS",
        existingQuestionTexts: Set<String> = emptySet()
    ): ImportValidationResult {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rows = parseCsvRows(reader)

        val validQuestions = mutableListOf<Question>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        var duplicates = 0
        var totalParsed = 0
        val seenTextsInBatch = mutableSetOf<String>()

        var currentTitle = "GMDSS Reviewer"
        var currentCompCode = "C1"
        var currentPart: Int? = null
        var sectionCount = 0

        for (rowIndex in rows.indices) {
            val row = rows[rowIndex]
            if (row.isEmpty() || row.all { it.isBlank() }) continue

            val firstCol = row[0].trim()

            // Header directives
            if (firstCol.equals("Title", ignoreCase = true)) {
                sectionCount++
                currentTitle = row.getOrNull(1)?.trim() ?: "GMDSS Reviewer"
                val (comp, part) = parseGmdssTitle(currentTitle)
                currentCompCode = comp
                currentPart = part
                continue
            }

            if (firstCol.equals("Description", ignoreCase = true) ||
                firstCol.equals("Duration", ignoreCase = true)
            ) {
                continue
            }

            if (firstCol.equals("Question", ignoreCase = true) &&
                row.any { it.contains("Option", ignoreCase = true) || it.contains("Type", ignoreCase = true) }
            ) {
                // Table header row
                continue
            }

            // Question row
            totalParsed++

            val qText = row.getOrNull(0)?.trim() ?: ""
            val qType = row.getOrNull(1)?.trim() ?: "Multiple Choice"
            val opt1 = row.getOrNull(2)?.trim() ?: ""
            val opt2 = row.getOrNull(3)?.trim() ?: ""
            val opt3 = row.getOrNull(4)?.trim() ?: ""
            val opt4 = row.getOrNull(5)?.trim() ?: ""
            val opt5 = row.getOrNull(6)?.trim() ?: ""
            val opt6 = row.getOrNull(7)?.trim() ?: ""
            val explanation = row.getOrNull(8)?.trim()
            val rawAnswer = row.getOrNull(9)?.trim() ?: ""
            val point = row.getOrNull(10)?.trim()?.toIntOrNull() ?: 1
            val section = row.getOrNull(11)?.trim()

            if (qText.isBlank()) {
                errors.add("Row ${rowIndex + 1}: Empty question text.")
                continue
            }

            val numericAnswer = rawAnswer.toIntOrNull()
            val (correctIdx, correctLetter) = when {
                numericAnswer != null && numericAnswer in 1..6 -> {
                    (numericAnswer - 1) to (('A'.code + (numericAnswer - 1)).toChar().toString())
                }
                rawAnswer.equals("A", ignoreCase = true) -> 0 to "A"
                rawAnswer.equals("B", ignoreCase = true) -> 1 to "B"
                rawAnswer.equals("C", ignoreCase = true) -> 2 to "C"
                rawAnswer.equals("D", ignoreCase = true) -> 3 to "D"
                rawAnswer.equals("E", ignoreCase = true) -> 4 to "E"
                rawAnswer.equals("F", ignoreCase = true) -> 5 to "F"
                else -> -1 to ""
            }

            if (correctIdx < 0) {
                errors.add("Row ${rowIndex + 1}: Invalid answer value '$rawAnswer'.")
                continue
            }

            val options = listOf(opt1, opt2, opt3, opt4, opt5, opt6).filter { it.isNotBlank() }
            if (correctIdx >= options.size) {
                warnings.add("Row ${rowIndex + 1}: Answer index $correctIdx exceeds available options count (${options.size}).")
            }

            val normalizedText = qText.lowercase()
            if (existingQuestionTexts.contains(normalizedText) || seenTextsInBatch.contains(normalizedText)) {
                duplicates++
            }
            seenTextsInBatch.add(normalizedText)

            val compTitle = CompetencyMetadata.getCompetencyTitle(forcedReviewer, currentCompCode)

            validQuestions.add(
                Question(
                    reviewer = forcedReviewer,
                    function = null,
                    competencyCode = currentCompCode,
                    competencyDescription = compTitle,
                    partNumber = currentPart,
                    questionText = qText,
                    questionType = qType,
                    optionA = opt1,
                    optionB = opt2,
                    optionC = opt3.ifBlank { null },
                    optionD = opt4.ifBlank { null },
                    optionE = opt5.ifBlank { null },
                    optionF = opt6.ifBlank { null },
                    correctAnswerLetter = correctLetter,
                    correctAnswerIndex = correctIdx,
                    explanation = if (explanation.isNullOrBlank()) null else explanation,
                    point = point,
                    section = if (section.isNullOrBlank()) null else section,
                    sourceSheet = currentTitle,
                    source = "GMDSS Reviewer"
                )
            )
        }

        return ImportValidationResult(
            reviewer = forcedReviewer,
            sheetCount = maxOf(sectionCount, 1),
            totalParsed = totalParsed,
            validCount = validQuestions.size,
            warningCount = warnings.size,
            duplicateCount = duplicates,
            errorCount = errors.size,
            warnings = warnings.take(30),
            errors = errors.take(30),
            validatedQuestions = validQuestions
        )
    }

    private fun parseGmdssTitle(title: String): Pair<String, Int?> {
        val upper = title.uppercase().replace(" ", "")
        val comp = when {
            upper.contains("C02") || upper.contains("C2") -> "C2"
            else -> "C1"
        }
        val partRegex = Regex("PART0*([1-9]|10)")
        val match = partRegex.find(upper)
        val part = match?.groupValues?.getOrNull(1)?.toIntOrNull()
        return comp to part
    }

    /**
     * Parses standard CSV with support for quoted multiline values.
     */
    fun parseCsvRows(reader: BufferedReader): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuote = false

        var intChar: Int
        while (reader.read().also { intChar = it } != -1) {
            val c = intChar.toChar()

            if (insideQuote) {
                if (c == '"') {
                    // Peek next character
                    reader.mark(1)
                    val nextChar = reader.read()
                    if (nextChar == '"'.code) {
                        currentField.append('"')
                    } else {
                        insideQuote = false
                        if (nextChar != -1) {
                            reader.reset()
                        }
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        insideQuote = true
                    }
                    ',' -> {
                        currentRow.add(currentField.toString())
                        currentField.setLength(0)
                    }
                    '\r' -> {
                        // ignore carriage return or handle CRLF
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString())
                        currentField.setLength(0)
                        rows.add(currentRow)
                        currentRow = mutableListOf()
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            rows.add(currentRow)
        }

        return rows
    }
}
