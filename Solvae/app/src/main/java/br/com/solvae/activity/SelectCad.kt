package br.com.solvae.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.databinding.ActivitySelectCadBinding

class SelectCad : AppCompatActivity() {

    private val binding by lazy {
        ActivitySelectCadBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 1. Clique no Card de Empresa (Pessoa Jurídica)
        binding.cardEmpresa.setOnClickListener {
            // Abre a tela de cadastro de empresa (Ajuste o nome 'EmpresaCad' se for diferente no seu app)
            val intent = Intent(this, EmpresaCad::class.java).apply {
                putExtra("TIPO_USUARIO", "PJ")
            }
            startActivity(intent)
        }

        // 2. Clique no Card de Pessoa Física (Cliente)
        binding.cardPessoaFisica.setOnClickListener {
            // Abre a tela de cadastro de cliente que acabamos de arrumar
            val intent = Intent(this, ClienteCad::class.java).apply {
                putExtra("TIPO_USUARIO", "PF")
            }
            startActivity(intent)
        }
    }
}