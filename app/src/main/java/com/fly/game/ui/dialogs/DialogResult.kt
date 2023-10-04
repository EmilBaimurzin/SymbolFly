package com.fly.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.fly.game.R
import com.fly.game.core.library.ViewBindingDialog
import com.fly.game.databinding.DialogResultBinding
import com.fly.game.domain.SP
import com.fly.game.ui.game1.FragmentGame1
import com.fly.game.ui.game2.FragmentGame2
import com.fly.game.ui.other.MainActivity

class DialogResult : ViewBindingDialog<DialogResultBinding>(DialogResultBinding::inflate) {
    private val sp by lazy {
        SP(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                (requireActivity() as MainActivity).navigateBack("main")
                dialog?.cancel()
                true
            } else {
                false
            }
        }
        binding.menu.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack("main")
            dialog?.cancel()
        }

        binding.close.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack("main")
            dialog?.cancel()
        }
        binding.restart.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack("main")
            if (arguments?.getInt("GAME") == 1) {
                (requireActivity() as MainActivity).navigate(FragmentGame1())
            } else {
                (requireActivity() as MainActivity).navigate(FragmentGame2())
            }
            dialog?.cancel()
        }
        val score = arguments?.getInt("SCORE").toString()
        binding.score.text = score

        val bestScore = sp.getBestScore(arguments?.getInt("GAME")!!).toString()
        binding.bestScore.text = bestScore
    }
}