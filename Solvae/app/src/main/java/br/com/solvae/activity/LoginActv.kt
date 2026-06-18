package br.com.solvae.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityLoginActvBinding
import br.com.solvae.model.Login
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActv : AppCompatActivity() {

    private val binding by lazy { ActivityLoginActvBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Botão Entrar
        binding.btlog.setOnClickListener {
            fazerLogin()
        }

        // Botão Cadastro (se houver, adicione aqui o seu Intent)
        binding.tvcadastro.setOnClickListener {
            startActivity(Intent(this, SelectCad::class.java))
        }
    }

    private fun fazerLogin() {
        val email = binding.etLogin.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            return
        }

        // Criamos o objeto Login com senha vazia (ou como sua API exigir)
        // e o e-mail informado
        val dadosLogin = Login(
            email = email,
            senha = senha,
            tipoUsuario = "" // Opcional: dependendo da sua API, pode deixar vazio no envio
        )

        RetrofitClient.instancia.realizarLogin(dadosLogin).enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!

                    // Validação: Verifica se retornou um ID válido
                    if (usuario.idUsuarioLogin != null) {
                        // Salva os dados no SharedPreferences
                        val prefs = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putInt("ID_USUARIO", usuario.idUsuarioLogin)
                            putString("TIPO_USUARIO", usuario.tipoUsuario)
                            apply()
                        }

                        // Direciona para a tela de Perfil
                        startActivity(Intent(this@LoginActv, MenuServices::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActv, "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActv, "Erro de autenticação: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                Toast.makeText(this@LoginActv, "Falha na conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}