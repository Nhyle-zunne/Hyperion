import { Question, ReviewerTrack } from '../types';
import { getCompetencyTitle } from './competencyMetadata';

export const INITIAL_SEED_QUESTIONS: Omit<Question, 'id'>[] = [
  // ==========================================
  // OIC-NW: F1 - NAVIGATION AT OPERATIONAL LEVEL
  // ==========================================
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C1'),
    questionNumber: 1,
    questionText: 'If there are small cumulus clouds in the morning in summer, what kind of cloud cover would you expect and is the reasonable to forecast later in the day?',
    questionType: 'Multiple Choice',
    optionA: 'CB cloud.',
    optionB: 'Haze',
    optionC: 'Clear skies.',
    optionD: 'ST and drizzle',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'In summer, morning cumulus often develops vertically into cumulonimbus (CB) clouds in the afternoon due to thermal convective currents.',
    point: 1,
    sourceSheet: 'F1 - C1',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C1'),
    questionNumber: 2,
    questionText: 'When planning a passage, what are the four fundamental stages recommended by IMO Resolution A.893(21)?',
    questionType: 'Multiple Choice',
    optionA: 'Appraisal, Planning, Execution, and Monitoring',
    optionB: 'Inspection, Departure, Navigation, and Arrival',
    optionC: 'Briefing, Plotting, Waypoints, and Debriefing',
    optionD: 'Positioning, Tracking, Steering, and Mooring',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'IMO guidelines for voyage planning delineate the 4 stages: Appraisal, Planning, Execution, and Monitoring.',
    point: 1,
    sourceSheet: 'F1 - C1',
    source: 'Ciriaco',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C1'),
    questionNumber: 3,
    questionText: 'A rhumb line appears as which curve on a Mercator chart projection?',
    questionType: 'Multiple Choice',
    optionA: 'A straight line',
    optionB: 'A circle curved towards the equator',
    optionC: 'A curve concave to the equator',
    optionD: 'A spiral line',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'On a Mercator projection, any line of constant compass course (rhumb line or loxodrome) plots as a straight line.',
    point: 1,
    sourceSheet: 'F1 - C1',
    source: 'TEKNIK',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C1'),
    questionNumber: 4,
    questionText: 'In terrestrial navigation, how many simultaneous visual bearing lines are required to obtain a verified position fix with an index of error?',
    questionType: 'Multiple Choice',
    optionA: 'At least three lines of position',
    optionB: 'Exactly one line of position',
    optionC: 'Only two perpendicular bearings',
    optionD: 'Four soundings along the contour',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "A three-bearing fix yields a 'cocked hat' (triangle of error), verifying accuracy and confirming compass error.",
    point: 1,
    sourceSheet: 'F1 - C1',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C2: Watchkeeping & COLREGS
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C2',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C2'),
    questionNumber: 1,
    questionText: 'According to Rule 5 of the COLREGS, every vessel shall at all times maintain a proper look-out by what means?',
    questionType: 'Multiple Choice',
    optionA: 'By sight and hearing as well as by all available means appropriate in the prevailing circumstances and conditions',
    optionB: 'Exclusively by ECDIS radar guard zones',
    optionC: 'By visual observation only from the bridge wings during daylight hours',
    optionD: 'By maintaining continuous radio listening watch on VHF Channel 16',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Rule 5 mandates look-out by sight and hearing as well as by all available means appropriate to make a full appraisal of the situation.',
    point: 1,
    sourceSheet: 'F1 - C2',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C2',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C2'),
    questionNumber: 2,
    questionText: 'Under Rule 15 of COLREGS (Crossing Situation), when two power-driven vessels are crossing so as to involve risk of collision, which vessel shall keep out of the way?',
    questionType: 'Multiple Choice',
    optionA: 'The vessel which has the other on her own starboard side',
    optionB: 'The vessel which is on the port side',
    optionC: 'The smaller or slower vessel',
    optionD: 'The vessel approaching from abaft the beam',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Rule 15 states that the vessel having the other on her own starboard side is the give-way vessel and shall keep clear.',
    point: 1,
    sourceSheet: 'F1 - C2',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C2',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C2'),
    questionNumber: 3,
    questionText: 'Under COLREGS Rule 19, when a vessel detects another vessel forward of the beam by radar alone in restricted visibility and a close-quarters situation is developing, what helm action should be avoided so far as possible?',
    questionType: 'Multiple Choice',
    optionA: 'An alteration of course to port for a vessel forward of the beam, other than for a vessel being overtaken',
    optionB: 'An alteration of course to starboard',
    optionC: 'Reducing vessel speed to bare steerageway',
    optionD: 'Stopping engines immediately',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Rule 19(d)(i) explicitly mandates avoiding an alteration of course to port for a vessel forward of the beam (other than for a vessel being overtaken).',
    point: 1,
    sourceSheet: 'F1 - C2',
    source: 'COLREGS',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C3: Radar & ARPA
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C3',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C3'),
    questionNumber: 1,
    questionText: 'What is the primary operational purpose of the Trial Manoeuvre function in modern ARPA equipment?',
    questionType: 'Multiple Choice',
    optionA: 'To simulate the effect of an intended course or speed alteration on all tracked targets prior to execution',
    optionB: 'To calibrate radar transmitter frequency tuning',
    optionC: 'To test gyrocompass heading alignment with magnetic compass',
    optionD: 'To calculate blind sector arcs caused by ship cargo cranes',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Trial Manoeuvre allows the watchkeeper to preview predicted CPAs and TCPAs under planned course/speed changes.',
    point: 1,
    sourceSheet: 'F1 - C3',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C3',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C3'),
    questionNumber: 2,
    questionText: 'In ARPA target tracking, what does TCPA stand for?',
    questionType: 'Multiple Choice',
    optionA: 'Time to Closest Point of Approach',
    optionB: 'Target Course Position Assessment',
    optionC: 'Tracked Contact Plotting Angle',
    optionD: 'True Compass Point Azimuth',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'TCPA is Time to Closest Point of Approach, calculated continuously by ARPA relative vectors.',
    point: 1,
    sourceSheet: 'F1 - C3',
    source: 'TEKNIK',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C3',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C3'),
    questionNumber: 3,
    questionText: "What causes an 'indirect reflection' false echo on a marine radar PPI screen?",
    questionType: 'Multiple Choice',
    optionA: 'Radar pulses reflected off a large structure on the ship (such as a mast, funnel, or crane) to a target and returning via the same path',
    optionB: 'Sea surface waves during gale-force winds',
    optionC: 'Atmospheric ducting caused by temperature inversions',
    optionD: 'Interference from a nearby vessel operating on the exact same frequency',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "Indirect reflections occur when the radar beam bounces off the vessel's own superstructure before striking a target, displaying a false contact along the obstruction bearing.",
    point: 1,
    sourceSheet: 'F1 - C3',
    source: 'TEKNIK',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C4: ECDIS
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C4',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C4'),
    questionNumber: 1,
    questionText: 'On an ECDIS display, what is the significance of the Safety Contour setting?',
    questionType: 'Multiple Choice',
    optionA: "It distinguishes between safe water and waters where the depth is less than the ship's safety draught",
    optionB: 'It outlines traffic separation schemes in magenta',
    optionC: 'It establishes the radar range ring spacing',
    optionD: 'It indicates areas of magnetic compass variation anomalies',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'The Safety Contour highlights navigable vs non-navigable depths and triggers automatic visual and audible alarms.',
    point: 1,
    sourceSheet: 'F1 - C4',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C4',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C4'),
    questionNumber: 2,
    questionText: 'In an ECDIS system, what is the primary operational function of the Lookahead Vector (Safety Frame)?',
    questionType: 'Multiple Choice',
    optionA: 'To continuously project a safety corridor ahead of the ship and trigger automatic alarms prior to entering shallow waters or crossing danger areas',
    optionB: 'To calculate fuel consumption based on engine RPM',
    optionC: 'To automatically correct gyrocompass errors using GPS headings',
    optionD: 'To steer the autopilot without officer supervision',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "The ECDIS Safety Frame/Lookahead scans ahead along the ship's projected track to warn the OOW before entering dangerous depths or crossing restricted zones.",
    point: 1,
    sourceSheet: 'F1 - C4',
    source: 'ECDIS Manual',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C5: Emergencies
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C5',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C5'),
    questionNumber: 1,
    questionText: 'What immediate helm action should be taken by the Officer of the Watch in an immediate Man Overboard situation?',
    questionType: 'Multiple Choice',
    optionA: 'Put the rudder hard over towards the side from which the person fell',
    optionB: 'Put the rudder hard over to the opposite side to swing the bow',
    optionC: 'Immediately go full astern without touching the helm',
    optionD: 'Keep heading steady and release anchor',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Putting the rudder toward the side the person fell swings the stern and lethal propeller away from the person in the water.',
    point: 1,
    sourceSheet: 'F1 - C5',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C5',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C5'),
    questionNumber: 2,
    questionText: 'Upon a sudden complete failure of steering control on the navigation bridge, what is the immediate first action required of the Officer of the Watch?',
    questionType: 'Multiple Choice',
    optionA: "Engage the alternative steering power unit/system, notify the Master, display 'Not Under Command' signals, and inform engine room",
    optionB: 'Immediately drop both bow anchors while making 15 knots',
    optionC: 'Leave the bridge unattended to inspect the steering gear flat',
    optionD: 'Sound 3 short blasts on the whistle and maintain full speed',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'The OOW must immediately switch to secondary steering/power units, inform the Master and Engine room, and display NUC shapes/lights to alert nearby traffic.',
    point: 1,
    sourceSheet: 'F1 - C5',
    source: 'Bridge Procedures Guide',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C7: SMCP & English
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C7',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C7'),
    questionNumber: 1,
    questionText: 'Under IMO Standard Marine Communication Phrases (SMCP), which message marker is used when transmitting important nautical warnings such as missing buoys?',
    questionType: 'Multiple Choice',
    optionA: 'WARNING',
    optionB: 'INFORMATION',
    optionC: 'INSTRUCTION',
    optionD: 'ADVICE',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "IMO SMCP message marker 'WARNING' is used to announce dangerous situations or navigational hazards.",
    point: 1,
    sourceSheet: 'F1 - C7',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C7',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C7'),
    questionNumber: 2,
    questionText: 'According to IMO SMCP, how should an Officer of the Watch indicate that a message was received and understood?',
    questionType: 'Multiple Choice',
    optionA: 'ROGER, UNDERSTOOD',
    optionB: 'COPIED THAT 10-4',
    optionC: 'AFFIRMATIVE YES',
    optionD: 'MESSAGE RECEIVED OVER AND OUT',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "Under IMO SMCP procedure, 'ROGER' indicates that a transmission has been received, and 'UNDERSTOOD' confirms complete comprehension.",
    point: 1,
    sourceSheet: 'F1 - C7',
    source: 'IMO SMCP',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F1 - C9: Ship Manoeuvring
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C9',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C9'),
    questionNumber: 1,
    questionText: "When a ship moves from deep water into shallow water, what hydrodynamic phenomenon typically occurs to the vessel's squat and turning circle diameter?",
    questionType: 'Multiple Choice',
    optionA: 'Squat increases and turning circle diameter increases',
    optionB: 'Squat decreases and turning circle diameter decreases',
    optionC: 'Squat remains unchanged and speed increases',
    optionD: 'Turning circle diameter is reduced by half',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'In shallow water, Bernoulli venturi under the hull increases squat, and increased water cushion along the hull widens turning diameter.',
    point: 1,
    sourceSheet: 'F1 - C9',
    source: 'TEKNIK',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F1',
    competencyCode: 'C9',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C9'),
    questionNumber: 2,
    questionText: 'Where is the pivoting point of a vessel located when she is making steady headway through the water and the rudder is applied?',
    questionType: 'Multiple Choice',
    optionA: "Approximately 1/4 to 1/3 of the ship's length from the bow",
    optionB: 'Directly at the stern post',
    optionC: 'Directly at the midships section',
    optionD: 'Behind the rudder stock',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: "When a ship makes headway, the hydrodynamic pressure moves the pivoting point forward to roughly 1/4 to 1/3 of the vessel's length from the stem.",
    point: 1,
    sourceSheet: 'F1 - C9',
    source: 'Ship Handling',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F2 - C10: Cargo Loading & Stowage
  {
    reviewer: 'OIC-NW',
    function: 'F2',
    competencyCode: 'C10',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C10'),
    questionNumber: 1,
    questionText: 'What is the primary danger when carrying bulk solid cargoes such as nickel ore or iron ore fines that exceed their Transportable Moisture Limit (TML)?',
    questionType: 'Multiple Choice',
    optionA: 'Liquefaction leading to sudden loss of vessel stability and capsizing',
    optionB: 'Spontaneous combustion inside the cargo hold',
    optionC: 'Excessive generation of hydrogen gas',
    optionD: 'Over-pressurization of cargo hatch pontoon covers',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Under cyclic ship motions, moisture rises creating dynamic slurry liquefaction, causing catastrophic cargo shift.',
    point: 1,
    sourceSheet: 'F2 - C10',
    source: 'Ciriaco',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F2',
    competencyCode: 'C10',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C10'),
    questionNumber: 2,
    questionText: "Under the International Maritime Dangerous Goods (IMDG) Code, which class designates 'Flammable Liquids'?",
    questionType: 'Multiple Choice',
    optionA: 'Class 3',
    optionB: 'Class 1',
    optionC: 'Class 7',
    optionD: 'Class 8',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'IMDG Code Class 3 covers flammable liquids having a flashpoint of 60°C or below (closed-cup test).',
    point: 1,
    sourceSheet: 'F2 - C10',
    source: 'IMDG Code',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F2 - C11: Cargo Spaces & Ballast
  {
    reviewer: 'OIC-NW',
    function: 'F2',
    competencyCode: 'C11',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C11'),
    questionNumber: 1,
    questionText: 'Which modern non-destructive test method is widely recognized by IACS for evaluating the weather-tightness of cargo hold hatch covers?',
    questionType: 'Multiple Choice',
    optionA: 'Ultrasonic tightness testing',
    optionB: 'Hose testing with saltwater only',
    optionC: 'Chalk testing under dry atmospheric conditions',
    optionD: 'Dye penetrant inspection of all cross joints',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Ultrasonic testing allows pinpoint quantification of seal compression and leakage paths without wetting cargo.',
    point: 1,
    sourceSheet: 'F2 - C11',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F2',
    competencyCode: 'C11',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C11'),
    questionNumber: 2,
    questionText: 'Prior to entering an enclosed cargo hold or ballast tank for structural inspection, what must be verified by the designated safety officer?',
    questionType: 'Multiple Choice',
    optionA: 'Atmospheric oxygen content is 20.9% by volume, hydrocarbon gases are below 1% LFL, and toxic gases are below permissible threshold limits',
    optionB: 'The bilge pump is running at maximum discharge capacity',
    optionC: 'The hatch covers are kept completely closed to maintain temperature',
    optionD: 'The deck water seal is filled with kerosene',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'IMO Resolution A.1050(27) mandates enclosed space testing for 20.9% oxygen, zero toxic gases, and flammable vapor < 1% LFL before any entry permit is issued.',
    point: 1,
    sourceSheet: 'F2 - C11',
    source: 'Safety Management',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F3 - C12: Pollution Prevention
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C12',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C12'),
    questionNumber: 1,
    questionText: 'Under MARPOL Annex I, what is the maximum oil content permitted in bilge water discharged overboard through an approved oily-water separator (OWS)?',
    questionType: 'Multiple Choice',
    optionA: '15 parts per million (ppm)',
    optionB: '50 parts per million (ppm)',
    optionC: '100 parts per million (ppm)',
    optionD: '0 parts per million (zero discharge always)',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'MARPOL Annex I Regulation 14 restricts overboard machinery space bilge effluent to an oil content not exceeding 15 ppm.',
    point: 1,
    sourceSheet: 'F3 - C12',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C12',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C12'),
    questionNumber: 2,
    questionText: 'Under MARPOL Annex V (Garbage Regulations), what is the minimum distance from nearest land for discharging comminuted (ground) food wastes capable of passing through a 25 mm screen outside special areas?',
    questionType: 'Multiple Choice',
    optionA: 'Not less than 3 nautical miles from the nearest land while the ship is en route',
    optionB: 'Not less than 12 nautical miles from nearest land',
    optionC: 'Any distance provided the vessel is anchored',
    optionD: 'Zero distance inside territorial waters',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Under MARPOL Annex V, comminuted food waste passing through a 25 mm screen may be discharged while en route at a distance not less than 3 NM from nearest land.',
    point: 1,
    sourceSheet: 'F3 - C12',
    source: 'MARPOL Annex V',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F3 - C13: Seaworthiness & Ship Stability
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C13',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C13'),
    questionNumber: 1,
    questionText: 'What effect does a slack tank (liquid with free surface) have upon the initial metacentric height (GM) of a vessel?',
    questionType: 'Multiple Choice',
    optionA: "It causes a virtual loss of GM by virtually raising the ship's center of gravity (G)",
    optionB: 'It lowers the center of buoyancy and increases transverse GM',
    optionC: 'It increases righting levers at angles of heel greater than 10 degrees',
    optionD: 'It has zero effect as long as the tank is located on the centerline',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Free surface moment transfers liquid toward the heel, creating an apparent virtual rise of G (GG1 = i * d / V), reducing GM.',
    point: 1,
    sourceSheet: 'F3 - C13',
    source: 'PIETRO',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C13',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C13'),
    questionNumber: 2,
    questionText: "What is the defining characteristic of an 'Angle of Loll' in ship stability, and how should it be corrected?",
    questionType: 'Multiple Choice',
    optionA: 'The vessel flops from side to side due to an initial negative GM; it is corrected by lowering weights or ballasting the low-side double bottom tank first',
    optionB: 'The ship lists permanently due to off-center cargo weights only',
    optionC: 'The vessel rolls heavily in synchronous beam seas with positive GM',
    optionD: 'The ship trims excessively by the stern when entering fresh water',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Angle of loll occurs when GM is initial negative. When ballasting to correct loll, always fill the tank on the low side first to prevent the vessel from rolling violently across to the other side.',
    point: 1,
    sourceSheet: 'F3 - C13',
    source: 'Ship Stability',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // F3 - C17: Maritime Legislation
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C17',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C17'),
    questionNumber: 1,
    questionText: 'Under the Maritime Labour Convention (MLC, 2006), what is the maximum hours of work permitted in any 24-hour period for seafarers?',
    questionType: 'Multiple Choice',
    optionA: '14 hours',
    optionB: '10 hours',
    optionC: '18 hours',
    optionD: '12 hours',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'MLC 2006 Regulation 2.3 mandates maximum hours of work shall not exceed 14 hours in any 24-hour period, or 72 hours in any 7-day period.',
    point: 1,
    sourceSheet: 'F3 - C17',
    source: 'MARINA Reviewer',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'OIC-NW',
    function: 'F3',
    competencyCode: 'C17',
    competencyDescription: getCompetencyTitle('OIC-NW', 'C17'),
    questionNumber: 2,
    questionText: 'Under STCW Section A-VIII/1, what are the mandatory minimum hours of rest required for watchkeeping personnel?',
    questionType: 'Multiple Choice',
    optionA: 'Not less than 10 hours in any 24-hour period, and 77 hours in any 7-day period',
    optionB: 'Not less than 8 hours in any 24-hour period, and 56 hours in any 7-day period',
    optionC: 'Not less than 12 hours in every 24-hour period',
    optionD: '6 hours continuous rest per day',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'STCW 2010 Manila Amendments require at least 10 hours of rest in any 24-hour period and 77 hours in any 7-day period.',
    point: 1,
    sourceSheet: 'F3 - C17',
    source: 'STCW Code',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },

  // ==========================================
  // GMDSS: C1 & C2
  // ==========================================
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('GMDSS', 'C1'),
    partNumber: 1,
    questionNumber: 1,
    questionText: 'Which dedicated frequency is designated for VHF Digital Selective Calling (DSC) distress and safety alerting?',
    questionType: 'Multiple Choice',
    optionA: 'VHF Channel 70 (156.525 MHz)',
    optionB: 'VHF Channel 16 (156.800 MHz)',
    optionC: 'VHF Channel 06 (156.300 MHz)',
    optionD: 'VHF Channel 13 (156.650 MHz)',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'VHF Channel 70 is used exclusively for Digital Selective Calling (DSC) for distress, urgency, safety, and routine calls.',
    point: 1,
    sourceSheet: 'C01 - Part 01',
    section: 'DSC Alerting',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('GMDSS', 'C1'),
    partNumber: 1,
    questionNumber: 2,
    questionText: 'What is the primary international frequency used worldwide for the broadcast of English NAVTEX maritime safety information?',
    questionType: 'Multiple Choice',
    optionA: '518 kHz',
    optionB: '490 kHz',
    optionC: '4209.5 kHz',
    optionD: '2182 kHz',
    optionE: '2187.5 kHz',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: '518 kHz is the international NAVTEX frequency transmitted exclusively in the English language.',
    point: 1,
    sourceSheet: 'C01 - Part 01',
    section: 'NAVTEX',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('GMDSS', 'C1'),
    partNumber: 2,
    questionNumber: 3,
    questionText: 'Under GMDSS definitions, which Sea Area is defined as an area, excluding sea area A1, within the radiotelephone coverage of at least one MF coast station providing continuous DSC alerting?',
    questionType: 'Multiple Choice',
    optionA: 'Sea Area A2',
    optionB: 'Sea Area A1',
    optionC: 'Sea Area A3',
    optionD: 'Sea Area A4',
    optionE: 'Inland Waterways',
    optionF: 'High Seas Ocean Corridor',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'Sea Area A2 is the coverage of MF coast stations (approx 100-150 nautical miles), excluding Sea Area A1.',
    point: 1,
    sourceSheet: 'C01 - Part 02',
    section: 'GMDSS Sea Areas',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C1',
    competencyDescription: getCompetencyTitle('GMDSS', 'C1'),
    partNumber: 3,
    questionNumber: 4,
    questionText: "What frequency band does a Search and Rescue Transponder (SART) operate on to create distinctive blips on a ship's radar display?",
    questionType: 'Multiple Choice',
    optionA: '9 GHz (3 cm X-band radar)',
    optionB: '3 GHz (10 cm S-band radar)',
    optionC: '406 MHz satellite band',
    optionD: '121.5 MHz aviation band',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'A radar-SART operates in the 9 GHz band and triggers a series of 12 dots on standard X-band radar displays.',
    point: 1,
    sourceSheet: 'C01 - Part 03',
    section: 'Search and Rescue',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C2',
    competencyDescription: getCompetencyTitle('GMDSS', 'C2'),
    partNumber: 1,
    questionNumber: 1,
    questionText: 'What should a radio operator do immediately if an inadvertent false distress alert is accidentally transmitted on VHF Channel 70 DSC?',
    questionType: 'Multiple Choice',
    optionA: "Immediately switch to VHF Channel 16 and broadcast a cancellation message to 'All Stations'",
    optionB: 'Turn off the radio power switch and keep it off for 24 hours',
    optionC: 'Immediately send a DSC Distress Relay to cancel the alert',
    optionD: 'Send an urgency message on Channel 13 only',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'IMO guidelines require immediately switching to the corresponding distress telephony channel (Ch 16 for VHF) and broadcasting a cancellation to All Stations.',
    point: 1,
    sourceSheet: 'C02 - Part 01',
    section: 'False Alert Procedures',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    reviewer: 'GMDSS',
    function: null,
    competencyCode: 'C2',
    competencyDescription: getCompetencyTitle('GMDSS', 'C2'),
    partNumber: 2,
    questionNumber: 2,
    questionText: 'Which priority signal indicates a message concerning the safety of a ship, aircraft, or person, but does NOT require immediate assistance for grave and imminent danger?',
    questionType: 'Multiple Choice',
    optionA: 'PAN PAN (Urgency)',
    optionB: 'MAYDAY (Distress)',
    optionC: 'SECURITE (Safety)',
    optionD: 'ROUTINE',
    correctAnswerLetter: 'A',
    correctAnswerIndex: 0,
    explanation: 'PAN PAN is the international spoken urgency signal for urgent communications not amounting to grave and imminent danger.',
    point: 1,
    sourceSheet: 'C02 - Part 02',
    section: 'Distress Urgency Safety',
    isFavorite: false,
    isFlagged: false,
    masteryStatus: 'NOT_ATTEMPTED',
    consecutiveCorrect: 0,
    timesAttempted: 0,
    timesCorrect: 0,
    timesIncorrect: 0,
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
];

