# Hyperion — MARINA Licensure Examination Reviewer

Hyperion is a standalone, offline-first exam reviewer for the Philippine Maritime Industry Authority (MARINA) Licensure Examinations for Filipino seafarers:
- **OIC-NW Track**: Officer in Charge of a Navigational Watch (Functions F1 Navigation, F2 Cargo Handling & Stowage, F3 Ship Operations & Seafarer Care across Competencies C1 through C17).
- **GMDSS Track**: Global Maritime Distress and Safety System (Subsystems & Functional Requirements C1, Radio Services in Emergencies C2).

## Features

- **Dual-Track Reviewer Architecture**: Independent master question banks, mastery ratings, and examination records for OIC-NW and GMDSS tracks.
- **Multiple Study Modes**:
  - **Tutor Mode**: Instant answer validation with comprehensive marine regulatory explanations and references.
  - **Drill Exam**: Uninterrupted question delivery without immediate hints, culminating in a performance scorecard.
  - **Flashcard Mode**: Flip-to-reveal cards with active recall self-assessment ("I Know This" vs "Needs Review").
  - **Speed Run Mode**: Timed rapid-fire challenge testing rapid reflex recall under high-pressure conditions.
- **Official & Custom Mock Exam Engine**:
  - **Official MARINA Simulation**: Strict simulation mirroring regulatory exam timing (180 minutes for OIC-NW, 90 minutes for GMDSS) and quota distributions across competencies with a 70% passing threshold.
  - **Custom Mock Exam**: User-configured competency selection, question volume, and time limits.
  - **Exam Runner & Review**: Color-coded overview grid, flagged question tagging, time tracking, comprehensive scorecards, and answer review with explanations.
- **Ongoing Session Persistence & Recovery**: Resumable practice drills and mock exams with state preservation.
- **Analytics & Competency Mastery Tracking**: Question-level spaced review tracking (Not Attempted, Needs Review, Improving, Mastered), accuracy calculations, and historical exam records.
- **Question Remediation**:
  - **My Incorrect**: Dedicated review queue for all questions answered incorrectly.
  - **Favorites & Flagged**: Bookmarks for high-priority or difficult test items.
  - **Smart Review**: Algorithmic prioritization targeting weak competencies.
- **Offline Search**: Instant full-text search across questions, options, explanations, and competency codes.
- **Study Timer & Daily Goals**: Integrated stopwatch for logging study sessions against daily target hours, question counts, and accuracy goals.
- **Admin Panel (Default PIN: 1234)**:
  - Excel (`.xlsx`, `.xls`) and CSV question bank importer with validation preview.
  - Bundled asset scanner and database reseed utilities.
  - Full `.hyperion` offline JSON backup and restoration.
  - Manual question creator and user issue report viewer.

## Technology Stack

- **Framework**: React 19 + TypeScript + Vite
- **Styling**: Tailwind CSS
- **Icons**: Lucide React
- **Storage**: Client-Side IndexedDB (100% offline, persistent storage)
- **Data Import**: SheetJS (`xlsx`) for Excel workbook processing & RFC 4180 CSV parser
