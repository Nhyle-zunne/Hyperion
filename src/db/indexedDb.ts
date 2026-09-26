import {
  Question,
  ReviewerTrack,
  ExamRecord,
  ExamAnswer,
  StudySession,
  DailyGoal,
  QuestionReport,
  SavedSessionLog,
  CompetencyCount
} from '../types';
import { INITIAL_SEED_QUESTIONS, processCsvContent } from '../data/seedQuestions';

const DB_NAME = 'hyperion_marina_db';
const DB_VERSION = 1;

let dbPromise: Promise<IDBDatabase> | null = null;

export function getDb(): Promise<IDBDatabase> {
  if (dbPromise) return dbPromise;

  dbPromise = new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event) => {
      const db = (event.target as IDBOpenDBRequest).result;

      if (!db.objectStoreNames.contains('questions')) {
        const qStore = db.createObjectStore('questions', { keyPath: 'id', autoIncrement: true });
        qStore.createIndex('reviewer', 'reviewer', { unique: false });
        qStore.createIndex('competencyCode', 'competencyCode', { unique: false });
        qStore.createIndex('reviewer_competency', ['reviewer', 'competencyCode'], { unique: false });
        qStore.createIndex('reviewer_favorite', ['reviewer', 'isFavorite'], { unique: false });
        qStore.createIndex('reviewer_flagged', ['reviewer', 'isFlagged'], { unique: false });
        qStore.createIndex('reviewer_incorrect', ['reviewer', 'timesIncorrect'], { unique: false });
        qStore.createIndex('reviewer_mastery', ['reviewer', 'masteryStatus'], { unique: false });
      }

      if (!db.objectStoreNames.contains('exam_records')) {
        const examStore = db.createObjectStore('exam_records', { keyPath: 'id', autoIncrement: true });
        examStore.createIndex('track', 'track', { unique: false });
        examStore.createIndex('timestamp', 'timestamp', { unique: false });
      }

      if (!db.objectStoreNames.contains('exam_answers')) {
        const ansStore = db.createObjectStore('exam_answers', { keyPath: 'id', autoIncrement: true });
        ansStore.createIndex('examId', 'examId', { unique: false });
      }

      if (!db.objectStoreNames.contains('study_sessions')) {
        const sStore = db.createObjectStore('study_sessions', { keyPath: 'id', autoIncrement: true });
        sStore.createIndex('track', 'track', { unique: false });
      }

      if (!db.objectStoreNames.contains('daily_goals')) {
        db.createObjectStore('daily_goals', { keyPath: 'dateKey' });
      }

      if (!db.objectStoreNames.contains('question_reports')) {
        const repStore = db.createObjectStore('question_reports', { keyPath: 'id', autoIncrement: true });
        repStore.createIndex('track', 'track', { unique: false });
      }

      if (!db.objectStoreNames.contains('saved_session_logs')) {
        const logStore = db.createObjectStore('saved_session_logs', { keyPath: 'id', autoIncrement: true });
        logStore.createIndex('track', 'track', { unique: false });
        logStore.createIndex('sessionType', 'sessionType', { unique: false });
      }
    };

    request.onsuccess = () => {
      resolve(request.result);
    };

    request.onerror = () => {
      reject(request.error);
    };
  });

  return dbPromise;
}

