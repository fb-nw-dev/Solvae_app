package br.com.solvae.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.solvae.R
import br.com.solvae.databinding.ActivityMenuServicesBinding
import kotlin.getValue

class MenuSevices : AppCompatActivity() {

    private val binding by lazy {
        ActivityMenuServicesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}