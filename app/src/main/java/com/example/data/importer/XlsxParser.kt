package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedSheet(
    val sheetName: String,
    val rows: List<List<String>>
)

object XlsxParser {

    /**
     * Parses all sheets and their rows from an .xlsx InputStream.
     */
    fun parseXlsx(inputStream: InputStream): List<ParsedSheet> {
        val bytes = inputStream.readBytes()
        val sharedStrings = extractSharedStrings(bytes)
        val sheetMap = extractSheetMap(bytes) // sheetId/name -> target file

        val resultSheets = mutableListOf<ParsedSheet>()
        for ((sheetName, path) in sheetMap) {
            val rows = parseSheetRows(bytes, path, sharedStrings)
            if (rows.isNotEmpty()) {
                resultSheets.add(ParsedSheet(sheetName = sheetName, rows = rows))
            }
        }
        return resultSheets
    }

    private fun extractSharedStrings(bytes: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        val zip = ZipInputStream(bytes.inputStream())
        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
            if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                val parser = Xml.newPullParser()
                parser.setInput(zip, "UTF-8")
                var eventType = parser.eventType
                var inText = false
                val currentText = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "t") {
                                inText = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inText) {
                                currentText.append(parser.text)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "t") {
                                inText = false
                            } else if (parser.name == "si") {
                                strings.add(currentText.toString())
                                currentText.clear()
                            }
                        }
                    }
                    eventType = parser.next()
                }
                break
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()
        return strings
    }

    private fun extractSheetMap(bytes: ByteArray): List<Pair<String, String>> {
        // Step 1: read xl/workbook.xml to get sheet names and r:id
        val sheetDefs = mutableListOf<Triple<String, String, String>>() // name, sheetId, rId
        var zip = ZipInputStream(bytes.inputStream())
        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
            if (entry.name.equals("xl/workbook.xml", ignoreCase = true)) {
                val parser = Xml.newPullParser()
                parser.setInput(zip, "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        val sheetId = parser.getAttributeValue(null, "sheetId") ?: ""
                        // r:id might have namespace
                        var rId = parser.getAttributeValue("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id")
                        if (rId == null) {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).endsWith("id")) {
                                    rId = parser.getAttributeValue(i)
                                    break
                                }
                            }
                        }
                        sheetDefs.add(Triple(name, sheetId, rId ?: "rId$sheetId"))
                    }
                    eventType = parser.next()
                }
                break
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()

        // Step 2: read xl/_rels/workbook.xml.rels to resolve rId to target filename
        val relsMap = mutableMapOf<String, String>()
        zip = ZipInputStream(bytes.inputStream())
        entry = zip.nextEntry
        while (entry != null) {
            if (entry.name.equals("xl/_rels/workbook.xml.rels", ignoreCase = true)) {
                val parser = Xml.newPullParser()
                parser.setInput(zip, "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "Relationship") {
                        val id = parser.getAttributeValue(null, "Id") ?: ""
                        var target = parser.getAttributeValue(null, "Target") ?: ""
                        if (!target.startsWith("xl/")) {
                            target = "xl/" + target.removePrefix("/")
                        }
                        relsMap[id] = target
                    }
                    eventType = parser.next()
                }
                break
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()

        val results = mutableListOf<Pair<String, String>>()
        for ((name, sheetId, rId) in sheetDefs) {
            val resolvedTarget = relsMap[rId] ?: "xl/worksheets/sheet$sheetId.xml"
            results.add(name to resolvedTarget)
        }

        // Fallback: If workbook.xml was missing or rels failed, scan for sheet*.xml entries directly
        if (results.isEmpty()) {
            zip = ZipInputStream(bytes.inputStream())
            entry = zip.nextEntry
            var idx = 1
            while (entry != null) {
                if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                    results.add("Sheet$idx" to entry.name)
                    idx++
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            zip.close()
        }

        return results
    }

    private fun parseSheetRows(
        bytes: ByteArray,
        sheetPath: String,
        sharedStrings: List<String>
    ): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val zip = ZipInputStream(bytes.inputStream())
        var entry: ZipEntry? = zip.nextEntry
        val normalizedPath = sheetPath.replace("\\", "/").lowercase()

        while (entry != null) {
            if (entry.name.replace("\\", "/").lowercase() == normalizedPath) {
                val parser = Xml.newPullParser()
                parser.setInput(zip, "UTF-8")
                var eventType = parser.eventType

                var currentRowMap = mutableMapOf<Int, String>()
                var currentCellCol = 0
                var currentCellType = ""
                var inValueTag = false
                var inInlineText = false
                val cellText = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "row" -> {
                                    currentRowMap = mutableMapOf()
                                }
                                "c" -> {
                                    val ref = parser.getAttributeValue(null, "r") ?: ""
                                    currentCellCol = colRefToIndex(ref.takeWhile { it.isLetter() })
                                    currentCellType = parser.getAttributeValue(null, "t") ?: ""
                                    cellText.clear()
                                }
                                "v" -> inValueTag = true
                                "t" -> inInlineText = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inValueTag || inInlineText) {
                                cellText.append(parser.text)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "v" -> inValueTag = false
                                "t" -> inInlineText = false
                                "c" -> {
                                    val rawVal = cellText.toString().trim()
                                    val finalVal = if (currentCellType == "s") {
                                        val idx = rawVal.toIntOrNull()
                                        if (idx != null && idx in sharedStrings.indices) {
                                            sharedStrings[idx]
                                        } else {
                                            rawVal
                                        }
                                    } else {
                                        rawVal
                                    }
                                    currentRowMap[currentCellCol] = finalVal
                                }
                                "row" -> {
                                    if (currentRowMap.isNotEmpty()) {
                                        val maxCol = currentRowMap.keys.maxOrNull() ?: -1
                                        val rowList = ArrayList<String>(maxCol + 1)
                                        for (i in 0..maxCol) {
                                            rowList.add(currentRowMap[i] ?: "")
                                        }
                                        rows.add(rowList)
                                    }
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
                break
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()
        return rows
    }

    private fun colRefToIndex(colRef: String): Int {
        if (colRef.isEmpty()) return 0
        var index = 0
        for (ch in colRef.uppercase()) {
            if (ch in 'A'..'Z') {
                index = index * 26 + (ch - 'A' + 1)
            }
        }
        return (index - 1).coerceAtLeast(0)
    }
}