// ==========================================
// SEEDING AND ASSETS
// ==========================================
export async function seedInitialDataIfNeeded(): Promise<number> {
  const db = await getDb();

  const totalQuestions = await new Promise<number>((resolve) => {
    const tx = db.transaction('questions', 'readonly');
    const store = tx.objectStore('questions');
    const countReq = store.count();
    countReq.onsuccess = () => resolve(countReq.result);
    countReq.onerror = () => resolve(0);
  });

  if (totalQuestions > 50) {
    return totalQuestions;
  }

  // 1. Insert built-in seed questions
  const tx = db.transaction('questions', 'readwrite');
  const store = tx.objectStore('questions');
  for (const q of INITIAL_SEED_QUESTIONS) {
    store.add(q);
  }
  await new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });

  // 2. Fetch and import CSV files from /data/
  let importedFromAssets = 0;
  try {
    const csvFiles = ['/data/gmdss_reviewer.csv', '/data/tmp_chunk.csv'];
    for (const url of csvFiles) {
      try {
        const res = await fetch(url);
        if (res.ok) {
          const csvText = await res.text();
          const parsed = processCsvContent(csvText, 'GMDSS');
          if (parsed.validatedQuestions.length > 0) {
            await insertBatch(parsed.validatedQuestions);
            importedFromAssets += parsed.validCount;
          }
        }
      } catch (err) {
        console.warn(`Could not load CSV from ${url}:`, err);
      }
    }
  } catch (e) {
    console.error('Error during asset CSV scan:', e);
  }

  return INITIAL_SEED_QUESTIONS.length + importedFromAssets;
}

// ==========================================
// QUESTION QUERIES
// ==========================================
export async function getAllQuestions(track: ReviewerTrack): Promise<Question[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readonly');
    const store = tx.objectStore('questions');
    const index = store.index('reviewer');
    const req = index.getAll(IDBKeyRange.only(track));
    req.onsuccess = () => resolve(req.result || []);
    req.onerror = () => reject(req.error);
  });
}

export async function getQuestionById(id: number): Promise<Question | null> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readonly');
    const store = tx.objectStore('questions');
    const req = store.get(id);
    req.onsuccess = () => resolve(req.result || null);
    req.onerror = () => reject(req.error);
  });
}

export async function getQuestionsByIds(ids: number[]): Promise<Question[]> {
  const db = await getDb();
  return new Promise((resolve) => {
    const tx = db.transaction('questions', 'readonly');
    const store = tx.objectStore('questions');
    const results: Question[] = [];
    let completed = 0;

    if (ids.length === 0) {
      resolve([]);
      return;
    }

    ids.forEach((id) => {
      const req = store.get(id);
      req.onsuccess = () => {
        if (req.result) results.push(req.result);
        completed++;
        if (completed === ids.length) {
          // Keep original order of ids
          const map = new Map(results.map((q) => [q.id, q]));
          const ordered = ids.map((id) => map.get(id)).filter(Boolean) as Question[];
          resolve(ordered);
        }
      };
      req.onerror = () => {
        completed++;
        if (completed === ids.length) {
          resolve(results);
        }
      };
    });
  });
}

export async function getFavorites(track: ReviewerTrack): Promise<Question[]> {
  const list = await getAllQuestions(track);
  return list.filter((q) => q.isFavorite);
}

export async function getFlagged(track: ReviewerTrack): Promise<Question[]> {
  const list = await getAllQuestions(track);
  return list.filter((q) => q.isFlagged);
}

export async function getIncorrect(track: ReviewerTrack): Promise<Question[]> {
  const list = await getAllQuestions(track);
  return list.filter((q) => q.timesIncorrect > 0);
}

export async function getCompetencyStats(track: ReviewerTrack): Promise<CompetencyCount[]> {
  const all = await getAllQuestions(track);
  const map = new Map<string, { total: number; mastered: number; attempted: number; correct: number }>();

  for (const q of all) {
    const code = q.competencyCode;
    let entry = map.get(code);
    if (!entry) {
      entry = { total: 0, mastered: 0, attempted: 0, correct: 0 };
      map.set(code, entry);
    }
    entry.total++;
    if (q.masteryStatus === 'MASTERED') entry.mastered++;
    if (q.timesAttempted > 0) entry.attempted++;
    entry.correct += q.timesCorrect;
  }

  return Array.from(map.entries()).map(([competencyCode, data]) => ({
    competencyCode,
    ...data
  }));
}

export async function toggleFavorite(questionId: number): Promise<boolean> {
  const db = await getDb();
  const q = await getQuestionById(questionId);
  if (!q) return false;

  q.isFavorite = !q.isFavorite;
  q.updatedAt = Date.now();

  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.put(q);
    req.onsuccess = () => resolve(q.isFavorite);
    req.onerror = () => reject(req.error);
  });
}

