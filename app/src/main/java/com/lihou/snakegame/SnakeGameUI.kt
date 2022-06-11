package com.lihou.snakegame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihou.snakegame.ui.theme.Pink80
import com.lihou.snakegame.ui.theme.Purple80
import com.lihou.snakegame.ui.theme.PurpleGrey80
import com.lihou.snakegame.ui.theme.SnakeGameTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

enum class BoxType {
  HEAD,
  BODY,
  FOOD;
}

data class UiModel(
  val gameModel: SnakeGameModel = SnakeGameModel(),
  val onStart: () -> Unit = {},
  val onStop: () -> Unit = {},
  val onUp: () -> Unit = {},
  val onDown: () -> Unit = {},
  val onRight: () -> Unit = {},
  val onLeft: () -> Unit = {},
)

private val initialUiModel = UiModel()

private fun boxColor(boxType: BoxType) = when (boxType) {
  BoxType.FOOD -> Pink80
  BoxType.BODY -> PurpleGrey80
  BoxType.HEAD -> Purple80
}

@Composable fun SnakeGameUI(uiModel: Flow<UiModel>) {
  val model = uiModel.collectAsState(initial = initialUiModel)

  Column {
    ZoneUI(gameModel = model.value.gameModel)

    Box(
      Modifier
        .height(8.dp)
        .fillMaxWidth()
    )

    ControlsUI(
      onUp = model.value.onUp,
      onLeft = model.value.onLeft,
      onRight = model.value.onRight,
      onDown = model.value.onDown
    )
  }
}

@Composable fun ZoneUI(gameModel: SnakeGameModel) {
  Box(
    Modifier
      .padding(8.dp)
      .border(width = 1.dp, color = Color.Black)
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .padding(16.dp)
    ) {

      val diam = size.width / NUMBER_OF_GRIDS_PER_SIDE

      val boxSize = Size(diam - 2, diam - 2)

      fun drawBox(type: BoxType, position: Position) {
        drawRect(
          boxColor(type),
          Offset(position.x * diam + 2, position.y * diam - 2),
          boxSize
        )
      }

      // draw snake head
      drawBox(BoxType.HEAD, gameModel.snakeHead)

      // draw snake body
      gameModel.snakeBody.forEach {
        drawBox(BoxType.BODY, it)
      }

      // draw snake food
      drawBox(BoxType.FOOD, gameModel.food)
    }
  }
}

@Composable fun ControlsUI(
  onUp: () -> Unit,
  onLeft: () -> Unit,
  onRight: () -> Unit,
  onDown: () -> Unit
) {
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier
        .wrapContentSize()
        .background(MaterialTheme.colorScheme.secondary, CircleShape),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      val buttonSize = 50.dp
      IconButton(
        onClick = onUp
      ) {
        Icon(
          modifier = Modifier.size(buttonSize),
          imageVector = Icons.Default.KeyboardArrowUp,
          contentDescription = "up",
          tint = Color.White
        )
      }
      Row {
        IconButton(
          modifier = Modifier
            .padding(horizontal = 16.dp),
          onClick = onLeft
        ) {
          Icon(
            modifier = Modifier.size(buttonSize),
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "left",
            tint = Color.White
          )
        }
        IconButton(
          modifier = Modifier
            .padding(horizontal = 16.dp),
          onClick = onRight
        ) {
          Icon(
            modifier = Modifier.size(buttonSize),
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "right",
            tint = Color.White
          )
        }
      }
      IconButton(
        onClick = onDown
      ) {
        Icon(
          modifier = Modifier.size(buttonSize),
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "down",
          tint = Color.White
        )
      }
    }
  }
}

@Preview(showBackground = true) @Composable fun DefaultPreview() {
  SnakeGameTheme {
    SnakeGameUI(channelFlow { trySend(initialUiModel) })
  }
}
