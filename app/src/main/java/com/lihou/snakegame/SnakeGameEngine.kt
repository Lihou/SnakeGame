package com.lihou.snakegame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lihou.snakegame.Event.DownEvent
import com.lihou.snakegame.Event.LeftEvent
import com.lihou.snakegame.Event.RightEvent
import com.lihou.snakegame.Event.StartEvent
import com.lihou.snakegame.Event.StopEvent
import com.lihou.snakegame.Event.UpEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Event {
  object StartEvent : Event()
  object StopEvent : Event()
  object UpEvent : Event()
  object DownEvent : Event()
  object LeftEvent : Event()
  object RightEvent : Event()
}

enum class Direction {
  RIGHT,
  LEFT,
  UP,
  DOWN
}

enum class RunState {
  RUNNING,
  STOP
}

data class Position(val x: Int, val y: Int)

private fun updateFoodPosition(snakeHead: Position, snakeBody: List<Position>): Position {
  val snake = snakeBody + snakeHead
  var food: Position
  do {
    food = Position(
      x = (0 until NUMBER_OF_GRIDS_PER_SIDE).random(),
      y = (0 until NUMBER_OF_GRIDS_PER_SIDE).random()
    )
  } while (food in snake)
  return food
}

data class SnakeGameModel(
  val snakeHead: Position = initSnakeHead,
  val snakeBody: List<Position> = initSnakeBody,
  val food: Position = updateFoodPosition(snakeHead, snakeBody)
)

const val NUMBER_OF_GRIDS_PER_SIDE = 20
private const val SNAKE_SPEED_DEFAULT = 400L

private val initSnakeBody = listOf(Position(0, 0), Position(1, 0))
private val initSnakeHead = Position(2, 0)

interface SnakeGameEngine {
  @Composable
  fun present(events: Flow<Event>): SnakeGameModel

  companion object {
    fun create(): SnakeGameEngine = SnakeGameEngineImpl()
  }
}

private class SnakeGameEngineImpl : SnakeGameEngine {
  private var runState = RunState.RUNNING
  private var direction = Direction.RIGHT

  private fun isEaten(snakeHead: Position, food: Position): Boolean = snakeHead == food

  private fun isSnakeHitWall(snakeHead: Position) = snakeHead.x < 0
    || snakeHead.x >= NUMBER_OF_GRIDS_PER_SIDE
    || snakeHead.y < 0
    || snakeHead.y >= NUMBER_OF_GRIDS_PER_SIDE

  private fun isSnakeHitBody(snakeBody: List<Position>, snakeHead: Position) =
    snakeBody.take(snakeBody.size - 1).any {
      snakeHead.x == it.x && snakeHead.y == it.y
    }

  private fun Position.step(): Position = when (direction) {
    Direction.RIGHT -> copy(x = x + 1)
    Direction.LEFT -> copy(x = x - 1)
    Direction.UP -> copy(y = y - 1)
    Direction.DOWN -> copy(y = y + 1)
  }

  private val oppositeDirection = mapOf(
    Direction.UP to Direction.DOWN,
    Direction.DOWN to Direction.UP,
    Direction.RIGHT to Direction.LEFT,
    Direction.LEFT to Direction.RIGHT
  )

  private fun Direction.changeTo(newDirection: Direction): Direction {
    return when (newDirection) {
      oppositeDirection[this] -> this
      else -> newDirection
    }
  }

  @Composable override fun present(events: Flow<Event>): SnakeGameModel {
    var snakeHead by remember { mutableStateOf(initSnakeHead) }
    var snakeBody by remember { mutableStateOf(initSnakeBody) }
    var food by remember { mutableStateOf(updateFoodPosition(snakeHead, snakeBody)) }

    fun updateModel() {
      val oldHead = snakeHead

      snakeHead = snakeHead.step()

      val isEaten = isEaten(snakeHead, food)

      snakeBody = when {
        isEaten -> snakeBody + oldHead
        else -> snakeBody.takeLast(snakeBody.size - 1) + oldHead
      }

      if (isEaten) {
        food = updateFoodPosition(snakeHead, snakeBody)
      }

      if (isSnakeHitWall(snakeHead) || isSnakeHitBody(snakeBody, snakeHead)) {
        snakeBody = initSnakeBody
        snakeHead = initSnakeHead
        direction = Direction.RIGHT
      }
    }

    LaunchedEffect(events) {
      events.collectLatest { event ->
        when (event) {
          StartEvent -> runState = RunState.RUNNING
          StopEvent -> runState = RunState.STOP
          UpEvent -> direction = direction.changeTo(Direction.UP)
          DownEvent -> direction = direction.changeTo(Direction.DOWN)
          LeftEvent -> direction = direction.changeTo(Direction.LEFT)
          RightEvent -> direction = direction.changeTo(Direction.RIGHT)
        }
      }
    }

    LaunchedEffect(Unit) {
      launch(Dispatchers.Default) {
        while (true) {
          delay(SNAKE_SPEED_DEFAULT)

          when (runState) {
            RunState.RUNNING -> updateModel()
            RunState.STOP -> Unit // no-op
          }
        }
      }
    }

    return SnakeGameModel(snakeHead, snakeBody, food)
  }
}