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
        // ... dentro do onCreate da sua MenuBar.kt ...

        binding.voltar.setOnClickListener {
            val intent = Intent(this, MenuServices::class.java)
            // FLAG_ACTIVITY_CLEAR_TOP faz o app voltar para a MenuServices que já estava aberta,
            // em vez de criar uma nova cópia dela na pilha
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Fecha a MenuBar para não acumular memória
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