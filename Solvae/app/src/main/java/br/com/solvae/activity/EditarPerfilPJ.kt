package br.com.solvae.activity

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityEditarPerfilPjBinding // Importante: verifique se este nome está correto
import br.com.solvae.model.Empresa
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarPerfilPJ : AppCompatActivity() {

    // Se der erro nesta linha, apague e escreva novamente, deixando o Android completar o nome da classe
    private val binding by lazy { ActivityEditarPerfilPjBinding.inflate(layoutInflater) }
    private lateinit var empresaLogada: Empresa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val idUsuario = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE).getInt("ID_USUARIO", -1)

        if (idUsuario != -1) {
            buscarDadosEmpresa(idUsuario)
        }

        binding.btnSalvarEdicao.setOnClickListener {
            validarEAtualizar()
        }
    }

    private fun buscarDadosEmpresa(id: Int) {
        RetrofitClient.instancia.obterEmpresaPorId(id).enqueue(object : Callback<Empresa> {
            override fun onResponse(call: Call<Empresa>, response: Response<Empresa>) {
                if (response.isSuccessful && response.body() != null) {
                    empresaLogada = response.body()!!
                    preencherCampos(empresaLogada)
                }
            }
            override fun onFailure(call: Call<Empresa>, t: Throwable) {
                Toast.makeText(this@EditarPerfilPJ, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun preencherCampos(emp: Empresa) {
        binding.tietNome.setText(emp.nome)
        binding.tietEmail.setText(emp.login.email)
        binding.tietTelefone.setText(emp.telefone)
        binding.tietCep.setText(emp.cep)
        binding.tietRua.setText(emp.rua)
        binding.tietBairro.setText(emp.bairro)
        binding.tietCidade.setText(emp.cidade)
    }

    private fun validarEAtualizar() {
        val novaSenha = binding.tietNovaSenha.text.toString()
        val confSenha = binding.tietNovaSenhaconfirm.text.toString()

        if (novaSenha.isNotEmpty() || confSenha.isNotEmpty()) {
            if (novaSenha != confSenha) {
                binding.tietNovaSenhaconfirm.error = "As senhas não conferem"
                return
            }
        }

        val empresaAtualizada = Empresa(
            idEmpresa = empresaLogada.idEmpresa,
            nome = binding.tietNome.text.toString(),
            rua = binding.tietRua.text.toString(),
            cnae = empresaLogada.cnae,
            cnpj = empresaLogada.cnpj,
            razaoSocial = empresaLogada.razaoSocial,
            dataAbertura = empresaLogada.dataAbertura,
            capitalSocial = empresaLogada.capitalSocial,
            tipoEmpresa = empresaLogada.tipoEmpresa,
            statusEmp = empresaLogada.statusEmp,
            telefone = binding.tietTelefone.text.toString(),
            numero = empresaLogada.numero,
            bairro = binding.tietBairro.text.toString(),
            cidade = binding.tietCidade.text.toString(),
            uf = empresaLogada.uf,
            cep = binding.tietCep.text.toString(),
            usuarioLoginId = empresaLogada.usuarioLoginId,
            login = empresaLogada.login.copy(
                email = binding.tietEmail.text.toString(),
                senha = if (novaSenha.isNotEmpty()) novaSenha else empresaLogada.login.senha
            )
        )

        salvarNoServidor(empresaAtualizada)
    }

    private fun salvarNoServidor(emp: Empresa) {
        RetrofitClient.instancia.atualizarEmpresa(emp.idEmpresa!!, emp).enqueue(object : Callback<Empresa> {
            override fun onResponse(call: Call<Empresa>, response: Response<Empresa>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditarPerfilPJ, "Sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarPerfilPJ, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Empresa>, t: Throwable) {
                Toast.makeText(this@EditarPerfilPJ, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }
}