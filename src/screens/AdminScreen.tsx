import React, { useState, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { ReviewerTrack, Question, QuestionReport } from '../types';
import * as db from '../db/indexedDb';
import { processCsvContent } from '../data/seedQuestions';
import { read, utils } from 'xlsx';
import {
  Lock,
  Unlock,
  Upload,
  Download,
  Database,
  Trash2,
  PlusCircle,
  AlertTriangle,
  FileSpreadsheet,
  CheckCircle2,
  XCircle,
  RotateCcw,
  Smartphone,
  X
} from 'lucide-react';

export const AdminScreen: React.FC = () => {
  const {
    currentTrack,
    navigateBack,
    navigateTo,
    isProcessing,
    importPreview,
    setImportPreview,
    confirmImport,
    cancelImportPreview,
    resetReviewerDatabase,
    reseedDatabase,
    scanAndImportAssets,
    exportBackupJson,
    restoreBackupJson,
    addOrEditQuestion,
    adminMessage,
    setAdminMessage,
    dismissAdminMessage
  } = useApp();

  const [isUnlocked, setIsUnlocked] = useState(false);
  const [pin, setPin] = useState('');
  const [pinError, setPinError] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const backupInputRef = useRef<HTMLInputElement>(null);

  // Dialog states
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showReportsModal, setShowReportsModal] = useState(false);
  const [reportsList, setReportsList] = useState<QuestionReport[]>([]);

  // Add question form state
  const [newQReviewer, setNewQReviewer] = useState<ReviewerTrack>(currentTrack);
  const [newQFunction, setNewQFunction] = useState('F1');
  const [newQComp, setNewQComp] = useState('C1');
  const [newQText, setNewQText] = useState('');
  const [newQOptA, setNewQOptA] = useState('');
  const [newQOptB, setNewQOptB] = useState('');
  const [newQOptC, setNewQOptC] = useState('');
  const [newQOptD, setNewQOptD] = useState('');
  const [newQAns, setNewQAns] = useState<'A' | 'B' | 'C' | 'D'>('A');
  const [newQExplanation, setNewQExplanation] = useState('');
  const [newQSource, setNewQSource] = useState('Manual Entry');

  const handlePinSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (pin === '1234') {
      setIsUnlocked(true);
      setPinError(false);
    } else {
      setPinError(true);
    }
  };

  // Handle Excel / CSV File upload
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const fileName = file.name.toLowerCase();
      const existing = await db.getAllQuestions(currentTrack);
      const existingTexts = new Set(existing.map((q) => q.questionText.toLowerCase().replace(/\s+/g, ' ')));

      if (fileName.endsWith('.csv')) {
        const text = await file.text();
        const parsed = processCsvContent(text, currentTrack, existingTexts);
        setImportPreview({
          reviewer: currentTrack,
          validCount: parsed.validCount,
          duplicateCount: parsed.duplicateCount,
          totalParsed: parsed.totalParsed,
          errors: parsed.errors,
          warnings: parsed.warnings,
          validatedQuestions: parsed.validatedQuestions as Question[]
        });
      } else if (fileName.endsWith('.xlsx') || fileName.endsWith('.xls')) {
        const buffer = await file.arrayBuffer();
        const workbook = read(buffer, { type: 'array' });
        const firstSheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[firstSheetName];
        const csvText = utils.sheet_to_csv(sheet);

        const parsed = processCsvContent(csvText, currentTrack, existingTexts);
        setImportPreview({
          reviewer: currentTrack,
          validCount: parsed.validCount,
          duplicateCount: parsed.duplicateCount,
          totalParsed: parsed.totalParsed,
          errors: parsed.errors,
          warnings: parsed.warnings,
          validatedQuestions: parsed.validatedQuestions as Question[]
        });
      }
    } catch (err: any) {
      setAdminMessage(`Error reading file: ${err.message}`);
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // Handle JSON backup export
  const handleExportBackup = async () => {
    try {
      const json = await exportBackupJson();
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `hyperion_backup_${new Date().toISOString().split('T')[0]}.hyperion`;
      a.click();
      URL.revokeObjectURL(url);
      setAdminMessage('Backup file exported successfully!');
    } catch (err: any) {
      setAdminMessage(`Export error: ${err.message}`);
    }
  };

  // Handle JSON backup restore
  const handleRestoreFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const text = await file.text();
      const count = await restoreBackupJson(text);
      setAdminMessage(`Successfully restored ${count} questions from backup!`);
    } catch (err: any) {
      setAdminMessage(`Restore error: ${err.message}`);
    } finally {
      if (backupInputRef.current) backupInputRef.current.value = '';
    }
  };

  const handleAddQuestionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newQText.trim() || !newQOptA.trim() || !newQOptB.trim()) {
      alert('Question text, Option A, and Option B are required.');
      return;
    }

    const idx = newQAns.charCodeAt(0) - 65;

    await addOrEditQuestion({
      reviewer: newQReviewer,
      function: newQReviewer === 'OIC-NW' ? newQFunction : null,
      competencyCode: newQComp,
      competencyDescription: `Competency ${newQComp}`,
      questionText: newQText.trim(),
      questionType: 'Multiple Choice',
      optionA: newQOptA.trim(),
      optionB: newQOptB.trim(),
      optionC: newQOptC.trim() || null,
      optionD: newQOptD.trim() || null,
      correctAnswerLetter: newQAns,
      correctAnswerIndex: idx,
      explanation: newQExplanation.trim() || null,
      point: 1,
      source: newQSource.trim() || 'Manual',
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

    setShowAddModal(false);
    setNewQText('');
    setNewQOptA('');
    setNewQOptB('');
    setNewQOptC('');
    setNewQOptD('');
    setAdminMessage('Question added successfully!');
  };

  const openReportsModal = async () => {
    const list = await db.getQuestionReports();
    setReportsList(list);
    setShowReportsModal(true);
  };

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Admin Panel"
        subtitle="Question Bank & Database Management"
        onBack={navigateBack}
      />

      <div className="max-w-2xl mx-auto w-full px-4 sm:px-6 pt-6">
        {!isUnlocked ? (
          /* PIN Entry Screen */
          <div className="bg-[#0F3460] border border-[#1E3E62] rounded-3xl p-8 shadow-2xl text-center max-w-md mx-auto my-12 space-y-4">
            <div className="w-16 h-16 rounded-full bg-[#0B192C] border border-[#2E86AB]/50 flex items-center justify-center mx-auto text-[#D4AF37]">
              <Lock className="w-8 h-8" />
            </div>

            <h2 className="text-xl font-black text-white">Administrator Access</h2>
            <p className="text-xs text-slate-300">
              Enter Administrator PIN to unlock question bank management and database operations.
              <span className="block mt-1 font-bold text-[#D4AF37]">(Default PIN: 1234)</span>
            </p>

            <form onSubmit={handlePinSubmit} className="space-y-4 pt-2">
              <input
                type="password"
                maxLength={8}
                value={pin}
                onChange={(e) => {
                  setPin(e.target.value);
                  setPinError(false);
                }}
                placeholder="Enter PIN"
                className="w-full bg-[#0B192C] border border-[#1E3E62] focus:border-[#D4AF37] rounded-xl px-4 py-3 text-center text-lg font-mono tracking-widest text-white outline-none"
              />

              {pinError && (
                <div className="text-xs text-red-400 font-bold">
                  Incorrect PIN. Please enter the default PIN 1234.
                </div>
              )}

              <button
                type="submit"
                className="w-full py-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black text-sm rounded-xl tracking-wider uppercase transition-colors shadow cursor-pointer"
              >
                UNLOCK ADMIN PANEL
              </button>
            </form>
          </div>
        ) : (
          /* Unlocked Admin Hub */
          <div className="space-y-5">
            {/* Notification alert */}
            {adminMessage && (
              <div className="bg-[#0F3460] border border-[#2E86AB] rounded-xl p-3.5 flex items-center justify-between text-xs text-slate-200">
                <span>{adminMessage}</span>
                <button onClick={dismissAdminMessage} className="text-slate-400 hover:text-white p-1">
                  <X className="w-4 h-4" />
                </button>
              </div>
            )}

            {/* Importer Section */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <div className="flex items-center gap-2.5">
                <FileSpreadsheet className="w-5 h-5 text-[#D4AF37]" />
                <h3 className="font-bold text-sm text-white uppercase tracking-wider">
                  Reviewer File Import (Excel / CSV)
                </h3>
              </div>

              <p className="text-xs text-slate-300 leading-relaxed">
                Upload your MARINA exam question bank in <strong>.xlsx</strong>, <strong>.xls</strong>, or <strong>.csv</strong> format.
                Standard headers supported: <em>Title, Question, Option1..Option4, Answer, Explanation, Section</em>.
              </p>

              <input
                type="file"
                ref={fileInputRef}
                accept=".xlsx,.xls,.csv"
                onChange={handleFileUpload}
                className="hidden"
              />

              <div className="flex flex-col sm:flex-row gap-2.5">
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="flex-1 py-3 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-xs rounded-xl flex items-center justify-center gap-2 shadow transition-colors cursor-pointer uppercase"
                >
                  <Upload className="w-4 h-4" />
                  <span>SELECT EXCEL (.XLSX) / CSV</span>
                </button>

                <button
                  onClick={scanAndImportAssets}
                  className="py-3 px-4 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-bold text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer"
                >
                  <Database className="w-4 h-4 text-[#D4AF37]" />
                  <span>LOAD BUNDLED CSV ASSETS</span>
                </button>
              </div>
            </div>

            {/* Database Management Tools */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <div className="flex items-center gap-2.5">
                <Database className="w-5 h-5 text-[#2E86AB]" />
                <h3 className="font-bold text-sm text-white uppercase tracking-wider">
                  Database & Question Tools
                </h3>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  onClick={() => setShowAddModal(true)}
                  className="p-3.5 bg-[#0B192C] hover:bg-[#153e74] border border-[#1E3E62] rounded-xl flex items-center gap-3 transition-colors cursor-pointer text-left"
                >
                  <PlusCircle className="w-5 h-5 text-emerald-400 shrink-0" />
                  <div>
                    <div className="text-xs font-bold text-white">Create New Question</div>
                    <div className="text-[11px] text-slate-400">Add custom item manually</div>
                  </div>
                </button>

                <button
                  onClick={reseedDatabase}
                  className="p-3.5 bg-[#0B192C] hover:bg-[#153e74] border border-[#1E3E62] rounded-xl flex items-center gap-3 transition-colors cursor-pointer text-left"
                >
                  <RotateCcw className="w-5 h-5 text-[#D4AF37] shrink-0" />
                  <div>
                    <div className="text-xs font-bold text-white">Restore Seed Questions</div>
                    <div className="text-[11px] text-slate-400">Re-verify authentic questions</div>
                  </div>
                </button>

                <button
                  onClick={openReportsModal}
                  className="p-3.5 bg-[#0B192C] hover:bg-[#153e74] border border-[#1E3E62] rounded-xl flex items-center gap-3 transition-colors cursor-pointer text-left"
                >
                  <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />
                  <div>
                    <div className="text-xs font-bold text-white">User Question Reports</div>
                    <div className="text-[11px] text-slate-400">View reported inaccuracies</div>
                  </div>
                </button>

                <button
                  onClick={() => setShowClearConfirm(true)}
                  className="p-3.5 bg-[#0B192C] hover:bg-red-950/40 border border-red-500/30 rounded-xl flex items-center gap-3 transition-colors cursor-pointer text-left"
                >
                  <Trash2 className="w-5 h-5 text-red-400 shrink-0" />
                  <div>
                    <div className="text-xs font-bold text-red-300">Clear {currentTrack} Bank</div>
                    <div className="text-[11px] text-slate-400">Purge items in active track</div>
                  </div>
                </button>
              </div>
            </div>

            {/* Android APK Release Package */}
            <div className="bg-[#0F3460] border border-[#D4AF37]/50 rounded-2xl p-5 shadow space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-lg bg-[#0B192C] border border-[#D4AF37] flex items-center justify-center text-[#D4AF37]">
                    <Download className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="font-bold text-sm text-white uppercase tracking-wider">
                      Android APK Release Package
                    </h3>
                    <p className="text-[11px] text-[#D4AF37]">
                      Compiled Standalone Offline APK (v1.0 • 17 MB)
                    </p>
                  </div>
                </div>

                <span className="text-[10px] font-black bg-emerald-950/70 border border-emerald-500/50 text-emerald-300 px-2.5 py-1 rounded-full">
                  READY
                </span>
              </div>

              <p className="text-xs text-slate-300 leading-relaxed">
                Directly install the Hyperion MARINA Reviewer APK on your Android device for complete offline sea-time review without internet access.
              </p>

              <div className="flex flex-col sm:flex-row gap-2.5 pt-1">
                <a
                  href="/app-debug.apk"
                  download="Hyperion-MARINA-Reviewer.apk"
                  className="flex-1 py-3 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-xs rounded-xl flex items-center justify-center gap-2 shadow transition-colors cursor-pointer uppercase tracking-wider"
                >
                  <Download className="w-4 h-4" />
                  <span>Download APK (17 MB)</span>
                </a>
                <button
                  onClick={() => navigateTo('PlayStore')}
                  className="py-3 px-4 bg-[#0B192C] hover:bg-[#1E3E62] border border-cyan-400 text-cyan-300 font-extrabold text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer uppercase tracking-wider"
                >
                  <Smartphone className="w-4 h-4" />
                  <span>Play Store Suite & .AAB</span>
                </button>
              </div>
            </div>

            {/* Offline Backup & Restore (.hyperion / .json) */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <div className="flex items-center gap-2.5">
                <Download className="w-5 h-5 text-emerald-400" />
                <h3 className="font-bold text-sm text-white uppercase tracking-wider">
                  Full Database Backup & Restore
                </h3>
              </div>

              <p className="text-xs text-slate-300">
                Export all questions, exam history records, and daily study goals into a single offline <strong>.hyperion</strong> backup file.
              </p>

              <input
                type="file"
                ref={backupInputRef}
                accept=".hyperion,.json"
                onChange={handleRestoreFile}
                className="hidden"
              />

              <div className="flex gap-2.5">
                <button
                  onClick={handleExportBackup}
                  className="flex-1 py-2.5 px-4 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-bold text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer"
                >
                  <Download className="w-4 h-4" />
                  <span>EXPORT BACKUP FILE</span>
                </button>

                <button
                  onClick={() => backupInputRef.current?.click()}
                  className="flex-1 py-2.5 px-4 bg-[#0B192C] hover:bg-[#153e74] border border-white/10 text-slate-200 font-bold text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer"
                >
                  <Upload className="w-4 h-4" />
                  <span>RESTORE FROM FILE</span>
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Import Preview Modal */}
      {importPreview && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-lg rounded-2xl p-6 shadow-2xl text-white space-y-4 max-h-[85vh] flex flex-col">
            <div className="flex items-start justify-between pb-3 border-b border-white/10">
              <div>
                <h3 className="font-bold text-white text-base">Import Validation Preview</h3>
                <p className="text-xs text-slate-300">Track: {importPreview.reviewer}</p>
              </div>
              <button onClick={cancelImportPreview} className="text-slate-400 p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between bg-[#0B192C] p-3 rounded-xl border border-white/5">
                <span>Valid Questions to Import:</span>
                <strong className="text-emerald-400 text-sm">{importPreview.validCount}</strong>
              </div>
              <div className="flex justify-between bg-[#0B192C] p-3 rounded-xl border border-white/5">
                <span>Duplicates Skipped:</span>
                <strong className="text-amber-400 text-sm">{importPreview.duplicateCount}</strong>
              </div>
              <div className="flex justify-between bg-[#0B192C] p-3 rounded-xl border border-white/5">
                <span>Total Parsed:</span>
                <strong className="text-white text-sm">{importPreview.totalParsed}</strong>
              </div>
            </div>

            {importPreview.warnings.length > 0 && (
              <div className="bg-amber-950/40 border border-amber-600/30 rounded-xl p-3 text-xs text-amber-300 max-h-24 overflow-y-auto">
                <div className="font-bold mb-1">Warnings ({importPreview.warnings.length}):</div>
                {importPreview.warnings.slice(0, 5).map((w, i) => (
                  <div key={i}>{w}</div>
                ))}
              </div>
            )}

            {importPreview.errors.length > 0 && (
              <div className="bg-red-950/40 border border-red-600/30 rounded-xl p-3 text-xs text-red-300 max-h-24 overflow-y-auto">
                <div className="font-bold mb-1">Errors ({importPreview.errors.length}):</div>
                {importPreview.errors.slice(0, 5).map((err, i) => (
                  <div key={i}>{err}</div>
                ))}
              </div>
            )}

            <div className="pt-3 border-t border-white/10 flex justify-end gap-2.5">
              <button
                onClick={cancelImportPreview}
                className="py-2.5 px-4 text-xs font-bold text-slate-300 hover:text-white"
              >
                Cancel
              </button>
              <button
                disabled={importPreview.validCount === 0 || isProcessing}
                onClick={confirmImport}
                className="py-2.5 px-5 bg-[#D4AF37] hover:bg-[#F3C64F] disabled:opacity-40 text-[#0B192C] text-xs font-black rounded-xl shadow cursor-pointer uppercase"
              >
                {isProcessing ? 'Importing...' : `Confirm Import (${importPreview.validCount} Items)`}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clear Bank Confirmation Dialog */}
      {showClearConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0F3460] border border-red-500 w-full max-w-md rounded-2xl p-6 shadow-2xl text-white text-center">
            <AlertTriangle className="w-12 h-12 text-red-400 mx-auto mb-3" />
            <h3 className="text-xl font-black text-white">Clear {currentTrack} Questions?</h3>
            <p className="text-xs text-slate-300 mt-2">
              Are you sure you want to permanently delete all questions in the <strong>{currentTrack}</strong> reviewer bank?
              This action cannot be undone unless you re-import or restore from backup.
            </p>

            <div className="flex gap-2.5 mt-6">
              <button
                onClick={() => setShowClearConfirm(false)}
                className="flex-1 py-2.5 rounded-xl border border-white/20 text-slate-300 text-xs font-bold"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  setShowClearConfirm(false);
                  resetReviewerDatabase(currentTrack);
                }}
                className="flex-1 py-2.5 rounded-xl bg-red-600 hover:bg-red-500 text-white text-xs font-black shadow"
              >
                YES, CLEAR BANK
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Manual Add Question Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in overflow-y-auto">
          <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-lg rounded-2xl p-6 shadow-2xl text-white my-8 max-h-[90vh] flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <h3 className="font-bold text-white text-base">Add New Question</h3>
              <button onClick={() => setShowAddModal(false)} className="text-slate-400 p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleAddQuestionSubmit} className="overflow-y-auto py-3 space-y-3.5 pr-1 flex-1">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Track</label>
                  <select
                    value={newQReviewer}
                    onChange={(e) => setNewQReviewer(e.target.value as ReviewerTrack)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  >
                    <option value="OIC-NW">OIC-NW</option>
                    <option value="GMDSS">GMDSS</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Competency</label>
                  <input
                    type="text"
                    value={newQComp}
                    onChange={(e) => setNewQComp(e.target.value)}
                    placeholder="e.g. C1"
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-300 mb-1">Question Text</label>
                <textarea
                  rows={3}
                  required
                  value={newQText}
                  onChange={(e) => setNewQText(e.target.value)}
                  placeholder="Enter full question..."
                  className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl p-3 text-xs text-white"
                />
              </div>

              <div className="space-y-2">
                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Option A</label>
                  <input
                    type="text"
                    required
                    value={newQOptA}
                    onChange={(e) => setNewQOptA(e.target.value)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Option B</label>
                  <input
                    type="text"
                    required
                    value={newQOptB}
                    onChange={(e) => setNewQOptB(e.target.value)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Option C</label>
                  <input
                    type="text"
                    value={newQOptC}
                    onChange={(e) => setNewQOptC(e.target.value)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Option D</label>
                  <input
                    type="text"
                    value={newQOptD}
                    onChange={(e) => setNewQOptD(e.target.value)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-bold text-[#D4AF37] mb-1">Correct Answer</label>
                  <select
                    value={newQAns}
                    onChange={(e) => setNewQAns(e.target.value as any)}
                    className="w-full bg-[#0B192C] border border-[#D4AF37] rounded-xl px-3 py-2 text-xs font-bold text-[#D4AF37]"
                  >
                    <option value="A">Option A</option>
                    <option value="B">Option B</option>
                    <option value="C">Option C</option>
                    <option value="D">Option D</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[11px] font-bold text-slate-300 mb-1">Source Sheet</label>
                  <input
                    type="text"
                    value={newQSource}
                    onChange={(e) => setNewQSource(e.target.value)}
                    className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-300 mb-1">Explanation & Rationale</label>
                <textarea
                  rows={2}
                  value={newQExplanation}
                  onChange={(e) => setNewQExplanation(e.target.value)}
                  placeholder="Maritime regulatory reference..."
                  className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl p-3 text-xs text-white"
                />
              </div>

              <div className="pt-3 border-t border-white/10 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 text-xs text-slate-400"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-[#D4AF37] text-[#0B192C] font-black text-xs rounded-xl shadow cursor-pointer uppercase"
                >
                  Save Question
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reports Viewer Modal */}
      {showReportsModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0B192C] border border-[#1E3E62] w-full max-w-lg rounded-2xl p-5 shadow-2xl text-white max-h-[85vh] flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <h3 className="font-bold text-white text-base">Question Error Reports</h3>
              <button onClick={() => setShowReportsModal(false)} className="text-slate-400 p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="overflow-y-auto py-3 space-y-3 flex-1">
              {reportsList.length === 0 ? (
                <p className="text-xs text-slate-400 text-center py-6">
                  No issues reported by users yet.
                </p>
              ) : (
                reportsList.map((r, i) => (
                  <div key={i} className="bg-[#0F3460] p-3 rounded-xl border border-white/5 space-y-1">
                    <div className="flex justify-between text-xs">
                      <span className="font-bold text-[#D4AF37]">{r.reason}</span>
                      <span className="text-slate-400">{new Date(r.timestamp).toLocaleDateString()}</span>
                    </div>
                    <div className="text-xs text-slate-200">
                      <strong>Question #{r.questionId}</strong> ({r.track})
                    </div>
                    {r.details && (
                      <p className="text-xs text-slate-300 italic bg-[#0B192C] p-2 rounded">
                        "{r.details}"
                      </p>
                    )}
                  </div>
                ))
              )}
            </div>

            <div className="pt-3 border-t border-white/10 flex justify-end">
              <button
                onClick={() => setShowReportsModal(false)}
                className="px-4 py-2 bg-[#1E3E62] text-xs font-bold rounded-xl"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
