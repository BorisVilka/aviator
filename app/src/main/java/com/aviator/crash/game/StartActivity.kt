package com.aviator.crash.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.aviator.crash.game.databinding.FragmentStartBinding

class StartActivity : Fragment() {
    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStartBinding.inflate(inflater,container,false)

        binding.button.setOnClickListener {
            val navController  = Navigation.findNavController(requireActivity(),
                R.id.fragmentContainerView
            )
            navController.navigate(R.id.fragmentGame, Bundle().apply {
            })
        }
        return binding.root
    }
}