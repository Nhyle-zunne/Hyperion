import React, { useState, useEffect, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import {
  Smartphone,
  Download,
  Copy,
  Check,
  FileCode2,
  ShieldCheck,
  Award,
  Layers,
  Sparkles,
  ExternalLink,
  Info,
  Key,
  Terminal,
  Image as ImageIcon
} from 'lucide-react';
import { generateAndroidProjectZip, downloadBlob } from '../utils/androidProjectExport';

export const PlayStoreScreen: React.FC = () => {
  const { navigateTo } = useApp();
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [isExportingZip, setIsExportingZip] = useState(false);
  const [canInstallPwa, setCanInstallPwa] = useState(false);
  const [activeTab, setActiveTab] = useState<'overview' | 'metadata' | 'graphics' | 'guide'>('overview');
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const checkPwa = () => {
      if (typeof window !== 'undefined' && 'deferredPrompt' in window && (window as any).deferredPrompt) {
        setCanInstallPwa(true);
      }
    };
    checkPwa();
    window.addEventListener('pwa-install-ready', checkPwa);
    return () => window.removeEventListener('pwa-install-ready', checkPwa);
  }, []);

  // Draw 1024x500 Google Play Feature Graphic banner on canvas
  useEffect(() => {
    if (activeTab === 'graphics' && canvasRef.current) {
      const canvas = canvasRef.current;
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      canvas.width = 1024;
      canvas.height = 500;

      // Deep nautical navy gradient background
      const gradient = ctx.createLinearGradient(0, 0, 1024, 500);
      gradient.addColorStop(0, '#070F1B');
      gradient.addColorStop(0.5, '#0B192C');
      gradient.addColorStop(1, '#0F3460');
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, 1024, 500);

      // Subtle nautical grid lines
      ctx.strokeStyle = 'rgba(212, 175, 55, 0.08)';
      ctx.lineWidth = 1;
      for (let x = 50; x < 1024; x += 60) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, 500);
        ctx.stroke();
      }
      for (let y = 50; y < 500; y += 60) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(1024, y);
        ctx.stroke();
      }

      // Compass Ring Background
      ctx.strokeStyle = 'rgba(212, 175, 55, 0.18)';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.arc(800, 250, 180, 0, Math.PI * 2);
      ctx.stroke();

      ctx.strokeStyle = 'rgba(212, 175, 55, 0.12)';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.arc(800, 250, 140, 0, Math.PI * 2);
      ctx.stroke();

      // Golden Compass Emblem on Right
      ctx.save();
      ctx.translate(800, 250);
      // Compass Points
      // North Needle
      ctx.fillStyle = '#F3C64F';
      ctx.beginPath();
      ctx.moveTo(0, -110);
      ctx.lineTo(25, 0);
      ctx.lineTo(0, -15);
      ctx.fill();
      ctx.fillStyle = '#D4AF37';
      ctx.beginPath();
      ctx.moveTo(0, -110);
      ctx.lineTo(-25, 0);
      ctx.lineTo(0, -15);
      ctx.fill();

      // South Needle
      ctx.fillStyle = '#64748B';
      ctx.beginPath();
      ctx.moveTo(0, 110);
      ctx.lineTo(25, 0);
      ctx.lineTo(0, 15);
      ctx.fill();
      ctx.fillStyle = '#334155';
      ctx.beginPath();
      ctx.moveTo(0, 110);
      ctx.lineTo(-25, 0);
      ctx.lineTo(0, 15);
      ctx.fill();

      // Center pivot
      ctx.fillStyle = '#D4AF37';
      ctx.beginPath();
      ctx.arc(0, 0, 14, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();

      // Text Header on Left
      ctx.fillStyle = '#D4AF37';
      ctx.font = 'bold 22px system-ui, -apple-system, sans-serif';
      ctx.fillText('PHILIPPINE LICENSURE PREPARATION', 80, 140);

      ctx.fillStyle = '#FFFFFF';
      ctx.font = '900 68px system-ui, -apple-system, sans-serif';
      ctx.fillText('HYPERION', 80, 215);

      ctx.fillStyle = '#38BDF8';
      ctx.font = '700 28px system-ui, -apple-system, sans-serif';
      ctx.fillText('MARINA EXAM REVIEWER', 80, 260);

      // Feature Badges
      ctx.fillStyle = '#94A3B8';
      ctx.font = '500 20px system-ui, -apple-system, sans-serif';
      ctx.fillText('★ OIC-NW (Functions 1, 2, 3)  ★ GMDSS (GOC Competencies)', 80, 315);
      ctx.fillText('★ 100% Offline  ★ Mock Exam Simulations  ★ Real-time Analytics', 80, 350);

      // Gold Accent Line
      ctx.fillStyle = '#D4AF37';
      ctx.fillRect(80, 385, 480, 4);
    }
  }, [activeTab]);

  const copyToClipboard = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2500);
  };

  const handleDownloadProjectZip = async () => {
    try {
      setIsExportingZip(true);
      const zipBlob = await generateAndroidProjectZip();
      downloadBlob(zipBlob, 'Hyperion-Android-Project.zip');
    } catch (e) {
      console.error(e);
    } finally {
      setIsExportingZip(false);
    }
  };

  const handleDownloadBanner = () => {
    if (canvasRef.current) {
      const dataUrl = canvasRef.current.toDataURL('image/png');
      const a = document.createElement('a');
      a.href = dataUrl;
      a.download = 'playstore-feature-graphic-1024x500.png';
      a.click();
    }
  };

  const handleInstallPwa = async () => {
    if (typeof window !== 'undefined' && (window as any).triggerPwaInstall) {
      const installed = await (window as any).triggerPwaInstall();
      if (installed) {
        setCanInstallPwa(false);
      }
    }
  };

  const APP_TITLE = 'Hyperion: MARINA Reviewer';
  const SHORT_DESC = 'Offline MARINA Licensure Exam Reviewer for Philippine OIC-NW & GMDSS Seafarers.';
  const FULL_DESC = `Hyperion is the premier offline-first mobile reviewer specifically engineered for Filipino merchant marine officers and deck cadets preparing for the Maritime Industry Authority (MARINA) Board Licensure Examinations.

Covers both Deck Department Licensure Tracks in accordance with the STCW 1978 Convention, as amended (Manila 2010 Amendments):

1. OFFICER IN CHARGE OF A NAVIGATIONAL WATCH (OIC-NW)
• Function 1: Navigation at the Operational Level (Competencies C1 – C6)
  - Plan and conduct a passage and determine position
  - Maintain a safe navigational watch
  - Use of radar and ARPA to maintain safety of navigation
  - Use of ECDIS to maintain the safety of navigation
  - Respond to emergencies and distress signals
• Function 2: Cargo Handling and Stowage (Competencies C7 – C9)
  - Monitor the loading, stowage, securing, care during voyage, and unloading of cargoes
  - Inspect and report defects and damage to cargo spaces, hatch covers, and ballast tanks
  - Carriage of dangerous, hazardous, and harmful cargoes
• Function 3: Controlling the Operation of the Ship & Care for Persons on Board (Competencies C10 – C15)
  - Ensure compliance with pollution-prevention requirements (MARPOL 73/78)
  - Maintain seaworthiness of the ship
  - Prevent, control, and fight fires on board
  - Operate life-saving appliances
  - Apply medical first aid on board ship
  - Monitor compliance with legislative requirements (SOLAS, MLC 2006, ISM/ISPS Code)

2. GLOBAL MARITIME DISTRESS AND SAFETY SYSTEM (GMDSS - GOC)
• General Operator's Certificate Competencies C1 – C6
  - VHF, MF, and HF DSC radiocommunications
  - INMARSAT-C, FleetBroadband, and satellite mobile services
  - Search and Rescue (SAR) communications and IAMSAR procedures
  - EPIRB (406 MHz), SART, NAVTEX, and Maritime Safety Information (MSI)
  - Battery maintenance, radio power sources, and emergency antenna rigging

KEY FEATURES FOR SEAFARERS:
★ 100% OFFLINE CAPABILITY: Study seamlessly in your cabin, mess hall, or at sea without an internet connection or cellular data.
★ 4 STUDY MODES:
  - Tutor Mode: Instant feedback with detailed MARINA regulatory rationale for every answer.
  - Mock Exam Mode: Official 100-question timed simulation (120 minutes) with real-time question matrix.
  - Flashcard Drill: Active recall mode for rapid memorization.
  - Speed Run / Shuffle: Quick practice drills for short study breaks.
★ COMPREHENSIVE ANALYTICS: Track passing percentage (70% MARINA threshold), competency-by-competency mastery, and coverage.
★ TARGETED PRACTICE: Filter by Unanswered, Incorrect Misses, Starred Favorites, and Flagged questions.
★ RESUMABLE SESSIONS: Pick up right where you left off if duty calls.
★ DISTRACTOR ELIMINATION: Strike through incorrect options to narrow down the best maritime answer.
★ CUSTOM QUESTION IMPORTER: Import institution questions via CSV or Excel (.xlsx) and create backup archives.

Built by seafarers, for seafarers. Pass your MARINA theoretical exam with confidence.`;

  return (
    <div className="min-h-screen bg-[#070F1B] pb-16">
      <HyperionTopBar
        title="Play Store Release Suite"
        subtitle="Google Play Store & Android Package Management"
        onBack={() => navigateTo('Home')}
      />

      <main className="max-w-4xl mx-auto px-4 pt-5 space-y-6">
        {/* Hero Header */}
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#0B192C] via-[#0F3460] to-[#1E3E62] border border-[#D4AF37]/50 p-6 shadow-xl">
          <div className="absolute top-0 right-0 w-80 h-80 bg-[#D4AF37]/10 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="space-y-2">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#070F1B]/70 border border-[#D4AF37]/50 text-[#D4AF37] text-xs font-black uppercase tracking-wider">
                <Smartphone className="w-3.5 h-3.5" />
                <span>Google Play Ready (targetSdk 35)</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
                Hyperion Android & Play Store Hub
              </h2>
              <p className="text-sm text-slate-300 max-w-xl leading-relaxed">
                Everything required to publish Hyperion to Google Play Store as an official Android app: Android App Bundle (.aab), standalone APK package, Store Listing copy, and compliance documentation.
              </p>
            </div>

            {/* Quick Specs Pill */}
            <div className="bg-[#070F1B]/90 border border-[#1E3E62] rounded-xl p-4 text-xs space-y-2 min-w-[210px]">
              <div className="flex justify-between text-slate-400">
                <span>Package ID:</span>
                <span className="font-mono text-emerald-400 font-bold">com.hyperion.marinareviewer</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Version:</span>
                <span className="text-white font-bold">1.0.0 (Code 1)</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Target SDK:</span>
                <span className="text-amber-400 font-bold">Android 15 (API 35)</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Min SDK:</span>
                <span className="text-slate-300">Android 7.0 (API 24)</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Architecture:</span>
                <span className="text-cyan-400 font-bold">64-bit Compliant</span>
              </div>
            </div>
          </div>
        </div>

        {/* Primary Action Buttons */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Card 1: Direct APK */}
          <div className="bg-[#0F3460] border border-[#D4AF37]/40 rounded-2xl p-5 shadow flex flex-col justify-between hover:border-[#D4AF37] transition-all">
            <div className="space-y-2.5">
              <div className="w-10 h-10 rounded-xl bg-[#0B192C] border border-[#D4AF37] flex items-center justify-center text-[#D4AF37]">
                <Smartphone className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-base">Direct Android APK</h3>
              <p className="text-xs text-slate-300">
                Download the compiled standalone APK (17 MB). Sideload and test immediately on any physical Android device.
              </p>
            </div>
            <a
              href="/app-debug.apk"
              download="Hyperion-MARINA-Reviewer-v1.0.apk"
              className="mt-4 py-3 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer uppercase tracking-wider shadow"
            >
              <Download className="w-4 h-4" />
              <span>Download APK (17 MB)</span>
            </a>
          </div>

          {/* Card 2: Android Studio Project ZIP */}
          <div className="bg-[#0F3460] border border-cyan-500/40 rounded-2xl p-5 shadow flex flex-col justify-between hover:border-cyan-400 transition-all">
            <div className="space-y-2.5">
              <div className="w-10 h-10 rounded-xl bg-[#0B192C] border border-cyan-400 flex items-center justify-center text-cyan-400">
                <FileCode2 className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-base">Android Studio Project</h3>
              <p className="text-xs text-slate-300">
                Download complete ready-to-build Android Studio project (.zip) with Kotlin source, Gradle scripts, and Play Store configs.
              </p>
            </div>
            <button
              onClick={handleDownloadProjectZip}
              disabled={isExportingZip}
              className="mt-4 py-3 px-4 bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50 text-[#070F1B] font-black text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer uppercase tracking-wider shadow"
            >
              <Download className="w-4 h-4" />
              <span>{isExportingZip ? 'Packaging ZIP...' : 'Export Project (.zip)'}</span>
            </button>
          </div>

          {/* Card 3: PWA WebAPK Direct Install */}
          <div className="bg-[#0F3460] border border-emerald-500/40 rounded-2xl p-5 shadow flex flex-col justify-between hover:border-emerald-400 transition-all">
            <div className="space-y-2.5">
              <div className="w-10 h-10 rounded-xl bg-[#0B192C] border border-emerald-400 flex items-center justify-center text-emerald-400">
                <Layers className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-base">PWA Android Install</h3>
              <p className="text-xs text-slate-300">
                Install as a progressive WebAPK directly to your Android launcher. Works offline with background asset caching.
              </p>
            </div>
            {canInstallPwa ? (
              <button
                onClick={handleInstallPwa}
                className="mt-4 py-3 px-4 bg-emerald-500 hover:bg-emerald-400 text-white font-black text-xs rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer uppercase tracking-wider shadow"
              >
                <Smartphone className="w-4 h-4" />
                <span>Install on Device</span>
              </button>
            ) : (
              <div className="mt-4 py-3 px-3 bg-[#0B192C] border border-emerald-500/30 rounded-xl text-center text-[11px] text-emerald-300 font-medium">
                PWA Manifest & Service Worker Active
              </div>
            )}
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-[#1E3E62] overflow-x-auto gap-2 pt-2">
          <button
            onClick={() => setActiveTab('overview')}
            className={`pb-3 px-3 text-xs sm:text-sm font-bold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
              activeTab === 'overview'
                ? 'border-[#D4AF37] text-[#D4AF37]'
                : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Play Store Compliance</span>
          </button>
          <button
            onClick={() => setActiveTab('metadata')}
            className={`pb-3 px-3 text-xs sm:text-sm font-bold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
              activeTab === 'metadata'
                ? 'border-[#D4AF37] text-[#D4AF37]'
                : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            <Copy className="w-4 h-4" />
            <span>Store Listing Copy</span>
          </button>
          <button
            onClick={() => setActiveTab('graphics')}
            className={`pb-3 px-3 text-xs sm:text-sm font-bold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
              activeTab === 'graphics'
                ? 'border-[#D4AF37] text-[#D4AF37]'
                : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            <ImageIcon className="w-4 h-4" />
            <span>Play Store Graphics</span>
          </button>
          <button
            onClick={() => setActiveTab('guide')}
            className={`pb-3 px-3 text-xs sm:text-sm font-bold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
              activeTab === 'guide'
                ? 'border-[#D4AF37] text-[#D4AF37]'
                : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            <Terminal className="w-4 h-4" />
            <span>Publishing Guide</span>
          </button>
        </div>

        {/* TAB 1: OVERVIEW & COMPLIANCE */}
        {activeTab === 'overview' && (
          <div className="space-y-5">
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Check className="w-5 h-5 text-emerald-400" />
                <span>Google Play Developer Policy Checklist</span>
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                <div className="p-3 bg-[#0B192C] rounded-xl border border-emerald-500/30 flex items-start gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    ✓
                  </div>
                  <div>
                    <h4 className="font-bold text-white">Target API 35 (Android 15)</h4>
                    <p className="text-slate-400 text-[11px]">
                      Complies with Google Play's mandatory target API level policy.
                    </p>
                  </div>
                </div>

                <div className="p-3 bg-[#0B192C] rounded-xl border border-emerald-500/30 flex items-start gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    ✓
                  </div>
                  <div>
                    <h4 className="font-bold text-white">64-bit Architecture</h4>
                    <p className="text-slate-400 text-[11px]">
                      ARM64-v8a and x86_64 native binaries included in APK/AAB builds.
                    </p>
                  </div>
                </div>

                <div className="p-3 bg-[#0B192C] rounded-xl border border-emerald-500/30 flex items-start gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    ✓
                  </div>
                  <div>
                    <h4 className="font-bold text-white">Zero Telemetry & Data Safety</h4>
                    <p className="text-slate-400 text-[11px]">
                      No data collection or tracking. 100% on-device local storage.
                    </p>
                  </div>
                </div>

                <div className="p-3 bg-[#0B192C] rounded-xl border border-emerald-500/30 flex items-start gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    ✓
                  </div>
                  <div>
                    <h4 className="font-bold text-white">Data Extraction & Backup Rules</h4>
                    <p className="text-slate-400 text-[11px]">
                      Android 12+ compliant data_extraction_rules.xml configured.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Release Keystore Command */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-white font-bold text-sm">
                  <Key className="w-4 h-4 text-[#D4AF37]" />
                  <span>Release Keystore Generation Command</span>
                </div>
                <button
                  onClick={() =>
                    copyToClipboard(
                      'keytool -genkey -v -keystore release.keystore -alias hyperionkey -keyalg RSA -keysize 2048 -validity 10000',
                      'keystore'
                    )
                  }
                  className="px-3 py-1.5 bg-[#0B192C] hover:bg-[#1E3E62] border border-[#D4AF37]/50 rounded-lg text-xs font-bold text-[#D4AF37] flex items-center gap-1.5 transition-colors cursor-pointer"
                >
                  {copiedKey === 'keystore' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedKey === 'keystore' ? 'Copied' : 'Copy Command'}</span>
                </button>
              </div>

              <pre className="p-3.5 bg-[#070F1B] rounded-xl border border-[#1E3E62] font-mono text-[11px] text-amber-300 overflow-x-auto whitespace-pre-wrap">
                keytool -genkey -v -keystore release.keystore -alias hyperionkey -keyalg RSA -keysize 2048 -validity 10000
              </pre>
            </div>

            {/* Gradle Build Command */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-white font-bold text-sm">
                  <Terminal className="w-4 h-4 text-cyan-400" />
                  <span>Generate App Bundle (.aab) Command</span>
                </div>
                <button
                  onClick={() => copyToClipboard('cd android && ./gradlew bundleRelease', 'aab_cmd')}
                  className="px-3 py-1.5 bg-[#0B192C] hover:bg-[#1E3E62] border border-cyan-400/50 rounded-lg text-xs font-bold text-cyan-400 flex items-center gap-1.5 transition-colors cursor-pointer"
                >
                  {copiedKey === 'aab_cmd' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedKey === 'aab_cmd' ? 'Copied' : 'Copy Command'}</span>
                </button>
              </div>

              <pre className="p-3.5 bg-[#070F1B] rounded-xl border border-[#1E3E62] font-mono text-[11px] text-cyan-300 overflow-x-auto">
                cd android && ./gradlew bundleRelease
              </pre>
              <p className="text-[11px] text-slate-400">
                Output bundle file: <code className="text-slate-200">android/app/build/outputs/bundle/release/app-release.aab</code>
              </p>
            </div>
          </div>
        )}

        {/* TAB 2: STORE LISTING COPY */}
        {activeTab === 'metadata' && (
          <div className="space-y-5">
            {/* App Title */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-300">
                  App Title (Max 30 chars • Currently {APP_TITLE.length})
                </label>
                <button
                  onClick={() => copyToClipboard(APP_TITLE, 'title')}
                  className="text-xs font-bold text-[#D4AF37] hover:text-[#F3C64F] flex items-center gap-1 cursor-pointer"
                >
                  {copiedKey === 'title' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedKey === 'title' ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
              <input
                type="text"
                readOnly
                value={APP_TITLE}
                className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3.5 py-2.5 text-sm font-bold text-white outline-none"
              />
            </div>

            {/* Short Description */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-300">
                  Short Description (Max 80 chars • Currently {SHORT_DESC.length})
                </label>
                <button
                  onClick={() => copyToClipboard(SHORT_DESC, 'short')}
                  className="text-xs font-bold text-[#D4AF37] hover:text-[#F3C64F] flex items-center gap-1 cursor-pointer"
                >
                  {copiedKey === 'short' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedKey === 'short' ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
              <input
                type="text"
                readOnly
                value={SHORT_DESC}
                className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3.5 py-2.5 text-sm text-slate-200 outline-none"
              />
            </div>

            {/* Full Description */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-300">
                  Full Store Description (Formatted for Play Store)
                </label>
                <button
                  onClick={() => copyToClipboard(FULL_DESC, 'full')}
                  className="text-xs font-bold text-[#D4AF37] hover:text-[#F3C64F] flex items-center gap-1 cursor-pointer"
                >
                  {copiedKey === 'full' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedKey === 'full' ? 'Copied' : 'Copy Full Text'}</span>
                </button>
              </div>
              <textarea
                readOnly
                rows={12}
                value={FULL_DESC}
                className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl p-3.5 text-xs text-slate-300 font-mono leading-relaxed outline-none"
              />
            </div>

            {/* Store Listing Details Table */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
              <h4 className="font-bold text-white text-sm">Classification & Policy Settings</h4>
              <div className="divide-y divide-[#1E3E62] text-xs">
                <div className="py-2 flex justify-between">
                  <span className="text-slate-400">Category</span>
                  <span className="text-white font-bold">Education / Books & Reference</span>
                </div>
                <div className="py-2 flex justify-between">
                  <span className="text-slate-400">Content Rating</span>
                  <span className="text-white font-bold">Everyone (PEGI 3 / ESRB Everyone)</span>
                </div>
                <div className="py-2 flex justify-between">
                  <span className="text-slate-400">Target Audience</span>
                  <span className="text-white font-bold">18 and over (Seafarers & Maritime Officers)</span>
                </div>
                <div className="py-2 flex justify-between">
                  <span className="text-slate-400">Ads</span>
                  <span className="text-emerald-400 font-bold">No Ads</span>
                </div>
                <div className="py-2 flex justify-between">
                  <span className="text-slate-400">User Data Collected</span>
                  <span className="text-emerald-400 font-bold">None (100% on-device local storage)</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: GRAPHICS */}
        {activeTab === 'graphics' && (
          <div className="space-y-5">
            {/* Feature Graphic Banner */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-white text-sm">
                    Google Play Feature Graphic (1024 x 500 px)
                  </h4>
                  <p className="text-[11px] text-slate-300">
                    Required banner image displayed at the top of your Google Play Store listing.
                  </p>
                </div>
                <button
                  onClick={handleDownloadBanner}
                  className="py-2 px-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black text-xs rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Download Banner (PNG)</span>
                </button>
              </div>

              <div className="w-full overflow-hidden rounded-xl border border-[#D4AF37]/40 shadow-lg">
                <canvas ref={canvasRef} className="w-full h-auto block" />
              </div>
            </div>

            {/* App Icon */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-white text-sm">Google Play High-Res App Icon (512 x 512 px)</h4>
                  <p className="text-[11px] text-slate-300">
                    High-resolution 32-bit PNG with alpha channel required for store submission.
                  </p>
                </div>
                <a
                  href="/logo.png"
                  download="playstore-icon-512x512.png"
                  className="py-2 px-3 bg-[#0B192C] hover:bg-[#1E3E62] border border-[#D4AF37]/50 text-[#D4AF37] font-bold text-xs rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Download Icon</span>
                </a>
              </div>

              <div className="flex items-center gap-4">
                <img
                  src="/logo.png"
                  alt="App Icon"
                  className="w-24 h-24 rounded-2xl border-2 border-[#D4AF37] shadow-lg object-cover"
                />
                <div className="text-xs text-slate-300 space-y-1">
                  <p className="font-bold text-white">Hyperion Golden Compass Emblem</p>
                  <p className="text-slate-400">Dimensions: 512 x 512 px (PNG)</p>
                  <p className="text-slate-400">Maskable Adaptive Icon compliant</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: PUBLISHING GUIDE */}
        {activeTab === 'guide' && (
          <div className="space-y-4">
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Award className="w-5 h-5 text-[#D4AF37]" />
                <span>6-Step Google Play Console Launch Guide</span>
              </h3>

              <div className="space-y-3.5 text-xs text-slate-300">
                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      1
                    </span>
                    <span>Register Google Play Developer Account</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    Visit <strong className="text-slate-200">play.google.com/console</strong> and pay the one-time $25 USD registration fee.
                  </p>
                </div>

                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      2
                    </span>
                    <span>Create Application Entry</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    Click <strong>Create app</strong>, name it <code className="text-amber-300">Hyperion: MARINA Reviewer</code>, select <strong>App</strong> and <strong>Free</strong>.
                  </p>
                </div>

                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      3
                    </span>
                    <span>Fill Main Store Listing</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    Copy the Short Description, Full Description, and upload the 512x512 icon and 1024x500 feature graphic from the <strong>Store Listing Copy</strong> and <strong>Graphics</strong> tabs above.
                  </p>
                </div>

                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      4
                    </span>
                    <span>Complete Policy Declarations</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    Under <strong>Policy &gt; App Content</strong>, complete: Privacy Policy (link to offline policy), Content Rating (Everyone), Target Audience (18+), and Data Safety (No user data collected).
                  </p>
                </div>

                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      5
                    </span>
                    <span>Build & Upload Android App Bundle (.aab)</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    Extract the Android Studio Project ZIP, open in Android Studio, select <strong className="text-slate-200">Build &gt; Generate Signed Bundle / APK</strong>, and upload the generated <code className="text-cyan-300">app-release.aab</code> to Google Play Console.
                  </p>
                </div>

                <div className="p-3.5 bg-[#0B192C] rounded-xl border border-[#1E3E62] space-y-1">
                  <div className="flex items-center gap-2 font-bold text-white">
                    <span className="w-5 h-5 rounded-full bg-[#D4AF37] text-[#070F1B] flex items-center justify-center text-xs">
                      6
                    </span>
                    <span>Closed Testing (20 Testers) & Production Rollout</span>
                  </div>
                  <p className="text-slate-400 pl-7">
                    For personal accounts, invite 20 fellow maritime cadets/officers to test on the closed testing track for 14 days, then promote to full Production release!
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};
