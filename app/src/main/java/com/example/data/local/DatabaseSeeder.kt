package com.example.data.local

import android.content.Context
import com.example.data.importer.BackupManager
import com.example.data.importer.CsvImporter
import com.example.data.importer.ExcelImporter
import com.example.data.model.CompetencyMetadata
import com.example.data.model.Question

object DatabaseSeeder {

    suspend fun scanAndSeedFromAssets(context: Context, dao: QuestionDao): Int {
        var totalLoaded = 0
        try {
            val assetList = context.assets.list("") ?: emptyArray()
            for (filename in assetList) {
                if (filename.endsWith(".csv", ignoreCase = true)) {
                    context.assets.open(filename).use { stream ->
                        val result = CsvImporter.processCsv(stream)
                        if (result.validatedQuestions.isNotEmpty()) {
                            result.validatedQuestions.chunked(200).forEach { chunk ->
                                dao.insertQuestions(chunk)
                            }
                            totalLoaded += result.validCount
                        }
                    }
                } else if (filename.endsWith(".xlsx", ignoreCase = true) || filename.endsWith(".xls", ignoreCase = true)) {
                    context.assets.open(filename).use { stream ->
                        val result = ExcelImporter.processWorkbook(stream)
                        if (result.validatedQuestions.isNotEmpty()) {
                            result.validatedQuestions.chunked(200).forEach { chunk ->
                                dao.insertQuestions(chunk)
                            }
                            totalLoaded += result.validCount
                        }
                    }
                } else if (filename.endsWith(".json", ignoreCase = true) || filename.endsWith(".hyperion", ignoreCase = true)) {
                    context.assets.open(filename).use { stream ->
                        val jsonString = stream.bufferedReader().use { it.readText() }
                        val db = AppDatabase.getInstance(context)
                        val count = BackupManager.restoreFromJson(db, jsonString)
                        totalLoaded += count
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return totalLoaded
    }

    suspend fun seedIfEmpty(dao: QuestionDao, context: Context? = null) {
        if (context != null) {
            val loadedFromAssets = scanAndSeedFromAssets(context, dao)
            val gmdssCount = dao.getQuestionCount("GMDSS")
            if (loadedFromAssets > 0 && gmdssCount > 50) return
        }

        val totalCount = dao.getTotalQuestionCount()
        val gmdssCount = dao.getQuestionCount("GMDSS")
        if (totalCount >= 50 && gmdssCount >= 10) return

        val seedQuestions = mutableListOf<Question>()

        // ==========================================
        // OIC-NW: F1 - NAVIGATION AT OPERATIONAL LEVEL
        // ==========================================

        // F1 - C1: Passage planning & position fixing
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C1"),
                questionNumber = 1,
                questionText = "If there are small cumulus clouds in the morning in summer, what kind of cloud cover would you expect and is the reasonable to forecast later in the day?",
                optionA = "CB cloud.",
                optionB = "Haze",
                optionC = "Clear skies.",
                optionD = "ST and drizzle",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "In summer, morning cumulus often develops vertically into cumulonimbus (CB) clouds in the afternoon due to thermal convective currents.",
                sourceSheet = "F1 - C1",
                source = "PIETRO"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C1"),
                questionNumber = 2,
                questionText = "When planning a passage, what are the four fundamental stages recommended by IMO Resolution A.893(21)?",
                optionA = "Appraisal, Planning, Execution, and Monitoring",
                optionB = "Inspection, Departure, Navigation, and Arrival",
                optionC = "Briefing, Plotting, Waypoints, and Debriefing",
                optionD = "Positioning, Tracking, Steering, and Mooring",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMO guidelines for voyage planning delineate the 4 stages: Appraisal, Planning, Execution, and Monitoring.",
                sourceSheet = "F1 - C1",
                source = "Ciriaco"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C1"),
                questionNumber = 3,
                questionText = "A rhumb line appears as which curve on a Mercator chart projection?",
                optionA = "A straight line",
                optionB = "A circle curved towards the equator",
                optionC = "A curve concave to the equator",
                optionD = "A spiral line",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "On a Mercator projection, any line of constant compass course (rhumb line or loxodrome) plots as a straight line.",
                sourceSheet = "F1 - C1",
                source = "TEKNIK"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C1"),
                questionNumber = 4,
                questionText = "In terrestrial navigation, how many simultaneous visual bearing lines are required to obtain a verified position fix with an index of error?",
                optionA = "At least three lines of position",
                optionB = "Exactly one line of position",
                optionC = "Only two perpendicular bearings",
                optionD = "Four soundings along the contour",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "A three-bearing fix yields a 'cocked hat' (triangle of error), verifying accuracy and confirming compass error.",
                sourceSheet = "F1 - C1",
                source = "MARINA Reviewer"
            )
        )

        // F1 - C2: Watchkeeping & COLREGS
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C2",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C2"),
                questionNumber = 1,
                questionText = "According to Rule 5 of the COLREGS, every vessel shall at all times maintain a proper look-out by what means?",
                optionA = "By sight and hearing as well as by all available means appropriate in the prevailing circumstances and conditions",
                optionB = "Exclusively by ECDIS radar guard zones",
                optionC = "By visual observation only from the bridge wings during daylight hours",
                optionD = "By maintaining continuous radio listening watch on VHF Channel 16",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Rule 5 mandates look-out by sight and hearing as well as by all available means appropriate to make a full appraisal of the situation.",
                sourceSheet = "F1 - C2",
                source = "PIETRO"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C2",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C2"),
                questionNumber = 2,
                questionText = "Under Rule 15 of COLREGS (Crossing Situation), when two power-driven vessels are crossing so as to involve risk of collision, which vessel shall keep out of the way?",
                optionA = "The vessel which has the other on her own starboard side",
                optionB = "The vessel which is on the port side",
                optionC = "The smaller or slower vessel",
                optionD = "The vessel approaching from abaft the beam",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Rule 15 states that the vessel having the other on her own starboard side is the give-way vessel and shall keep clear.",
                sourceSheet = "F1 - C2",
                source = "MARINA Reviewer"
            )
        )

        // F1 - C3: Radar & ARPA
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C3",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C3"),
                questionNumber = 1,
                questionText = "What is the primary operational purpose of the Trial Manoeuvre function in modern ARPA equipment?",
                optionA = "To simulate the effect of an intended course or speed alteration on all tracked targets prior to execution",
                optionB = "To calibrate radar transmitter frequency tuning",
                optionC = "To test gyrocompass heading alignment with magnetic compass",
                optionD = "To calculate blind sector arcs caused by ship cargo cranes",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Trial Manoeuvre allows the watchkeeper to preview predicted CPAs and TCPAs under planned course/speed changes.",
                sourceSheet = "F1 - C3",
                source = "PIETRO"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C3",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C3"),
                questionNumber = 2,
                questionText = "In ARPA target tracking, what does TCPA stand for?",
                optionA = "Time to Closest Point of Approach",
                optionB = "Target Course Position Assessment",
                optionC = "Tracked Contact Plotting Angle",
                optionD = "True Compass Point Azimuth",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "TCPA is Time to Closest Point of Approach, calculated continuously by ARPA relative vectors.",
                sourceSheet = "F1 - C3",
                source = "TEKNIK"
            )
        )

        // F1 - C4: ECDIS
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C4",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C4"),
                questionNumber = 1,
                questionText = "On an ECDIS display, what is the significance of the Safety Contour setting?",
                optionA = "It distinguishes between safe water and waters where the depth is less than the ship's safety draught",
                optionB = "It outlines traffic separation schemes in magenta",
                optionC = "It establishes the radar range ring spacing",
                optionD = "It indicates areas of magnetic compass variation anomalies",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "The Safety Contour highlights navigable vs non-navigable depths and triggers automatic visual and audible alarms.",
                sourceSheet = "F1 - C4",
                source = "MARINA Reviewer"
            )
        )

        // F1 - C5: Emergencies
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C5",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C5"),
                questionNumber = 1,
                questionText = "What immediate helm action should be taken by the Officer of the Watch in an immediate Man Overboard situation?",
                optionA = "Put the rudder hard over towards the side from which the person fell",
                optionB = "Put the rudder hard over to the opposite side to swing the bow",
                optionC = "Immediately go full astern without touching the helm",
                optionD = "Keep heading steady and release anchor",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Putting the rudder toward the side the person fell swings the stern and lethal propeller away from the person in the water.",
                sourceSheet = "F1 - C5",
                source = "PIETRO"
            )
        )

        // F1 - C7: SMCP & English
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C7",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C7"),
                questionNumber = 1,
                questionText = "Under IMO Standard Marine Communication Phrases (SMCP), which message marker is used when transmitting important nautical warnings such as missing buoys?",
                optionA = "WARNING",
                optionB = "INFORMATION",
                optionC = "INSTRUCTION",
                optionD = "ADVICE",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMO SMCP message marker 'WARNING' is used to announce dangerous situations or navigational hazards.",
                sourceSheet = "F1 - C7",
                source = "MARINA Reviewer"
            )
        )

        // F1 - C9: Ship Manoeuvring
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C9",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C9"),
                questionNumber = 1,
                questionText = "When a ship moves from deep water into shallow water, what hydrodynamic phenomenon typically occurs to the vessel's squat and turning circle diameter?",
                optionA = "Squat increases and turning circle diameter increases",
                optionB = "Squat decreases and turning circle diameter decreases",
                optionC = "Squat remains unchanged and speed increases",
                optionD = "Turning circle diameter is reduced by half",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "In shallow water, Bernoulli venturi under the hull increases squat, and increased water cushion along the hull widens turning diameter.",
                sourceSheet = "F1 - C9",
                source = "TEKNIK"
            )
        )

        // ==========================================
        // OIC-NW: F2 - CARGO HANDLING & STOWAGE
        // ==========================================

        // F2 - C10: Cargo Loading & Care
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F2",
                competencyCode = "C10",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C10"),
                questionNumber = 1,
                questionText = "What is the primary danger when carrying bulk solid cargoes such as nickel ore or iron ore fines that exceed their Transportable Moisture Limit (TML)?",
                optionA = "Liquefaction leading to sudden loss of vessel stability and capsizing",
                optionB = "Spontaneous combustion inside the cargo hold",
                optionC = "Excessive generation of hydrogen gas",
                optionD = "Over-pressurization of cargo hatch pontoon covers",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Under cyclic ship motions, moisture rises creating dynamic slurry liquefaction, causing catastrophic cargo shift.",
                sourceSheet = "F2 - C10",
                source = "Ciriaco"
            )
        )

        // F2 - C11: Cargo spaces & hatch covers
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F2",
                competencyCode = "C11",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C11"),
                questionNumber = 1,
                questionText = "Which modern non-destructive test method is widely recognized by IACS for evaluating the weather-tightness of cargo hold hatch covers?",
                optionA = "Ultrasonic tightness testing",
                optionB = "Hose testing with saltwater only",
                optionC = "Chalk testing under dry atmospheric conditions",
                optionD = "Dye penetrant inspection of all cross joints",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Ultrasonic testing allows pinpoint quantification of seal compression and leakage paths without wetting cargo.",
                sourceSheet = "F2 - C11",
                source = "PIETRO"
            )
        )

        // ==========================================
        // OIC-NW: F3 - CONTROLLING SHIP OPERATION & CREW
        // ==========================================

        // F3 - C12: Pollution Prevention
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C12",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C12"),
                questionNumber = 1,
                questionText = "Under MARPOL Annex I, what is the maximum oil content permitted in bilge water discharged overboard through an approved oily-water separator (OWS)?",
                optionA = "15 parts per million (ppm)",
                optionB = "50 parts per million (ppm)",
                optionC = "100 parts per million (ppm)",
                optionD = "0 parts per million (zero discharge always)",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "MARPOL Annex I Regulation 14 restricts overboard machinery space bilge effluent to an oil content not exceeding 15 ppm.",
                sourceSheet = "F3 - C12",
                source = "MARINA Reviewer"
            )
        )

        // F3 - C13: Seaworthiness & Ship Stability
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C13",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C13"),
                questionNumber = 1,
                questionText = "What effect does a slack tank (liquid with free surface) have upon the initial metacentric height (GM) of a vessel?",
                optionA = "It causes a virtual loss of GM by virtually raising the ship's center of gravity (G)",
                optionB = "It lowers the center of buoyancy and increases transverse GM",
                optionC = "It increases righting levers at angles of heel greater than 10 degrees",
                optionD = "It has zero effect as long as the tank is located on the centerline",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Free surface moment transfers liquid toward the heel, creating an apparent virtual rise of G (GG1 = i * d / V), reducing GM.",
                sourceSheet = "F3 - C13",
                source = "PIETRO"
            )
        )

        // F3 - C17: Maritime Legislation
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C17",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C17"),
                questionNumber = 1,
                questionText = "Under the Maritime Labour Convention (MLC, 2006), what is the maximum hours of work permitted in any 24-hour period for seafarers?",
                optionA = "14 hours",
                optionB = "10 hours",
                optionC = "18 hours",
                optionD = "12 hours",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "MLC 2006 Regulation 2.3 mandates maximum hours of work shall not exceed 14 hours in any 24-hour period, or 72 hours in any 7-day period.",
                sourceSheet = "F3 - C17",
                source = "MARINA Reviewer"
            )
        )

        // ==========================================
        // GMDSS: C1 & C2
        // ==========================================

        // GMDSS C1 - Part 01: Subsystems & Functional Requirements
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 1,
                questionNumber = 1,
                questionText = "Which dedicated frequency is designated for VHF Digital Selective Calling (DSC) distress and safety alerting?",
                questionType = "Multiple Choice",
                optionA = "VHF Channel 70 (156.525 MHz)",
                optionB = "VHF Channel 16 (156.800 MHz)",
                optionC = "VHF Channel 06 (156.300 MHz)",
                optionD = "VHF Channel 13 (156.650 MHz)",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "VHF Channel 70 is used exclusively for Digital Selective Calling (DSC) for distress, urgency, safety, and routine calls.",
                sourceSheet = "C01 - Part 01",
                section = "DSC Alerting"
            )
        )
        // Dynamic options test: 5 choices
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 1,
                questionNumber = 2,
                questionText = "What is the primary international frequency used worldwide for the broadcast of English NAVTEX maritime safety information?",
                questionType = "Multiple Choice",
                optionA = "518 kHz",
                optionB = "490 kHz",
                optionC = "4209.5 kHz",
                optionD = "2182 kHz",
                optionE = "2187.5 kHz",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "518 kHz is the international NAVTEX frequency transmitted exclusively in the English language.",
                sourceSheet = "C01 - Part 01",
                section = "NAVTEX"
            )
        )
        // Dynamic options test: 6 choices!
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 2,
                questionNumber = 3,
                questionText = "Under GMDSS definitions, which Sea Area is defined as an area, excluding sea area A1, within the radiotelephone coverage of at least one MF coast station providing continuous DSC alerting?",
                questionType = "Multiple Choice",
                optionA = "Sea Area A2",
                optionB = "Sea Area A1",
                optionC = "Sea Area A3",
                optionD = "Sea Area A4",
                optionE = "Inland Waterways",
                optionF = "High Seas Ocean Corridor",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Sea Area A2 is the coverage of MF coast stations (approx 100-150 nautical miles), excluding Sea Area A1.",
                sourceSheet = "C01 - Part 02",
                section = "GMDSS Sea Areas"
            )
        )
        // GMDSS C1 - Part 03: EPIRB & SART
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 3,
                questionNumber = 4,
                questionText = "What frequency band does a Search and Rescue Transponder (SART) operate on to create distinctive blips on a ship's radar display?",
                questionType = "Multiple Choice",
                optionA = "9 GHz (3 cm X-band radar)",
                optionB = "3 GHz (10 cm S-band radar)",
                optionC = "406 MHz satellite band",
                optionD = "121.5 MHz aviation band",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "A radar-SART operates in the 9 GHz band and triggers a series of 12 dots on standard X-band radar displays.",
                sourceSheet = "C01 - Part 03",
                section = "Search and Rescue"
            )
        )

        // GMDSS C2 - Part 01: Emergencies & Distress Traffic
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C2",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C2"),
                partNumber = 1,
                questionNumber = 1,
                questionText = "What should a radio operator do immediately if an inadvertent false distress alert is accidentally transmitted on VHF Channel 70 DSC?",
                questionType = "Multiple Choice",
                optionA = "Immediately switch to VHF Channel 16 and broadcast a cancellation message to 'All Stations'",
                optionB = "Turn off the radio power switch and keep it off for 24 hours",
                optionC = "Immediately send a DSC Distress Relay to cancel the alert",
                optionD = "Send an urgency message on Channel 13 only",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMO guidelines require immediately switching to the corresponding distress telephony channel (Ch 16 for VHF) and broadcasting a cancellation to All Stations.",
                sourceSheet = "C02 - Part 01",
                section = "False Alert Procedures"
            )
        )
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C2",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C2"),
                partNumber = 2,
                questionNumber = 2,
                questionText = "Which priority signal indicates a message concerning the safety of a ship, aircraft, or person, but does NOT require immediate assistance for grave and imminent danger?",
                questionType = "Multiple Choice",
                optionA = "PAN PAN (Urgency)",
                optionB = "MAYDAY (Distress)",
                optionC = "SECURITE (Safety)",
                optionD = "ROUTINE",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "PAN PAN is the international spoken urgency signal for urgent communications not amounting to grave and imminent danger.",
                sourceSheet = "C02 - Part 02",
                section = "Distress Urgency Safety"
            )
        )

        // GMDSS C1 - Part 04: EPIRBs
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 4,
                questionNumber = 1,
                questionText = "At what approximate water depth does an approved hydrostatic release unit (HRU) automatically free a satellite EPIRB from its bracket?",
                optionA = "Between 1.5 and 4.0 meters depth",
                optionB = "At exactly 10.0 meters depth",
                optionC = "Immediately upon any immersion over 0.1 meter",
                optionD = "Between 6.0 and 8.0 meters depth",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "SOLAS requirements specify that HRUs must activate automatically at a water depth of not more than 4 meters, typically between 1.5 and 4.0 meters.",
                sourceSheet = "C01 - Part 04",
                section = "EPIRB"
            )
        )

        // GMDSS C1 - Part 05: SART Operations
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 5,
                questionNumber = 1,
                questionText = "What is the minimum operating battery life required for a GMDSS Search and Rescue Radar Transponder (SART)?",
                optionA = "96 hours in standby state followed by 8 hours of active interrogation transmission",
                optionB = "48 hours continuous transmission only",
                optionC = "24 hours standby and 1 hour transmission",
                optionD = "7 days standby with no active transmission",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMO performance standards mandate that a SART battery must provide at least 96 hours of standby monitoring followed by 8 hours of continuous response when interrogated by a 9 GHz radar.",
                sourceSheet = "C01 - Part 05",
                section = "SART"
            )
        )

        // GMDSS C1 - Part 06: NAVTEX Message Selection
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 6,
                questionNumber = 1,
                questionText = "Which NAVTEX message subject indicator characters (B2) CANNOT be rejected or deselected by the radio operator on an approved receiver?",
                optionA = "A (Navigational warnings), B (Meteorological warnings), D (SAR information), and L (Additional navigational warnings)",
                optionB = "Only D (SAR information)",
                optionC = "C (Ice reports) and E (Meteorological forecasts)",
                optionD = "F (Pilot service) and G (AIS messages)",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Under IMO standards, NAVTEX receivers must not allow rejection of message types A, B, D, and L because of their critical safety importance.",
                sourceSheet = "C01 - Part 06",
                section = "NAVTEX"
            )
        )

        // GMDSS C1 - Part 07: Power Sources & Battery Maintenance
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 7,
                questionNumber = 1,
                questionText = "If a vessel's emergency electrical power supply does not comply fully with all SOLAS requirements, how many hours of GMDSS radio operation must the reserve source of energy (batteries) supply?",
                optionA = "At least 6 hours",
                optionB = "At least 1 hour",
                optionC = "At least 12 hours",
                optionD = "At least 24 hours",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "SOLAS Chapter IV requires a reserve source of energy for 1 hour if the emergency generator conforms to SOLAS, or at least 6 hours if it does not.",
                sourceSheet = "C01 - Part 07",
                section = "Power Supplies"
            )
        )

        // GMDSS C1 - Part 08: Antenna Systems
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 8,
                questionNumber = 1,
                questionText = "Why must MF/HF transmitting antenna lead-in insulators be kept thoroughly clean and free of salt encrustation on a seagoing ship?",
                optionA = "To prevent high-voltage RF flashover and severe transmission power loss to ground during high-power broadcasts",
                optionB = "To ensure proper reception of GPS satellite signals",
                optionC = "To avoid static interference on the magnetic compass",
                optionD = "To decrease the antenna's physical resonance length",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Salt spray on antenna insulators creates a conductive path causing high-voltage RF arcing to the ship's steel hull, severely attenuating transmitted radio power.",
                sourceSheet = "C01 - Part 08",
                section = "Antenna Maintenance"
            )
        )

        // GMDSS C1 - Part 09: GMDSS Sea Areas A3 & A4
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 9,
                questionNumber = 1,
                questionText = "What defines GMDSS Sea Area A4?",
                optionA = "An area outside sea areas A1, A2, and A3 (generally polar regions above approximately 70 degrees North and South latitude)",
                optionB = "The Atlantic Ocean region covered by geostationary satellites",
                optionC = "Within 20 nautical miles of the coastline",
                optionD = "Any area navigated exclusively under coastal pilotage",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Sea Area A4 comprises the polar areas beyond geostationary Inmarsat satellite footprint (above roughly 70° N/S), requiring HF radio communications with DSC.",
                sourceSheet = "C01 - Part 09",
                section = "Sea Areas"
            )
        )

        // GMDSS C1 - Part 10: Radio Logbook Procedures
        seedQuestions.add(
            Question(
                reviewer = "GMDSS",
                function = null,
                competencyCode = "C1",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("GMDSS", "C1"),
                partNumber = 10,
                questionNumber = 1,
                questionText = "How long must the GMDSS Radio Logbook be retained on board and made available for inspection by authorized maritime authorities?",
                optionA = "At least 3 years or as directed by the Administration",
                optionB = "Only until the end of the current voyage",
                optionC = "Exactly 6 months after the last entry",
                optionD = "10 years in all circumstances",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "ITU Radio Regulations and Administration rules require GMDSS radio logs to be kept on board for at least 3 years or longer for official legal inquiries.",
                sourceSheet = "C01 - Part 10",
                section = "Radio Log"
            )
        )

        // OIC-NW F1 - C2: COLREGS Rule 19 in Restricted Visibility
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C2",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C2"),
                questionNumber = 3,
                questionText = "Under COLREGS Rule 19, when a vessel detects another vessel forward of the beam by radar alone in restricted visibility and a close-quarters situation is developing, what helm action should be avoided so far as possible?",
                optionA = "An alteration of course to port for a vessel forward of the beam, other than for a vessel being overtaken",
                optionB = "An alteration of course to starboard",
                optionC = "Reducing vessel speed to bare steerageway",
                optionD = "Stopping engines immediately",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Rule 19(d)(i) explicitly mandates avoiding an alteration of course to port for a vessel forward of the beam (other than for a vessel being overtaken).",
                sourceSheet = "F1 - C2",
                source = "COLREGS"
            )
        )

        // OIC-NW F1 - C3: Radar Blind Sectors & False Echoes
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C3",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C3"),
                questionNumber = 3,
                questionText = "What causes an 'indirect reflection' false echo on a marine radar PPI screen?",
                optionA = "Radar pulses reflected off a large structure on the ship (such as a mast, funnel, or crane) to a target and returning via the same path",
                optionB = "Sea surface waves during gale-force winds",
                optionC = "Atmospheric ducting caused by temperature inversions",
                optionD = "Interference from a nearby vessel operating on the exact same frequency",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Indirect reflections occur when the radar beam bounces off the vessel's own superstructure before striking a target, displaying a false contact along the obstruction bearing.",
                sourceSheet = "F1 - C3",
                source = "TEKNIK"
            )
        )

        // OIC-NW F1 - C4: ECDIS Lookahead & Safety Frame
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C4",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C4"),
                questionNumber = 2,
                questionText = "In an ECDIS system, what is the primary operational function of the Lookahead Vector (Safety Frame)?",
                optionA = "To continuously project a safety corridor ahead of the ship and trigger automatic alarms prior to entering shallow waters or crossing danger areas",
                optionB = "To calculate fuel consumption based on engine RPM",
                optionC = "To automatically correct gyrocompass errors using GPS headings",
                optionD = "To steer the autopilot without officer supervision",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "The ECDIS Safety Frame/Lookahead scans ahead along the ship's projected track to warn the OOW before entering dangerous depths or crossing restricted zones.",
                sourceSheet = "F1 - C4",
                source = "ECDIS Manual"
            )
        )

        // OIC-NW F1 - C5: Emergency Steering Gear Failure
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C5",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C5"),
                questionNumber = 2,
                questionText = "Upon a sudden complete failure of steering control on the navigation bridge, what is the immediate first action required of the Officer of the Watch?",
                optionA = "Engage the alternative steering power unit/system, notify the Master, display 'Not Under Command' signals, and inform engine room",
                optionB = "Immediately drop both bow anchors while making 15 knots",
                optionC = "Leave the bridge unattended to inspect the steering gear flat",
                optionD = "Sound 3 short blasts on the whistle and maintain full speed",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "The OOW must immediately switch to secondary steering/power units, inform the Master and Engine room, and display NUC shapes/lights to alert nearby traffic.",
                sourceSheet = "F1 - C5",
                source = "Bridge Procedures Guide"
            )
        )

        // OIC-NW F1 - C7: IMO Standard Marine Communication Phrases
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C7",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C7"),
                questionNumber = 2,
                questionText = "According to IMO SMCP, how should an Officer of the Watch indicate that a message was received and understood?",
                optionA = "ROGER, UNDERSTOOD",
                optionB = "COPIED THAT 10-4",
                optionC = "AFFIRMATIVE YES",
                optionD = "MESSAGE RECEIVED OVER AND OUT",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Under IMO SMCP procedure, 'ROGER' indicates that a transmission has been received, and 'UNDERSTOOD' confirms complete comprehension.",
                sourceSheet = "F1 - C7",
                source = "IMO SMCP"
            )
        )

        // OIC-NW F1 - C9: Pivoting Point in Ship Handling
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F1",
                competencyCode = "C9",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C9"),
                questionNumber = 2,
                questionText = "Where is the pivoting point of a vessel located when she is making steady headway through the water and the rudder is applied?",
                optionA = "Approximately 1/4 to 1/3 of the ship's length from the bow",
                optionB = "Directly at the stern post",
                optionC = "Directly at the midships section",
                optionD = "Behind the rudder stock",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "When a ship makes headway, the hydrodynamic pressure moves the pivoting point forward to roughly 1/4 to 1/3 of the vessel's length from the stem.",
                sourceSheet = "F1 - C9",
                source = "Ship Handling"
            )
        )

        // OIC-NW F2 - C10: IMDG Code Dangerous Goods Classes
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F2",
                competencyCode = "C10",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C10"),
                questionNumber = 2,
                questionText = "Under the International Maritime Dangerous Goods (IMDG) Code, which class designates 'Flammable Liquids'?",
                optionA = "Class 3",
                optionB = "Class 1",
                optionC = "Class 7",
                optionD = "Class 8",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMDG Code Class 3 covers flammable liquids having a flashpoint of 60°C or below (closed-cup test).",
                sourceSheet = "F2 - C10",
                source = "IMDG Code"
            )
        )

        // OIC-NW F2 - C11: Cargo Spaces & Ballast Tank Inspection
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F2",
                competencyCode = "C11",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C11"),
                questionNumber = 2,
                questionText = "Prior to entering an enclosed cargo hold or ballast tank for structural inspection, what must be verified by the designated safety officer?",
                optionA = "Atmospheric oxygen content is 20.9% by volume, hydrocarbon gases are below 1% LFL, and toxic gases are below permissible threshold limits",
                optionB = "The bilge pump is running at maximum discharge capacity",
                optionC = "The hatch covers are kept completely closed to maintain temperature",
                optionD = "The deck water seal is filled with kerosene",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "IMO Resolution A.1050(27) mandates enclosed space testing for 20.9% oxygen, zero toxic gases, and flammable vapor < 1% LFL before any entry permit is issued.",
                sourceSheet = "F2 - C11",
                source = "Safety Management"
            )
        )

        // OIC-NW F3 - C12: MARPOL Annex V Garbage Disposal Rules
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C12",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C12"),
                questionNumber = 2,
                questionText = "Under MARPOL Annex V (Garbage Regulations), what is the minimum distance from nearest land for discharging comminuted (ground) food wastes capable of passing through a 25 mm screen outside special areas?",
                optionA = "Not less than 3 nautical miles from the nearest land while the ship is en route",
                optionB = "Not less than 12 nautical miles from nearest land",
                optionC = "Any distance provided the vessel is anchored",
                optionD = "Zero distance inside territorial waters",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Under MARPOL Annex V, comminuted food waste passing through a 25 mm screen may be discharged while en route at a distance not less than 3 NM from nearest land.",
                sourceSheet = "F3 - C12",
                source = "MARPOL Annex V"
            )
        )

        // OIC-NW F3 - C13: Angle of Loll in Ship Stability
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C13",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C13"),
                questionNumber = 2,
                questionText = "What is the defining characteristic of an 'Angle of Loll' in ship stability, and how should it be corrected?",
                optionA = "The vessel flops from side to side due to an initial negative GM; it is corrected by lowering weights or ballasting the low-side double bottom tank first",
                optionB = "The ship lists permanently due to off-center cargo weights only",
                optionC = "The vessel rolls heavily in synchronous beam seas with positive GM",
                optionD = "The ship trims excessively by the stern when entering fresh water",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "Angle of loll occurs when GM is initial negative. When ballasting to correct loll, always fill the tank on the low side first to prevent the vessel from rolling violently across to the other side.",
                sourceSheet = "F3 - C13",
                source = "Ship Stability"
            )
        )

        // OIC-NW F3 - C17: STCW Rest Hours Regulations
        seedQuestions.add(
            Question(
                reviewer = "OIC-NW",
                function = "F3",
                competencyCode = "C17",
                competencyDescription = CompetencyMetadata.getCompetencyTitle("OIC-NW", "C17"),
                questionNumber = 2,
                questionText = "Under STCW Section A-VIII/1, what are the mandatory minimum hours of rest required for watchkeeping personnel?",
                optionA = "Not less than 10 hours in any 24-hour period, and 77 hours in any 7-day period",
                optionB = "Not less than 8 hours in any 24-hour period, and 56 hours in any 7-day period",
                optionC = "Not less than 12 hours in every 24-hour period",
                optionD = "6 hours continuous rest per day",
                correctAnswerLetter = "A",
                correctAnswerIndex = 0,
                explanation = "STCW 2010 Manila Amendments require at least 10 hours of rest in any 24-hour period and 77 hours in any 7-day period. The 10 hours may be divided into no more than two periods, one of which must be at least 6 hours.",
                sourceSheet = "F3 - C17",
                source = "STCW Code"
            )
        )

        dao.insertQuestions(seedQuestions)
    }
}