export async function toggleFlag(questionId: number): Promise<boolean> {
  const db = await getDb();
  const q = await getQuestionById(questionId);
  if (!q) return false;

  q.isFlagged = !q.isFlagged;
  q.updatedAt = Date.now();

  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.put(q);
    req.onsuccess = () => resolve(q.isFlagged);
    req.onerror = () => reject(req.error);
  });
}

export async function removeFromIncorrect(questionId: number): Promise<void> {
  const db = await getDb();
  const q = await getQuestionById(questionId);
  if (!q) return;

  q.timesIncorrect = 0;
  q.updatedAt = Date.now();

  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.put(q);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function recordAnswer(questionId: number, selectedIndex: number): Promise<Question | null> {
  const db = await getDb();
  const q = await getQuestionById(questionId);
  if (!q) return null;

  const isCorrect = selectedIndex === q.correctAnswerIndex;
  const newTimesAttempted = q.timesAttempted + 1;
  const newTimesCorrect = isCorrect ? q.timesCorrect + 1 : q.timesCorrect;
  const newTimesIncorrect = isCorrect ? q.timesIncorrect : q.timesIncorrect + 1;
  const newConsecutive = isCorrect ? q.consecutiveCorrect + 1 : 0;

  let newStatus = q.masteryStatus;
  if (newConsecutive >= 3) {
    newStatus = 'MASTERED';
  } else if (newConsecutive >= 1) {
    newStatus = 'IMPROVING';
  } else if (newTimesIncorrect > 0) {
    newStatus = 'NEEDS_REVIEW';
  }

  q.timesAttempted = newTimesAttempted;
  q.timesCorrect = newTimesCorrect;
  q.timesIncorrect = newTimesIncorrect;
  q.consecutiveCorrect = newConsecutive;
  q.masteryStatus = newStatus;
  q.lastAnsweredTimestamp = Date.now();
  q.lastSelectedOption = selectedIndex;
  q.updatedAt = Date.now();

  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.put(q);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });

  // Record into today's goal
  await incrementDailyGoalQuestions();

  return q;
}

export async function addQuestion(question: Omit<Question, 'id'>): Promise<number> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.add(question);
    req.onsuccess = () => resolve(req.result as number);
    req.onerror = () => reject(req.error);
  });
}

