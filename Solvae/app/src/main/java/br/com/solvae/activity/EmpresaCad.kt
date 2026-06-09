package br.com.solvae.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityEmpresaCadBinding
import br.com.solvae.model.Empresa
import br.com.solvae.model.Login
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpresaCad : AppCompatActivity() {

    private val binding by lazy {
        ActivityEmpresaCadBinding.inflate(layoutInflater)
    }

    private var tipoUsuario: String = "EMPRESA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        tipoUsuario = intent.getStringExtra("TIPO_USUARIO") ?: "EMPRESA"

        binding.btnCadastrar.setOnClickListener {
            executarCadastro()
        }
    }

    private fun executarCadastro() {
        val nome = binding.tietNome.text.toString().trim()
        val email = binding.tietEmail.text.toString().trim()
        val senha = binding.tietSenha.text.toString().trim()
        val cnpj = binding.tietCnpj.text.toString().trim()
        val razaoSocial = binding.tietRazaoSocial.text.toString().trim()
        val tipoEmpresa = binding.tietTipoEmpresa.text.toString().trim()
        val cnaeStr = binding.tietCnae.text.toString().trim()
        val telefone = binding.tietTelefone.text.toString().trim()
        val uf = binding.tietUf.text.toString().trim()
        val city = binding.tietCidade.text.toString().trim()
        val bairro = binding.tietBairro.text.toString().trim()
        val rua = binding.tietRua.text.toString().trim()
        val numStr = binding.tietNumero.text.toString().trim()
        val cep = binding.tietCep.text.toString().trim()
        val dataAbertura = binding.tietDataAbertura.text.toString().trim()
        val capitalStr = binding.tietCapitalSocial.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpj.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        val dadosLogin = Login(
            email = email,
            senha = senha,
            tipoUsuario = tipoUsuario
        )

        val novaEmpresa = Empresa(
            nome = nome,
            rua = rua,
            cnae = cnaeStr.toIntOrNull() ?: 0,
            cnpj = cnpj,
            razaoSocial = razaoSocial,
            dataAbertura = dataAbertura,
            capitalSocial = capitalStr.toDoubleOrNull() ?: 0.0,
            tipoEmpresa = tipoEmpresa,
            statusEmp = 1,
            telefone = telefone,
            numero = numStr.toIntOrNull(),
            bairro = bairro,
            cidade = city,
            uf = uf,
            cep = cep,
            usuarioLoginId = 0,
            login = dadosLogin
        )

        val api = RetrofitClient.instancia
        api.cadastrarEmpresa(novaEmpresa).enqueue(object : Callback<Empresa> {
            override fun onResponse(call: Call<Empresa>, response: Response<Empresa>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EmpresaCad, "Empresa ${response.body()?.nome} cadastrada!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EmpresaCad, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Empresa>, t: Throwable) {
                Toast.makeText(this@EmpresaCad, "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}