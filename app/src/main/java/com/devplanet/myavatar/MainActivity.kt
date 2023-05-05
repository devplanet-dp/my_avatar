package com.devplanet.myavatar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devplanet.myavatar.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val avatarArray: Array<Int> = arrayOf(
                R.drawable.avatar_1,
                R.drawable.avatar_2,
                R.drawable.avatar_3,
                R.drawable.avatar_4,
        )
        binding.ivAvatar.setImageResource(R.drawable.avatar_1)
        binding.genrateButton.setOnClickListener {
            binding.ivAvatar.setImageResource(avatarArray[(0..3).random()])
        }
    }
    private fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) + start
}