export async function updateQuestion(question: Question): Promise<void> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    const req = store.put(question);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function insertBatch(questions: Omit<Question, 'id'>[]): Promise<void> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    for (const q of questions) {
      store.add(q);
    }
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function clearReviewer(track: ReviewerTrack): Promise<void> {
  const db = await getDb();
  const all = await getAllQuestions(track);
  return new Promise((resolve, reject) => {
    const tx = db.transaction('questions', 'readwrite');
    const store = tx.objectStore('questions');
    for (const q of all) {
      store.delete(q.id);
    }
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function searchQuestions(track: ReviewerTrack, query: string): Promise<Question[]> {
  const all = await getAllQuestions(track);
  if (!query.trim()) return all;

  const lower = query.toLowerCase().trim();
  return all.filter((q) => {
    return (
      q.questionText.toLowerCase().includes(lower) ||
      q.optionA.toLowerCase().includes(lower) ||
      q.optionB.toLowerCase().includes(lower) ||
      (q.optionC && q.optionC.toLowerCase().includes(lower)) ||
      (q.optionD && q.optionD.toLowerCase().includes(lower)) ||
      (q.explanation && q.explanation.toLowerCase().includes(lower)) ||
      q.competencyCode.toLowerCase().includes(lower) ||
      (q.section && q.section.toLowerCase().includes(lower)) ||
      (q.source && q.source.toLowerCase().includes(lower))
    );
  });
}

export async function reportQuestion(
  questionId: number,
  track: ReviewerTrack,
  reason: string,
  details: string
): Promise<void> {
  const db = await getDb();
  const report: QuestionReport = {
    questionId,
    track,
    reason,
    details,
    timestamp: Date.now()
  };
  return new Promise((resolve, reject) => {
    const tx = db.transaction('question_reports', 'readwrite');
    const store = tx.objectStore('question_reports');
    const req = store.add(report);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function getQuestionReports(track?: ReviewerTrack): Promise<QuestionReport[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('question_reports', 'readonly');
    const store = tx.objectStore('question_reports');
    const req = store.getAll();
    req.onsuccess = () => {
      let list: QuestionReport[] = req.result || [];
      if (track) list = list.filter((r) => r.track === track);
      resolve(list);
    };
    req.onerror = () => reject(req.error);
  });
}

// ==========================================
// EXAM RECORDS & SUBMISSION
// ==========================================
export async function submitExam(params: {
  track: ReviewerTrack;
  examType: 'OFFICIAL_SIMULATION' | 'CUSTOM';
  title: string;
  questions: Question[];
  userAnswers: Record<number, number>;
  timeUsedSeconds: number;
}): Promise<{ record: ExamRecord; answers: ExamAnswer[] }> {
  const db = await getDb();

  let score = 0;
  const competencyMap: Record<string, { total: number; correct: number }> = {};
  const examAnswers: Omit<ExamAnswer, 'id' | 'examId'>[] = [];

  for (const q of params.questions) {
    const code = q.competencyCode;
    if (!competencyMap[code]) {
      competencyMap[code] = { total: 0, correct: 0 };
    }
    competencyMap[code].total++;

    const userAns = params.userAnswers[q.id] !== undefined ? params.userAnswers[q.id] : -1;
    const isCorrect = userAns === q.correctAnswerIndex;
    if (isCorrect) {
      score++;
      competencyMap[code].correct++;
    }

    examAnswers.push({
      questionId: q.id,
      questionText: q.questionText,
      selectedIndex: userAns,
      correctIndex: q.correctAnswerIndex,
      isCorrect,
      competencyCode: q.competencyCode
    });

    // Also update question mastery stats
    if (userAns !== -1) {
      await recordAnswer(q.id, userAns);
    }
  }

  const total = params.questions.length;
  const percentage = total > 0 ? Math.round((score / total) * 100 * 10) / 10 : 0;
  const passed = percentage >= 70;

  const record: Omit<ExamRecord, 'id'> = {
    track: params.track,
    examType: params.examType,
    title: params.title,
    timestamp: Date.now(),
    totalQuestions: total,
    score,
    percentage,
    passed,
    timeUsedSeconds: params.timeUsedSeconds,
    competencyBreakdownJson: JSON.stringify(competencyMap)
  };

  const savedRecordId = await new Promise<number>((resolve, reject) => {
    const tx = db.transaction('exam_records', 'readwrite');
    const store = tx.objectStore('exam_records');
    const req = store.add(record);
    req.onsuccess = () => resolve(req.result as number);
    req.onerror = () => reject(req.error);
  });

  const fullRecord: ExamRecord = { ...record, id: savedRecordId };

  // Save answers
  const fullAnswers: ExamAnswer[] = [];
  const ansTx = db.transaction('exam_answers', 'readwrite');
  const ansStore = ansTx.objectStore('exam_answers');
  for (const a of examAnswers) {
    const fullAns: ExamAnswer = { ...a, examId: savedRecordId };
    ansStore.add(fullAns);
    fullAnswers.push(fullAns);
  }
  await new Promise<void>((resolve, reject) => {
    ansTx.oncomplete = () => resolve();
    ansTx.onerror = () => reject(ansTx.error);
  });

  return { record: fullRecord, answers: fullAnswers };
}

export async function getExamRecords(track: ReviewerTrack): Promise<ExamRecord[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('exam_records', 'readonly');
    const store = tx.objectStore('exam_records');
    const index = store.index('track');
    const req = index.getAll(IDBKeyRange.only(track));
    req.onsuccess = () => {
      const list: ExamRecord[] = req.result || [];
      list.sort((a, b) => b.timestamp - a.timestamp);
      resolve(list);
    };
    req.onerror = () => reject(req.error);
  });
}

export async function getExamRecordById(id: number): Promise<ExamRecord | null> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('exam_records', 'readonly');
    const store = tx.objectStore('exam_records');
    const req = store.get(id);
    req.onsuccess = () => resolve(req.result || null);
    req.onerror = () => reject(req.error);
  });
}

