package br.com.solvae.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityClienteCadastroBinding // CORRIGIDO O IMPORT DO BINDING
import br.com.solvae.model.Login
import br.com.solvae.model.PessoaFisica
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClienteCad : AppCompatActivity() {

    // CORRIGIDO: Agora aponta para o XML correto (activity_cliente_cadastro.xml)
    private val binding by lazy {
        ActivityClienteCadastroBinding.inflate(layoutInflater)
    }

    private var tipoUsuario: String = "PF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Recebe o tipo da tela de seleção
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO") ?: "PF"

        binding.btnCadastrar.setOnClickListener {
            executarCadastro()
        }
    }

    private fun executarCadastro() {
        val nome = binding.tietNome.text.toString().trim()
        val email = binding.tietEmail.text.toString().trim()
        val senha = binding.tietSenha.text.toString().trim()
        val cpf = binding.tietCpf.text.toString().trim()
        val dataNasc = binding.tietDataNascimento.text.toString().trim()
        val telefone = binding.tietTelefone.text.toString().trim()
        val cidade = binding.tietCidade.text.toString().trim()
        val bairro = binding.tietBairro.text.toString().trim()
        val rua = binding.tietRua.text.toString().trim()
        val numeroStr = binding.tietNumero.text.toString().trim()
        val cep = binding.tietCep.text.toString().trim()

        val sexo = when (binding.rgSexo.checkedRadioButtonId) {
            binding.rbMasculino.id -> "Masculino"
            binding.rbFeminino.id -> "Feminino"
            else -> "Não Informado"
        }

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        val dadosLogin = Login(
            email = email,
            senha = senha,
            tipoUsuario = tipoUsuario
        )

        val novaPf = PessoaFisica(
            nome = nome,
            dataNasc = dataNasc,
            cep = cep,
            rua = rua,
            bairro = bairro,
            cidade = cidade,
            numero = numeroStr.toIntOrNull(),
            telefone = telefone,
            sexo = sexo,
            cpf = cpf,
            usuarioLoginId = 0,
            login = dadosLogin
        )

        val api = RetrofitClient.instancia
        api.cadastrarPessoaFisica(novaPf).enqueue(object : Callback<PessoaFisica> {
            override fun onResponse(call: Call<PessoaFisica>, response: Response<PessoaFisica>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ClienteCad, "Cadastro de ${response.body()?.nome} realizado!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ClienteCad, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PessoaFisica>, t: Throwable) {
                Toast.makeText(this@ClienteCad, "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}