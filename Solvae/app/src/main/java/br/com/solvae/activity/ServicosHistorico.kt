package br.com.solvae.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.databinding.ActivityServicosHistoricoBinding

class ServicosHistorico : AppCompatActivity() {

    private val binding by lazy {
        ActivityServicosHistoricoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}