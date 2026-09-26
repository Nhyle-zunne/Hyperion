import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { TrackSelectScreen } from './screens/TrackSelectScreen';
import { HomeScreen } from './screens/HomeScreen';
import { QuestionBankScreen } from './screens/QuestionBankScreen';
import { PracticeScreen } from './screens/PracticeScreen';
import {
  ExamSetupScreen,
  ExamRunnerScreen,
  ExamResultScreen,
  ExamReviewScreen
} from './screens/ExamScreens';
import { FavoritesScreen } from './screens/FavoritesScreen';
import { FlaggedScreen } from './screens/FlaggedScreen';
import { IncorrectScreen } from './screens/IncorrectScreen';
import { SearchScreen } from './screens/SearchScreen';
import { StatsScreen } from './screens/StatsScreen';
import { StudyTimerScreen } from './screens/StudyTimerScreen';
import { PlayStoreScreen } from './screens/PlayStoreScreen';
import { AdminScreen } from './screens/AdminScreen';
import { OngoingSessionDialog } from './components/OngoingSessionDialog';

const AppContent: React.FC = () => {
  const {
    currentScreen,
    activeRecordId,
    ongoingPrompt,
    dismissOngoingSessionPrompt,
    resumeSession,
    deleteSessionLog
  } = useApp();

  return (
    <div className="min-h-screen bg-[#070F1B] text-slate-100 font-sans antialiased">
      {/* Active Screen Transition */}
      {currentScreen === 'TrackSelect' && <TrackSelectScreen />}
      {currentScreen === 'Home' && <HomeScreen />}
      {currentScreen === 'QuestionBank' && <QuestionBankScreen />}
      {currentScreen === 'Practice' && <PracticeScreen />}
      {currentScreen === 'ExamSetup' && <ExamSetupScreen />}
      {currentScreen === 'ExamRunner' && <ExamRunnerScreen />}
      {currentScreen === 'ExamResult' && <ExamResultScreen recordId={activeRecordId} />}
      {currentScreen === 'ExamReview' && <ExamReviewScreen recordId={activeRecordId} />}
      {currentScreen === 'Favorites' && <FavoritesScreen />}
      {currentScreen === 'Flagged' && <FlaggedScreen />}
      {currentScreen === 'Incorrect' && <IncorrectScreen />}
      {currentScreen === 'Search' && <SearchScreen />}
      {currentScreen === 'Stats' && <StatsScreen />}
      {currentScreen === 'StudyTimer' && <StudyTimerScreen />}
      {currentScreen === 'PlayStore' && <PlayStoreScreen />}
      {currentScreen === 'Admin' && <AdminScreen />}

      {/* Ongoing Session Resume Prompt Modal */}
      {ongoingPrompt && (
        <OngoingSessionDialog
          prompt={ongoingPrompt}
          onDismiss={dismissOngoingSessionPrompt}
          onResume={(log) => {
            dismissOngoingSessionPrompt();
            resumeSession(log);
          }}
          onStartNew={() => {
            ongoingPrompt.onStartNew();
          }}
          onDeleteLog={(id) => {
            deleteSessionLog(id);
            dismissOngoingSessionPrompt();
          }}
        />
      )}
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
};

export default App;
