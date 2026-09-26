package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.OngoingSessionDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ExamResultScreen
import com.example.ui.screens.ExamReviewScreen
import com.example.ui.screens.ExamRunnerScreen
import com.example.ui.screens.ExamSetupScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FlaggedScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncorrectScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.StudyTimerScreen
import com.example.ui.screens.TrackSelectScreen
import com.example.ui.theme.HyperionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HyperionTheme {
                val viewModel: MainViewModel = viewModel()
                HyperionApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HyperionApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                is AppScreen.TrackSelect -> TrackSelectScreen(viewModel = viewModel)
                is AppScreen.Home -> HomeScreen(viewModel = viewModel)
                is AppScreen.QuestionBank -> QuestionBankScreen(viewModel = viewModel)
                is AppScreen.Practice -> PracticeScreen(viewModel = viewModel, practiceArgs = screen)
                is AppScreen.ExamSetup -> ExamSetupScreen(viewModel = viewModel)
                is AppScreen.ExamRunner -> ExamRunnerScreen(viewModel = viewModel)
                is AppScreen.ExamResult -> ExamResultScreen(viewModel = viewModel, recordId = screen.recordId)
                is AppScreen.ExamReview -> ExamReviewScreen(viewModel = viewModel, recordId = screen.recordId)
                is AppScreen.Incorrect -> IncorrectScreen(viewModel = viewModel)
                is AppScreen.Favorites -> FavoritesScreen(viewModel = viewModel)
                is AppScreen.Flagged -> FlaggedScreen(viewModel = viewModel)
                is AppScreen.Search -> SearchScreen(viewModel = viewModel)
                is AppScreen.Stats -> StatsScreen(viewModel = viewModel)
                is AppScreen.StudyTimer -> StudyTimerScreen(viewModel = viewModel)
                is AppScreen.Admin -> AdminScreen(viewModel = viewModel)
                is AppScreen.EditQuestion -> AdminScreen(viewModel = viewModel)
            }
        }
    }

    val ongoingPrompt by viewModel.ongoingSessionPrompt.collectAsState()
    if (ongoingPrompt != null) {
        OngoingSessionDialog(
            prompt = ongoingPrompt!!,
            onDismiss = { viewModel.dismissOngoingSessionPrompt() },
            onResume = { log ->
                viewModel.dismissOngoingSessionPrompt()
                viewModel.resumeSession(log)
            },
            onStartNew = {
                val action = ongoingPrompt!!.onStartNew
                viewModel.dismissOngoingSessionPrompt()
                action()
            },
            onDeleteLog = { id ->
                viewModel.deleteSessionLog(id)
                viewModel.dismissOngoingSessionPrompt()
            }
        )
    }
}
