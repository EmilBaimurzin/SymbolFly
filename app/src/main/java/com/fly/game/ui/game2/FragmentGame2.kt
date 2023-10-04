package com.fly.game.ui.game2

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.fly.game.core.library.GameFragment
import com.fly.game.databinding.FragmentGame2Binding
import com.fly.game.domain.SP
import com.fly.game.domain.game2.PairsAdapter
import com.fly.game.ui.dialogs.DialogResult
import com.fly.game.ui.game1.FragmentGame1
import com.fly.game.ui.other.MainActivity

class FragmentGame2: GameFragment<FragmentGame2Binding>(FragmentGame2Binding::inflate) {
    private lateinit var pairsAdapter: PairsAdapter
    private val sp by lazy {
        SP(requireContext())
    }
    private val viewModel: Game2ViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        binding.close.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.menu.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.restart.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
            (requireActivity() as MainActivity).navigate(FragmentGame2())
        }

        binding.coontinue.setOnClickListener {
            viewModel.pauseState = false
            viewModel.changePauseState()
            viewModel.startTimer()
        }

        binding.pause.setOnClickListener {
            viewModel.changePauseState()
            viewModel.stopTimer()
            viewModel.pauseState = true
        }

        viewModel.winCallback = {
            end()
        }

        viewModel.list.observe(viewLifecycleOwner) {
            pairsAdapter.items = it.toMutableList()
            pairsAdapter.notifyDataSetChanged()
        }
        viewModel.timer.observe(viewLifecycleOwner) { totalSecs ->
            val minutes = (totalSecs % 3600) / 60;
            val seconds = totalSecs % 60;

            binding.time.text = String.format("%02d:%02d" ,minutes, seconds);
            if (totalSecs == 0 && viewModel.gameState && !viewModel.pauseState) {
                end()
            }
        }

        viewModel.score.observe(viewLifecycleOwner) {
            binding.score.text = it.toString()
        }

        viewModel.pause.observe(viewLifecycleOwner) {
            binding.pauseLayout.isVisible = it
        }

        if (viewModel.gameState && !viewModel.pauseState) {
            viewModel.startTimer()
        }
    }

    private fun end() {
        viewModel.stopTimer()
        viewModel.gameState = false
        if (sp.getBestScore(2) < viewModel.score.value!!) {
            sp.setBestScore(2, viewModel.score.value!!)
        }
        (requireActivity() as MainActivity).navigateToDialog(DialogResult().apply {
            arguments = Bundle().apply {
                putInt("SCORE", viewModel.score.value!!)
                putInt("GAME", 2)
            }
        })
    }

    private fun initAdapter() {
        pairsAdapter = PairsAdapter {
            if (viewModel.list.value!!.find { it.closeAnimation } == null && viewModel.list.value!!.find { it.openAnimation } == null) {
                viewModel.openItem(it)
            }
        }
        with(binding.gameRv) {
            adapter = pairsAdapter
            layoutManager = GridLayoutManager(requireContext(), 6).also {
                it.orientation = GridLayoutManager.VERTICAL
            }
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopTimer()
    }
}