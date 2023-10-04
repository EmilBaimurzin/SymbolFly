package com.fly.game.ui.game1

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fly.game.R
import com.fly.game.core.library.GameFragment
import com.fly.game.databinding.FrgmentGame1Binding
import com.fly.game.domain.SP
import com.fly.game.ui.dialogs.DialogResult
import com.fly.game.ui.other.MainActivity
import kotlinx.coroutines.launch

class FragmentGame1 : GameFragment<FrgmentGame1Binding>(FrgmentGame1Binding::inflate) {
    private val viewModel: Game1ViewModel by viewModels()
    private val sp by lazy {
        SP(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setJoystick()

        binding.shoot.setOnClickListener {
            viewModel.shot(
                binding.player.height,
                binding.player.width,
                dpToPx(20)
            )
        }

        binding.close.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.menu.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.restart.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
            (requireActivity() as MainActivity).navigate(FragmentGame1())
        }

        binding.coontinue.setOnClickListener {
            viewModel.pauseState = false
            viewModel.changePauseState()
            viewModel.start(
                xy.y.toInt(),
                xy.x.toInt(),
                binding.player.height,
                binding.player.width,
                dpToPx(80),
                dpToPx(80),
                dpToPx(20),
                dpToPx(20),
                dpToPx(40)
            )
        }

        binding.pause.setOnClickListener {
            viewModel.changePauseState()
            viewModel.stop()
            viewModel.pauseState = true
        }

        viewModel.playerXY.observe(viewLifecycleOwner) {
            binding.player.x = it.x
            binding.player.y = it.y
        }

        viewModel.scores.observe(viewLifecycleOwner) {
            binding.score.text = it.toString()
        }

        viewModel.pause.observe(viewLifecycleOwner) {
            binding.pauseLayout.isVisible = it
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.livesLayout.removeAllViews()
            repeat(it) {
                val liveView = ImageView(requireContext())
                liveView.layoutParams = LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)).apply {
                    marginStart = dpToPx(2)
                    marginEnd = dpToPx(2)
                }
                liveView.setImageResource(R.drawable.live)
                binding.livesLayout.addView(liveView)
            }

            if (it == 0 && viewModel.gameState && !viewModel.pauseState) {
                viewModel.stop()
                viewModel.gameState = false
                if (sp.getBestScore(1) < viewModel.scores.value!!) {
                    sp.setBestScore(1, viewModel.scores.value!!)
                }
                (requireActivity() as MainActivity).navigateToDialog(DialogResult().apply {
                    arguments = Bundle().apply {
                        putInt("SCORE", viewModel.scores.value!!)
                        putInt("GAME", 1)
                    }
                })
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger1.collect {
                    val enemies = viewModel.enemies.value
                    binding.enemyLayout.removeAllViews()
                    enemies.forEach { enemy ->
                        val enemyView = ImageView(requireContext())
                        enemyView.layoutParams = ViewGroup.LayoutParams(dpToPx(80), dpToPx(80))
                        enemyView.setImageResource(R.drawable.enemy)
                        enemyView.x = enemy.x
                        enemyView.y = enemy.y
                        binding.enemyLayout.addView(enemyView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger2.collect {
                    val bullets = viewModel.bullets.value
                    binding.bulletsLayout.removeAllViews()
                    bullets.forEach { bullet ->
                        val bulletView = ImageView(requireContext())
                        bulletView.layoutParams = ViewGroup.LayoutParams(dpToPx(20), dpToPx(20))
                        bulletView.setImageResource(R.drawable.bullet)
                        bulletView.x = bullet.x
                        bulletView.y = bullet.y
                        binding.bulletsLayout.addView(bulletView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger3.collect {
                    val symbols = viewModel.symbols.value
                    binding.symbolsLayout.removeAllViews()
                    symbols.forEach { symbol ->
                        val symbolView = ImageView(requireContext())
                        symbolView.layoutParams = ViewGroup.LayoutParams(dpToPx(40), dpToPx(40))
                        symbolView.setImageResource(R.drawable.game01_plus5)
                        symbolView.x = symbol.x
                        symbolView.y = symbol.y
                        binding.symbolsLayout.addView(symbolView)
                    }
                }
            }
        }

        startAction = {
            if (viewModel.gameState && !viewModel.pauseState) {
                if (viewModel.playerXY.value!!.x == 0F) {
                    viewModel.initPlayer(dpToPx(40).toFloat(), xy.y / 2 - binding.player.height / 2)
                }
                viewModel.start(
                    xy.y.toInt(),
                    xy.x.toInt(),
                    binding.player.height,
                    binding.player.width,
                    dpToPx(80),
                    dpToPx(80),
                    dpToPx(20),
                    dpToPx(20),
                    dpToPx(40)
                )
            }
        }
    }

    private fun setJoystick() {
        binding.joystick.setOnMoveListener { angle, strength ->
            viewModel.resetMove()
            if (strength == 0) {
                viewModel.resetMove()
            } else {
                when (angle) {
                    in 0..30 -> {
                        viewModel.resetMove()
                        viewModel.isGoingRight = true
                    }

                    in 31..60 -> {
                        viewModel.resetMove()
                        viewModel.isGoingRight = true
                        viewModel.isGoingUp = true
                    }

                    in 61..90 -> {
                        viewModel.resetMove()
                        viewModel.isGoingUp = true
                    }

                    in 91..120 -> {
                        viewModel.resetMove()
                        viewModel.isGoingUp = true
                    }

                    in 121..150 -> {
                        viewModel.resetMove()
                        viewModel.isGoingUp = true
                        viewModel.isGoingLeft = true
                    }

                    in 151..180 -> {
                        viewModel.resetMove()
                        viewModel.isGoingLeft = true
                    }

                    in 181..210 -> {
                        viewModel.resetMove()
                        viewModel.isGoingLeft = true
                    }

                    in 211..240 -> {
                        viewModel.resetMove()
                        viewModel.isGoingLeft = true
                        viewModel.isGoingDown = true
                    }

                    in 241..270 -> {
                        viewModel.resetMove()
                        viewModel.isGoingDown = true
                    }

                    in 271..300 -> {
                        viewModel.resetMove()
                        viewModel.isGoingDown = true
                    }

                    in 301..330 -> {
                        viewModel.resetMove()
                        viewModel.isGoingDown = true
                        viewModel.isGoingRight = true
                    }

                    in 331..359 -> {
                        viewModel.resetMove()
                        viewModel.isGoingRight = true
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}