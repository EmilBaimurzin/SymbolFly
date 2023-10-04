package com.fly.game.ui.game2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fly.game.domain.game2.PairsRepository
import com.ninja.game.domain.ninja_pairs.NinjaPairsItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Game2ViewModel: ViewModel() {
    private val repository = PairsRepository()
    private var gameScope = CoroutineScope(Dispatchers.Default)
    var gameState = true
    var winCallback: (() -> Unit)? = null
    var pauseState = false
    private val _list = MutableLiveData(
        repository.generateList()
    )
    val list: LiveData<List<NinjaPairsItem>> = _list

    private val _timer = MutableLiveData(60)
    val timer: LiveData<Int> = _timer

    private val _pause = MutableLiveData(false)
    val pause: LiveData<Boolean> = _pause

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    fun startTimer() {
        gameScope = CoroutineScope(Dispatchers.Default)
        gameScope.launch {
            while (true) {
                delay(1000)
                _timer.postValue(_timer.value!! - 1)
            }
        }
    }

    fun stopTimer() {
        gameScope.cancel()
    }

    fun changePauseState() {
        _pause.postValue(!_pause.value!!)
    }

    fun openItem(index: Int) {
        viewModelScope.launch {
            val newList = _list.value!!
            newList[index].openAnimation = true
            _list.postValue(newList)
            delay(410)
            newList[index].openAnimation = false
            newList[index].isOpened = true
            newList[index].lastOpened = true
            val filteredList = newList.filter { it.lastOpened }
            if (filteredList.size == 2) {
                val item1 = filteredList[0]
                val item2 = filteredList[1]
                if (item1.value == item2.value) {
                    newList.map {
                        it.lastOpened = false
                    }
                    _score.postValue(_score.value!! + 1)
                    _list.postValue(newList)
                    if (_list.value!!.find { !it.isOpened } == null) {
                        winCallback?.invoke()
                    }
                } else {

                    newList[newList.indexOf(item1)].closeAnimation = true
                    newList[newList.indexOf(item1)].lastOpened = false

                    newList[newList.indexOf(item2)].closeAnimation = true
                    newList[newList.indexOf(item2)].lastOpened = false

                    _list.postValue(newList)
                    delay(410)
                    newList[newList.indexOf(item1)].closeAnimation = false
                    newList[newList.indexOf(item1)].isOpened = false

                    newList[newList.indexOf(item2)].closeAnimation = false
                    newList[newList.indexOf(item2)].isOpened = false

                    _list.postValue(newList)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}
