package com.devplanet.myavatar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val avatarArray: Array<Int> = arrayOf(
                R.drawable.avatar_1,
                R.drawable.avatar_2,
                R.drawable.avatar_3,
                R.drawable.avatar_4,
        )
        ivAvatar.setImageResource(R.drawable.avatar_1)
        genrateButton.setOnClickListener {
            ivAvatar.setImageResource(avatarArray[(0..3).random()])
        }
    }
    private fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) + start
}