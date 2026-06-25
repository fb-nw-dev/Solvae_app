package br.com.solvae.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityRecallSenhaBinding // Ajuste se o nome do seu XML for diferente
import br.com.solvae.model.RecuperarSenhaRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecallSenha : AppCompatActivity() {

    private val binding by lazy {
        ActivityRecallSenhaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Botão de voltar do cabeçalho
        binding.voltar.setOnClickListener {
            finish() // Fecha a tela e volta para o Login
        }

        // Botão de Atualizar Senha
        binding.btnAtualizarSenha.setOnClickListener {
            val documentoDigitado = binding.tietCpf.text.toString().trim()
            val novaSenha = binding.tietSenhaRecall.text.toString().trim()

            // Validações básicas
            if (documentoDigitado.isEmpty() || novaSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var cpfEnvio = ""
            var cnpjEnvio = ""

            // Identifica se é CPF (11 números) ou CNPJ (14 números)
            if (documentoDigitado.length == 11) {
                cpfEnvio = documentoDigitado
            } else if (documentoDigitado.length == 14) {
                cnpjEnvio = documentoDigitado
            } else {
                Toast.makeText(this, "Documento inválido! Digite 11 números para CPF ou 14 para CNPJ.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Cria o objeto com o que foi descoberto
            val dadosRequisicao = RecuperarSenhaRequest(
                cpf = cpfEnvio,
                cnpj = cnpjEnvio,
                novaSenha = novaSenha
            )

            // Executa a chamada para o servidor
            enviarNovaSenhaParaServidor(dadosRequisicao)
        }
    }

    private fun enviarNovaSenhaParaServidor(dados: RecuperarSenhaRequest) {
        val api = RetrofitClient.instancia

        api.recuperarSenha(dados).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecallSenha, "Senha atualizada com sucesso!", Toast.LENGTH_LONG).show()
                    finish() // Fecha a tela e volta para o login com a senha nova funcional
                } else if (response.code() == 404) {
                    Toast.makeText(this@RecallSenha, "Documento não encontrado no sistema.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@RecallSenha, "Erro ao atualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RecallSenha, "Erro de conexão: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}