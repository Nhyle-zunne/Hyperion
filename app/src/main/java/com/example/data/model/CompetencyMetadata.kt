package com.example.data.model

data class CompetencyInfo(
    val code: String, // e.g. "C1"
    val functionCode: String?, // e.g. "F1", "F2", "F3" or null
    val functionTitle: String?, // e.g. "Navigation at the Operational Level"
    val title: String,
    val examQuota: Int, // Official examination item count
    val reviewer: String // "OIC-NW" or "GMDSS"
)

object CompetencyMetadata {
    val OIC_NW_COMPETENCIES = listOf(
        CompetencyInfo(
            code = "C1",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Plan and conduct a passage and determine position",
            examQuota = 35,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C2",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Maintain a safe navigational watch",
            examQuota = 20,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C3",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Use of Radar and ARPA to maintain safety of navigation",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C4",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Use of ECDIS to maintain safety of navigation",
            examQuota = 10,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C5",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Respond to emergencies",
            examQuota = 10,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C7",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Use IMO Standard Marine Communication Phrases (SMCP) and English in written and oral form",
            examQuota = 20,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C9",
            functionCode = "F1",
            functionTitle = "F1 – Navigation at the Operational Level",
            title = "Manoeuvre the ship",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C10",
            functionCode = "F2",
            functionTitle = "F2 – Cargo Handling and Stowage at the Operational Level",
            title = "Monitor loading, stowage, securing and care during voyage and unloading of cargoes",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C11",
            functionCode = "F2",
            functionTitle = "F2 – Cargo Handling and Stowage at the Operational Level",
            title = "Inspect and report defect and damage to cargo spaces, hatch covers and ballast tanks",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C12",
            functionCode = "F3",
            functionTitle = "F3 – Controlling the Operation of the Ship and Care for Persons on Board",
            title = "Ensure compliance with pollution-prevention requirements",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C13",
            functionCode = "F3",
            functionTitle = "F3 – Controlling the Operation of the Ship and Care for Persons on Board",
            title = "Maintain seaworthiness of the ship",
            examQuota = 15,
            reviewer = "OIC-NW"
        ),
        CompetencyInfo(
            code = "C17",
            functionCode = "F3",
            functionTitle = "F3 – Controlling the Operation of the Ship and Care for Persons on Board",
            title = "Monitor compliance with legislative requirements",
            examQuota = 15,
            reviewer = "OIC-NW"
        )
    )

    val GMDSS_COMPETENCIES = listOf(
        CompetencyInfo(
            code = "C1",
            functionCode = null,
            functionTitle = null,
            title = "Transmit and receive information using GMDSS subsystems and equipment and fulfilling functional requirements",
            examQuota = 85,
            reviewer = "GMDSS"
        ),
        CompetencyInfo(
            code = "C2",
            functionCode = null,
            functionTitle = null,
            title = "Provide radio services in emergencies",
            examQuota = 15,
            reviewer = "GMDSS"
        )
    )

    fun getCompetencyTitle(reviewer: String, code: String): String {
        val list = if (reviewer == "OIC-NW") OIC_NW_COMPETENCIES else GMDSS_COMPETENCIES
        return list.firstOrNull { it.code.equals(code, ignoreCase = true) }?.title ?: "Competency $code"
    }

    fun getFunctionTitle(code: String): String {
        return when (code.uppercase()) {
            "F1" -> "F1 – Navigation at the Operational Level"
            "F2" -> "F2 – Cargo Handling and Stowage at the Operational Level"
            "F3" -> "F3 – Controlling Operation of Ship and Care for Persons"
            else -> code
        }
    }
}