export async function getExamAnswersByExamId(examId: number): Promise<ExamAnswer[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('exam_answers', 'readonly');
    const store = tx.objectStore('exam_answers');
    const index = store.index('examId');
    const req = index.getAll(IDBKeyRange.only(examId));
    req.onsuccess = () => resolve(req.result || []);
    req.onerror = () => reject(req.error);
  });
}

// ==========================================
// SESSION LOGS (RESUME PRACTICE & EXAM)
// ==========================================
export async function saveSessionLog(log: Omit<SavedSessionLog, 'id'> & { id?: number }): Promise<number> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('saved_session_logs', 'readwrite');
    const store = tx.objectStore('saved_session_logs');
    const req = log.id ? store.put(log) : store.add(log);
    req.onsuccess = () => resolve(req.result as number);
    req.onerror = () => reject(req.error);
  });
}

export async function getSessionLogsByTrack(track: ReviewerTrack): Promise<SavedSessionLog[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('saved_session_logs', 'readonly');
    const store = tx.objectStore('saved_session_logs');
    const index = store.index('track');
    const req = index.getAll(IDBKeyRange.only(track));
    req.onsuccess = () => {
      const list: SavedSessionLog[] = req.result || [];
      list.sort((a, b) => b.lastActiveTimestamp - a.lastActiveTimestamp);
      resolve(list);
    };
    req.onerror = () => reject(req.error);
  });
}

export async function getLatestSessionLog(track: ReviewerTrack): Promise<SavedSessionLog | null> {
  const logs = await getSessionLogsByTrack(track);
  return logs[0] || null;
}

export async function deleteSessionLog(id: number): Promise<void> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('saved_session_logs', 'readwrite');
    const store = tx.objectStore('saved_session_logs');
    const req = store.delete(id);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function clearSessionLogs(track?: ReviewerTrack): Promise<void> {
  const db = await getDb();
  if (track) {
    const logs = await getSessionLogsByTrack(track);
    const tx = db.transaction('saved_session_logs', 'readwrite');
    const store = tx.objectStore('saved_session_logs');
    for (const l of logs) store.delete(l.id);
    return new Promise((res, rej) => {
      tx.oncomplete = () => res();
      tx.onerror = () => rej(tx.error);
    });
  } else {
    return new Promise((resolve, reject) => {
      const tx = db.transaction('saved_session_logs', 'readwrite');
      const store = tx.objectStore('saved_session_logs');
      const req = store.clear();
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }
}

// ==========================================
// STUDY TIMER & DAILY GOALS
// ==========================================
function getTodayDateKey(): string {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export async function getTodayGoal(): Promise<DailyGoal> {
  const db = await getDb();
  const dateKey = getTodayDateKey();

  return new Promise((resolve) => {
    const tx = db.transaction('daily_goals', 'readwrite');
    const store = tx.objectStore('daily_goals');
    const req = store.get(dateKey);
    req.onsuccess = () => {
      if (req.result) {
        resolve(req.result);
      } else {
        const newGoal: DailyGoal = {
          dateKey,
          targetQuestions: 100,
          completedQuestions: 0,
          targetStudySeconds: 3600,
          completedStudySeconds: 0,
          targetAccuracy: 70
        };
        store.put(newGoal);
        resolve(newGoal);
      }
    };
    req.onerror = () => {
      resolve({
        dateKey,
        targetQuestions: 100,
        completedQuestions: 0,
        targetStudySeconds: 3600,
        completedStudySeconds: 0,
        targetAccuracy: 70
      });
    };
  });
}

export async function updateDailyGoal(updates: Partial<DailyGoal>): Promise<DailyGoal> {
  const current = await getTodayGoal();
  const merged: DailyGoal = { ...current, ...updates };

  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('daily_goals', 'readwrite');
    const store = tx.objectStore('daily_goals');
    const req = store.put(merged);
    req.onsuccess = () => resolve(merged);
    req.onerror = () => reject(req.error);
  });
}

export async function incrementDailyGoalQuestions(): Promise<void> {
  const current = await getTodayGoal();
  await updateDailyGoal({ completedQuestions: current.completedQuestions + 1 });
}

export async function recordStudySession(track: ReviewerTrack, durationSeconds: number): Promise<void> {
  const db = await getDb();
  const session: StudySession = {
    track,
    timestamp: Date.now(),
    durationSeconds,
    questionsAnswered: 0
  };

  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction('study_sessions', 'readwrite');
    const store = tx.objectStore('study_sessions');
    const req = store.add(session);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });

  const current = await getTodayGoal();
  await updateDailyGoal({
    completedStudySeconds: current.completedStudySeconds + durationSeconds
  });
}