/**
 * Standard RFC 4180 CSV parser matching Kotlin's CsvImporter
 */
export function parseCsvRows(csvText: string): string[][] {
  const rows: string[][] = [];
  let currentRow: string[] = [];
  let currentField = '';
  let inQuotes = false;
  let i = 0;

  while (i < csvText.length) {
    const c = csvText[i];

    if (inQuotes) {
      if (c === '"') {
        if (i + 1 < csvText.length && csvText[i + 1] === '"') {
          currentField += '"';
          i += 2;
          continue;
        } else {
          inQuotes = false;
          i++;
          continue;
        }
      } else {
        currentField += c;
        i++;
        continue;
      }
    } else {
      if (c === '"') {
        inQuotes = true;
        i++;
        continue;
      } else if (c === ',') {
        currentRow.push(currentField.trim());
        currentField = '';
        i++;
        continue;
      } else if (c === '\r') {
        if (i + 1 < csvText.length && csvText[i + 1] === '\n') {
          i++;
        }
        currentRow.push(currentField.trim());
        currentField = '';
        rows.push(currentRow);
        currentRow = [];
        i++;
        continue;
      } else if (c === '\n') {
        currentRow.push(currentField.trim());
        currentField = '';
        rows.push(currentRow);
        currentRow = [];
        i++;
        continue;
      } else {
        currentField += c;
        i++;
        continue;
      }
    }
  }

  if (currentField.length > 0 || currentRow.length > 0) {
    currentRow.push(currentField.trim());
    rows.push(currentRow);
  }

  return rows;
}

