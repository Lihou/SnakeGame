package com.lihou.snakegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AndroidUiDispatcher.Companion.Main
import app.cash.molecule.launchMolecule
import com.lihou.snakegame.Event.DownEvent
import com.lihou.snakegame.Event.LeftEvent
import com.lihou.snakegame.Event.RightEvent
import com.lihou.snakegame.Event.StartEvent
import com.lihou.snakegame.Event.UpEvent
import com.lihou.snakegame.ui.theme.SnakeGameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {
  private val scope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val engine = SnakeGameEngine.create()

    val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)

    val onStart: () -> Unit = { events.tryEmit(StartEvent) }
    val onStop: () -> Unit = { events.tryEmit(Event.StopEvent) }
    val onUp: () -> Unit = { events.tryEmit(UpEvent) }
    val onDown: () -> Unit = { events.tryEmit(DownEvent) }
    val onLeft: () -> Unit = { events.tryEmit(LeftEvent) }
    val onRight: () -> Unit = { events.tryEmit(RightEvent) }

    val models: Flow<UiModel> = scope.launchMolecule {
      val gameModel = engine.present(events)
      UiModel(
        gameModel = gameModel,
        onStart = onStart,
        onStop = onStop,
        onUp = onUp,
        onDown = onDown,
        onLeft = onLeft,
        onRight = onRight
      )
    }

    setContent {
      SnakeGameTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
          SnakeGameUI(models)
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}