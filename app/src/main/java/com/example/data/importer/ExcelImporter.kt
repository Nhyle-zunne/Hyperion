package com.example.data.importer

import com.example.data.model.CompetencyMetadata
import com.example.data.model.Question
import java.io.InputStream

data class ImportValidationResult(
    val reviewer: String,
    val sheetCount: Int,
    val totalParsed: Int,
    val validCount: Int,
    val warningCount: Int,
    val duplicateCount: Int,
    val errorCount: Int,
    val warnings: List<String>,
    val errors: List<String>,
    val validatedQuestions: List<Question>
)

object ExcelImporter {

    /**
     * Parses an .xlsx input stream, auto-detecting or using explicit reviewer type,
     * and produces a full validation summary with prepared Question entities.
     */
    fun processWorkbook(
        inputStream: InputStream,
        forcedReviewer: String? = null,
        existingQuestionTexts: Set<String> = emptySet()
    ): ImportValidationResult {
        val sheets = XlsxParser.parseXlsx(inputStream)

        if (sheets.isEmpty()) {
            return ImportValidationResult(
                reviewer = forcedReviewer ?: "Unknown",
                sheetCount = 0,
                totalParsed = 0,
                validCount = 0,
                warningCount = 0,
                duplicateCount = 0,
                errorCount = 1,
                warnings = emptyList(),
                errors = listOf("No worksheets found in Excel workbook."),
                validatedQuestions = emptyList()
            )
        }

        // Determine reviewer format
        val reviewer = forcedReviewer ?: detectReviewerType(sheets)

        return if (reviewer == "GMDSS") {
            parseGmdssWorkbook(sheets, existingQuestionTexts)
        } else {
            parseOicNwWorkbook(sheets, existingQuestionTexts)
        }
    }

    private fun detectReviewerType(sheets: List<ParsedSheet>): String {
        var gmdssScore = 0
        var oicScore = 0

        for (sheet in sheets) {
            val name = sheet.sheetName.uppercase()
            if (name.contains("C01") || name.contains("C02") || name.contains("PART")) {
                gmdssScore += 2
            }
            if (name.contains("F1") || name.contains("F2") || name.contains("F3")) {
                oicScore += 2
            }

            // Check column headers in the first few rows
            for (row in sheet.rows.take(6)) {
                val rowJoined = row.joinToString(" ").lowercase()
                if (rowJoined.contains("option1") || rowJoined.contains("option2")) {
                    gmdssScore += 5
                }
                if (rowJoined.contains("correct answer") && (rowJoined.contains("no.") || rowJoined.contains("no"))) {
                    oicScore += 5
                }
            }
        }

        return if (gmdssScore > oicScore) "GMDSS" else "OIC-NW"
    }

