package br.com.solvae.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.solvae.R
import br.com.solvae.databinding.ActivityMenuBarBinding

class MenuBar : AppCompatActivity() {

    private val binding by lazy {
        ActivityMenuBarBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}