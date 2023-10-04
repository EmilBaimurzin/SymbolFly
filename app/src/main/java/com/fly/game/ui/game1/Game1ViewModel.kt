package com.fly.game.ui.game1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fly.game.core.library.GameViewModel
import com.fly.game.core.library.XY
import com.fly.game.core.library.XYIMpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Game1ViewModel : GameViewModel() {
    var isGoingLeft = false
    var isGoingRight = false
    var isGoingUp = false
    var isGoingDown = false

    var isSpawningBullet = false
    var isSpawningEnemy = false
    var isSpawningSymbol = false

    private val _trigger1 = MutableStateFlow(false)
    val trigger1 = _trigger1.asStateFlow()

    private val _trigger2 = MutableStateFlow(false)
    val trigger2 = _trigger2.asStateFlow()

    private val _trigger3 = MutableStateFlow(false)
    val trigger3 = _trigger3.asStateFlow()


    private val _enemies = MutableStateFlow<List<XYIMpl>>(emptyList())
    val enemies = _enemies.asStateFlow()

    private val _bullets = MutableStateFlow<List<XYIMpl>>(emptyList())
    val bullets = _bullets.asStateFlow()

    private val _symbols = MutableStateFlow<List<XYIMpl>>(emptyList())
    val symbols = _symbols.asStateFlow()

    private val _scores = MutableLiveData(0)
    val scores: LiveData<Int> = _scores

    private val _lives = MutableLiveData(3)
    val lives: LiveData<Int> = _lives

    private val _pause = MutableLiveData(false)
    val pause: LiveData<Boolean> = _pause

    init {
        _playerXY.postValue(XYIMpl(0f, 0f))
    }

    fun changePauseState() {
        _pause.postValue(!_pause.value!!)
    }

    fun start(
        maxY: Int,
        maxX: Int,
        playerHeight: Int,
        playerWidth: Int,
        enemyHeight: Int,
        enemyWidth: Int,
        bulletHeight: Int,
        bulletWidth: Int,
        symbolSize: Int
    ) {
        isSpawningBullet = false
        isSpawningEnemy = false
        isSpawningSymbol = false
        gameScope = CoroutineScope(Dispatchers.Default)
        letPlayerMove(maxY, maxX, playerHeight, playerWidth)
        generateEnemies(maxY, enemyHeight, maxX)
        letEnemiesMove(enemyHeight, enemyWidth, bulletHeight, bulletWidth)
        letBulletsMove(bulletHeight, bulletWidth, playerHeight, playerWidth, maxX)
        generateSymbols(maxY, symbolSize, maxX)
        letSymbolsMove(symbolSize, playerHeight, playerWidth)
    }

    fun shot(
        playerHeight: Int,
        playerWidth: Int,
        bulletHeight: Int,
    ) {
        isSpawningBullet = true
        val currentList = _bullets.value.toMutableList()
        currentList.add(
            XYIMpl(
                _playerXY.value!!.x + playerWidth,
                _playerXY.value!!.y + ((playerHeight - bulletHeight) / 2)
            )
        )
        _bullets.value = currentList
        isSpawningBullet = false
    }

    private fun generateEnemies(maxY: Int, enemyHeight: Int, maxX: Int) {
        gameScope.launch {
            while (true) {
                delay(1500)
                isSpawningEnemy = true
                val currentList = _enemies.value.toMutableList()
                val y = (0..(maxY - enemyHeight)).random()
                currentList.add(XYIMpl(maxX.toFloat(), y.toFloat()))
                _enemies.value = currentList
                isSpawningEnemy = false
            }
        }
    }

    private fun generateSymbols(maxY: Int, symbolSize: Int, maxX: Int) {
        gameScope.launch {
            while (true) {
                delay(10000)
                isSpawningSymbol = true
                val currentList = _symbols.value.toMutableList()
                val y = (0..(maxY - symbolSize)).random()
                currentList.add(XYIMpl(maxX.toFloat(), y.toFloat()))
                _symbols.value = currentList
                isSpawningSymbol = false
            }
        }
    }

    private fun letPlayerMove(
        maxY: Int,
        maxX: Int,
        playerHeight: Int,
        playerWidth: Int,
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                val currentXY = _playerXY.value!!

                if (isGoingLeft && currentXY.x - 10f > 0) {
                    currentXY.x -= 10
                }

                if (isGoingRight && (currentXY.x + playerWidth) + 10f < maxX) {
                    currentXY.x += 10
                }

                if (isGoingUp && currentXY.y - 10f > 0) {
                    currentXY.y -= 10
                }

                if (isGoingDown && (currentXY.y + playerHeight) + 10f < maxY) {
                    currentXY.y += 10
                }
                _playerXY.postValue(currentXY)
            }
        }
    }

    private fun letEnemiesMove(
        enemyHeight: Int,
        enemyWidth: Int,
        bulletHeight: Int,
        bulletWidth: Int,
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                if (!isSpawningEnemy) {
                    _enemies.value = moveSomethingLeft(
                        enemyHeight,
                        enemyWidth,
                        bulletWidth,
                        bulletHeight,
                        _enemies.value.toMutableList(),
                        {
                            _scores.postValue(_scores.value!! + 1)
                        },
                        {
                            _lives.postValue(_lives.value!! - 1)
                        },
                        8,
                        _bullets.value
                    ) as List<XYIMpl>
                    _trigger1.value = !_trigger1.value
                }
            }
        }
    }

    private fun letSymbolsMove(
        symbolSize: Int,
        playerHeight: Int,
        playerWidth: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                if (!isSpawningSymbol) {
                    _symbols.value = moveSymbolsLeft(
                        symbolSize,
                        symbolSize,
                        playerWidth,
                        playerHeight,
                        _symbols.value.toMutableList(),
                        {
                            _scores.postValue(_scores.value!! + 5)
                        },
                        {},
                        20
                    ) as List<XYIMpl>
                    _trigger3.value = !_trigger3.value
                }
            }
        }
    }

    private suspend fun moveSymbolsLeft(
        objHeight: Int,
        objWidth: Int,
        playerWidth: Int,
        playerHeight: Int,
        oldList: MutableList<XY>,
        onIntersect: ((XY) -> Unit) = {},
        onOutOfScreen: ((XY) -> Unit) = {},
        distance: Int,
    ): MutableList<XY> {
        return suspendCoroutine { cn ->
            val newList = mutableListOf<XY>()
            oldList.forEach { obj ->
                if (obj.x >= 0f - objWidth) {
                    val objX = obj.x.toInt()..obj.x.toInt() + objWidth
                    val objY = obj.y.toInt()..obj.y.toInt() + objHeight
                    val playerX =
                        _playerXY.value!!.x.toInt().._playerXY.value!!.x.toInt() + playerWidth
                    val playerY =
                        _playerXY.value!!.y.toInt().._playerXY.value!!.y.toInt() + playerHeight
                    if (playerX.any { it in objX } && playerY.any { it in objY }) {
                        onIntersect.invoke(obj)
                    } else {
                        obj.y = obj.y
                        obj.x = obj.x - distance
                        newList.add(obj)
                    }

                } else {
                    onOutOfScreen.invoke(obj)
                }
            }
            cn.resume(newList)
        }
    }

    private fun letBulletsMove(
        bulletHeight: Int,
        bulletWidth: Int,
        playerHeight: Int,
        playerWidth: Int,
        maxX: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                if (!isSpawningBullet) {
                    _bullets.value = moveSomethingRight(
                        maxX,
                        bulletHeight,
                        bulletWidth,
                        playerWidth,
                        playerHeight,
                        _bullets.value.toMutableList(),
                        {},
                        {},
                        15
                    ) as List<XYIMpl>
                    _trigger2.value = !_trigger2.value
                }
            }
        }
    }

    fun resetMove() {
        isGoingUp = false
        isGoingDown = false
        isGoingLeft = false
        isGoingRight = false
    }

    fun initPlayer(x: Float, y: Float) {
        _playerXY.postValue(XYIMpl(x, y))
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}