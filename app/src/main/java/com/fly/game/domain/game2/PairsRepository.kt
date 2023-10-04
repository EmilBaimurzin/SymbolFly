package com.fly.game.domain.game2

import com.ninja.game.domain.ninja_pairs.NinjaPairsItem

class PairsRepository {
    fun generateList(): List<NinjaPairsItem> {
        val listToReturn = mutableListOf<NinjaPairsItem>()

        repeat(2) {
            repeat(12) { ind ->
                listToReturn.add(NinjaPairsItem(value = ind + 1))
            }
        }

        listToReturn.shuffle()
        return listToReturn
    }
}