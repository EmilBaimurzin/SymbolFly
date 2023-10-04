package com.fly.game.domain

import android.content.Context

class SP(private val context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getBestScore(game: Int) = sp.getInt("BEST$game", 0)
    fun setBestScore(game: Int, score: Int) = sp.edit().putInt("BEST$game", score).apply()
}