export function parseGmdssTitle(title: string): { competency: string; part: number | null } {
  const compMatch = /C([0-9]+)/i.exec(title);
  const comp = compMatch ? `C${parseInt(compMatch[1], 10)}` : 'C1';

  const partMatch = /Part\s*([0-9]+)/i.exec(title);
  const part = partMatch ? parseInt(partMatch[1], 10) : null;

  return { competency: comp, part };
}

export function processCsvContent(
  csvText: string,
  forcedReviewer: ReviewerTrack = 'GMDSS',
  existingQuestionTexts: Set<string> = new Set()
): {
  reviewer: ReviewerTrack;
  validCount: number;
  duplicateCount: number;
  totalParsed: number;
  errors: string[];
  warnings: string[];
  validatedQuestions: Omit<Question, 'id'>[];
} {
  const rows = parseCsvRows(csvText);
  const validQuestions: Omit<Question, 'id'>[] = [];
  const warnings: string[] = [];
  const errors: string[] = [];
  let duplicates = 0;
  let totalParsed = 0;
  const seenInBatch = new Set<string>();

  let currentTitle = 'GMDSS Reviewer';
  let currentCompCode = 'C1';
  let currentPart: number | null = null;

  for (let rowIndex = 0; rowIndex < rows.length; rowIndex++) {
    const row = rows[rowIndex];
    if (row.length === 0 || row.every((c) => c.trim().length === 0)) continue;

    const firstCol = row[0]?.trim() ?? '';

    // Directives
    if (firstCol.toLowerCase() === 'title') {
      currentTitle = row[1]?.trim() ?? 'GMDSS Reviewer';
      const parsed = parseGmdssTitle(currentTitle);
      currentCompCode = parsed.competency;
      currentPart = parsed.part;
      continue;
    }

    if (firstCol.toLowerCase() === 'description' || firstCol.toLowerCase() === 'duration') {
      continue;
    }

    if (
      firstCol.toLowerCase() === 'question' &&
      row.some((cell) => cell.toLowerCase().includes('option') || cell.toLowerCase().includes('type'))
    ) {
      continue;
    }

    totalParsed++;

    const qText = row[0]?.trim() ?? '';
    const qType = row[1]?.trim() || 'Multiple Choice';
    const opt1 = row[2]?.trim() ?? '';
    const opt2 = row[3]?.trim() ?? '';
    const opt3 = row[4]?.trim() ?? '';
    const opt4 = row[5]?.trim() ?? '';
    const opt5 = row[6]?.trim() ?? '';
    const opt6 = row[7]?.trim() ?? '';
    const explanation = row[8]?.trim() || null;
    const rawAnswer = row[9]?.trim() ?? '';
    const point = parseInt(row[10]?.trim() || '1', 10) || 1;
    const section = row[11]?.trim() || null;

    if (!qText) {
      errors.push(`Row ${rowIndex + 1}: Empty question text.`);
      continue;
    }

    // Determine correct answer
    const numericAns = parseInt(rawAnswer, 10);
    let correctIdx = -1;
    let correctLetter = '';

    if (!isNaN(numericAns) && numericAns >= 1 && numericAns <= 6) {
      correctIdx = numericAns - 1;
      correctLetter = String.fromCharCode('A'.charCodeAt(0) + correctIdx);
    } else {
      const upper = rawAnswer.toUpperCase();
      if (['A', 'B', 'C', 'D', 'E', 'F'].includes(upper)) {
        correctIdx = upper.charCodeAt(0) - 'A'.charCodeAt(0);
        correctLetter = upper;
      }
    }

    if (correctIdx < 0) {
      errors.push(`Row ${rowIndex + 1}: Invalid answer value '${rawAnswer}'.`);
      continue;
    }

    if (!opt1 || !opt2) {
      errors.push(`Row ${rowIndex + 1}: Question must have at least Option 1 and Option 2.`);
      continue;
    }

    const normKey = qText.toLowerCase().replace(/\s+/g, ' ');
    if (seenInBatch.has(normKey) || existingQuestionTexts.has(normKey)) {
      duplicates++;
      warnings.push(`Duplicate skipped: "${qText.substring(0, 40)}..."`);
      continue;
    }
    seenInBatch.add(normKey);

    validQuestions.push({
      reviewer: forcedReviewer,
      function: forcedReviewer === 'OIC-NW' ? 'F1' : null,
      competencyCode: currentCompCode,
      competencyDescription: getCompetencyTitle(forcedReviewer, currentCompCode),
      partNumber: currentPart,
      questionNumber: validQuestions.length + 1,
      questionText: qText,
      questionType: qType,
      optionA: opt1,
      optionB: opt2,
      optionC: opt3 || null,
      optionD: opt4 || null,
      optionE: opt5 || null,
      optionF: opt6 || null,
      correctAnswerLetter: correctLetter,
      correctAnswerIndex: correctIdx,
      explanation: explanation || null,
      point,
      section: section || null,
      sourceSheet: currentTitle,
      source: 'MARINA GMDSS Import',
      isFavorite: false,
      isFlagged: false,
      masteryStatus: 'NOT_ATTEMPTED',
      consecutiveCorrect: 0,
      timesAttempted: 0,
      timesCorrect: 0,
      timesIncorrect: 0,
      createdAt: Date.now(),
      updatedAt: Date.now()
    });
  }

  return {
    reviewer: forcedReviewer,
    validCount: validQuestions.length,
    duplicateCount: duplicates,
    totalParsed,
    errors,
    warnings,
    validatedQuestions: validQuestions
  };
}
