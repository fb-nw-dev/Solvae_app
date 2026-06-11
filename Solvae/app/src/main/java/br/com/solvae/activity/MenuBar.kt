package br.com.solvae.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.databinding.ActivityMenuBarBinding

class MenuBar : AppCompatActivity() {

    private val binding by lazy { ActivityMenuBarBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 1. Botão Voltar
        binding.voltar.setOnClickListener {
            finish()
        }

        // 2. Botão Perfil
        binding.tvMenuBarItem.setOnClickListener {
            val intent = Intent(this, PerfilUsuario::class.java)
            startActivity(intent)
            finish()
        }

        // 3. Botão Sair
        binding.tvSair.setOnClickListener {
            val prefs = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()


            val intent = Intent(this, LoginActv::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}