    private fun parseOicNwWorkbook(
        sheets: List<ParsedSheet>,
        existingTexts: Set<String>
    ): ImportValidationResult {
        val validQuestions = mutableListOf<Question>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        var duplicates = 0
        var totalParsed = 0
        val seenTextsInBatch = mutableSetOf<String>()

        for (sheet in sheets) {
            val sheetName = sheet.sheetName.trim()
            if (sheetName.equals("README", ignoreCase = true)) continue

            // Parse function and competency from sheet name (e.g. "F1 - C1", "F2 - C10", "C1")
            val (functionCode, compCode) = parseOicSheetName(sheetName)

            var headerRowIndex = -1
            for (i in 0 until minOf(sheet.rows.size, 10)) {
                val row = sheet.rows[i].map { it.lowercase().trim() }
                if (row.any { it.contains("question") } && row.any { it.contains("answer") || it.contains("a") }) {
                    headerRowIndex = i
                    break
                }
            }

            val dataStartIndex = if (headerRowIndex >= 0) headerRowIndex + 1 else 3

            for (rowIndex in dataStartIndex until sheet.rows.size) {
                val row = sheet.rows[rowIndex]
                if (row.all { it.isBlank() }) continue

                totalParsed++

                val qNum = row.getOrNull(0)?.trim()?.toIntOrNull()
                val qText = row.getOrNull(1)?.trim() ?: ""
                val optA = row.getOrNull(2)?.trim() ?: ""
                val optB = row.getOrNull(3)?.trim() ?: ""
                val optC = row.getOrNull(4)?.trim() ?: ""
                val optD = row.getOrNull(5)?.trim() ?: ""
                val rawAnswer = row.getOrNull(6)?.trim()?.uppercase() ?: ""

                // Validations
                if (qText.isBlank()) {
                    errors.add("Sheet '$sheetName', Row ${rowIndex + 1}: Empty question text.")
                    continue
                }

                if (optA.isBlank() || optB.isBlank()) {
                    warnings.add("Sheet '$sheetName', Row ${rowIndex + 1}: Question #$qNum has missing minimum options (A/B).")
                }

                val cleanAnswerLetter = when {
                    rawAnswer.startsWith("A") -> "A"
                    rawAnswer.startsWith("B") -> "B"
                    rawAnswer.startsWith("C") -> "C"
                    rawAnswer.startsWith("D") -> "D"
                    rawAnswer == "1" -> "A"
                    rawAnswer == "2" -> "B"
                    rawAnswer == "3" -> "C"
                    rawAnswer == "4" -> "D"
                    else -> ""
                }

                if (cleanAnswerLetter.isEmpty()) {
                    errors.add("Sheet '$sheetName', Row ${rowIndex + 1}: Invalid correct answer value '$rawAnswer'.")
                    continue
                }

                val correctIdx = cleanAnswerLetter[0] - 'A'

                // Duplicate check
                val normalizedText = qText.lowercase()
                if (existingTexts.contains(normalizedText) || seenTextsInBatch.contains(normalizedText)) {
                    duplicates++
                }
                seenTextsInBatch.add(normalizedText)

                val compTitle = CompetencyMetadata.getCompetencyTitle("OIC-NW", compCode)

                validQuestions.add(
                    Question(
                        reviewer = "OIC-NW",
                        function = functionCode,
                        competencyCode = compCode,
                        competencyDescription = compTitle,
                        questionNumber = qNum,
                        questionText = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC.ifBlank { null },
                        optionD = optD.ifBlank { null },
                        correctAnswerLetter = cleanAnswerLetter,
                        correctAnswerIndex = correctIdx,
                        sourceSheet = sheetName,
                        source = "MARINA Reviewer"
                    )
                )
            }
        }

        return ImportValidationResult(
            reviewer = "OIC-NW",
            sheetCount = sheets.size,
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

    private fun parseGmdssWorkbook(
        sheets: List<ParsedSheet>,
        existingTexts: Set<String>
    ): ImportValidationResult {
        val validQuestions = mutableListOf<Question>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        var duplicates = 0
        var totalParsed = 0
        val seenTextsInBatch = mutableSetOf<String>()

        for (sheet in sheets) {
            val sheetName = sheet.sheetName.trim()
            val (compCode, partNum) = parseGmdssSheetName(sheetName)

            var headerRowIndex = 0
            for (i in 0 until minOf(sheet.rows.size, 5)) {
                val row = sheet.rows[i].map { it.lowercase().trim() }
                if (row.any { it.contains("question") } && row.any { it.contains("option") || it.contains("type") }) {
                    headerRowIndex = i
                    break
                }
            }

            val dataStartIndex = headerRowIndex + 1

            for (rowIndex in dataStartIndex until sheet.rows.size) {
                val row = sheet.rows[rowIndex]
                if (row.all { it.isBlank() }) continue

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
                    errors.add("Sheet '$sheetName', Row ${rowIndex + 1}: Empty question text.")
                    continue
                }

                // GMDSS correct answer is stored numerically (1 to 6)
                // Answer = 1 -> Option1 (Index 0, "A")
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
                    errors.add("Sheet '$sheetName', Row ${rowIndex + 1}: Invalid GMDSS answer value '$rawAnswer'.")
                    continue
                }

                // Verify the option corresponding to the answer actually exists
                val options = listOf(opt1, opt2, opt3, opt4, opt5, opt6).filter { it.isNotBlank() }
                if (correctIdx >= options.size) {
                    warnings.add("Sheet '$sheetName', Row ${rowIndex + 1}: Answer index $correctIdx exceeds available options count (${options.size}).")
                }

                // Duplicate check
                val normalizedText = qText.lowercase()
                if (existingTexts.contains(normalizedText) || seenTextsInBatch.contains(normalizedText)) {
                    duplicates++
                }
                seenTextsInBatch.add(normalizedText)

                val compTitle = CompetencyMetadata.getCompetencyTitle("GMDSS", compCode)

                validQuestions.add(
                    Question(
                        reviewer = "GMDSS",
                        function = null,
                        competencyCode = compCode,
                        competencyDescription = compTitle,
                        partNumber = partNum,
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
                        sourceSheet = sheetName,
                        source = "GMDSS Reviewer"
                    )
                )
            }
        }

        return ImportValidationResult(
            reviewer = "GMDSS",
            sheetCount = sheets.size,
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

    private fun parseOicSheetName(sheetName: String): Pair<String, String> {
        val upper = sheetName.uppercase().replace(" ", "")
        val function = when {
            upper.contains("F1") -> "F1"
            upper.contains("F2") -> "F2"
            upper.contains("F3") -> "F3"
            else -> "F1"
        }
        val compRegex = Regex("C(1[0-7]|[1-9])")
        val match = compRegex.find(upper)
        val competency = match?.value ?: "C1"
        return function to competency
    }

    private fun parseGmdssSheetName(sheetName: String): Pair<String, Int?> {
        val upper = sheetName.uppercase().replace(" ", "")
        val comp = when {
            upper.contains("C02") || upper.contains("C2") -> "C2"
            else -> "C1"
        }
        val partRegex = Regex("PART0*([1-9]|10)")
        val match = partRegex.find(upper)
        val part = match?.groupValues?.getOrNull(1)?.toIntOrNull()
        return comp to part
    }
}
