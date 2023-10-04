package com.fly.game.ui.nav

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.fly.game.core.library.GameFragment
import com.fly.game.ui.game1.FragmentGame1
import com.fly.game.ui.game2.FragmentGame2
import com.fly.game.ui.other.MainActivity
import com.fly.game.databinding.FragmentNavBinding

class FragmentNav : GameFragment<FragmentNavBinding>(FragmentNavBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.game1.setOnClickListener {
            (requireActivity() as MainActivity).navigate(FragmentGame1())
        }

        binding.game2.setOnClickListener {
            (requireActivity() as MainActivity).navigate(FragmentGame2())
        }

        binding.exit.setOnClickListener {
            requireActivity().finish()
        }


        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}