export async function getTotalStudySeconds(track: ReviewerTrack): Promise<number> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('study_sessions', 'readonly');
    const store = tx.objectStore('study_sessions');
    const index = store.index('track');
    const req = index.getAll(IDBKeyRange.only(track));
    req.onsuccess = () => {
      const list: StudySession[] = req.result || [];
      const total = list.reduce((acc, s) => acc + s.durationSeconds, 0);
      resolve(total);
    };
    req.onerror = () => reject(req.error);
  });
}

export async function getStudySessions(track: ReviewerTrack): Promise<StudySession[]> {
  const db = await getDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('study_sessions', 'readonly');
    const store = tx.objectStore('study_sessions');
    const index = store.index('track');
    const req = index.getAll(IDBKeyRange.only(track));
    req.onsuccess = () => {
      const list: StudySession[] = req.result || [];
      list.sort((a, b) => b.timestamp - a.timestamp);
      resolve(list);
    };
    req.onerror = () => reject(req.error);
  });
}

// ==========================================
// BACKUP AND RESTORE
// ==========================================
export async function exportBackupJson(): Promise<string> {
  const db = await getDb();

  const oic = await getAllQuestions('OIC-NW');
  const gmdss = await getAllQuestions('GMDSS');
  const allQ = [...oic, ...gmdss];

  const examRecords: ExamRecord[] = await new Promise((res) => {
    const tx = db.transaction('exam_records', 'readonly');
    const req = tx.objectStore('exam_records').getAll();
    req.onsuccess = () => res(req.result || []);
  });

  const dailyGoals: DailyGoal[] = await new Promise((res) => {
    const tx = db.transaction('daily_goals', 'readonly');
    const req = tx.objectStore('daily_goals').getAll();
    req.onsuccess = () => res(req.result || []);
  });

  const backupData = {
    version: 1,
    appName: 'Hyperion',
    exportTimestamp: Date.now(),
    questions: allQ,
    examRecords,
    dailyGoals
  };

  return JSON.stringify(backupData, null, 2);
}

export async function restoreBackupJson(jsonString: string): Promise<number> {
  const data = JSON.parse(jsonString);
  const questions: Question[] = data.questions || [];

  if (questions.length === 0) {
    throw new Error('No questions found in backup file.');
  }

  const db = await getDb();

  // Clear and insert
  const tx = db.transaction('questions', 'readwrite');
  const store = tx.objectStore('questions');
  store.clear();

  for (const q of questions) {
    // strip ID to let autoincrement recreate
    const { id, ...rest } = q;
    store.add(rest);
  }

  await new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });

  if (Array.isArray(data.examRecords) && data.examRecords.length > 0) {
    const eTx = db.transaction('exam_records', 'readwrite');
    const eStore = eTx.objectStore('exam_records');
    for (const r of data.examRecords) {
      const { id, ...rest } = r;
      eStore.add(rest);
    }
  }

  return questions.length;
}
