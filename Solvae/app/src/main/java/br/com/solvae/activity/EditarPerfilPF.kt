package br.com.solvae.activity

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityEditarPerfilPfBinding
import br.com.solvae.model.PessoaFisica
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarPerfilPF : AppCompatActivity() {

    private val binding by lazy { ActivityEditarPerfilPfBinding.inflate(layoutInflater) }
    private lateinit var usuarioLogado: PessoaFisica

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Recupera ID do usuário logado
        val sharedPreferences = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
        val idUsuario = sharedPreferences.getInt("ID_USUARIO", -1)

        if (idUsuario != -1) {
            buscarDadosUsuario(idUsuario)
        } else {
            Toast.makeText(this, "Erro: Usuário não autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Botão para salvar alterações
        binding.btnSalvarEdicao.setOnClickListener {
            validarEAtualizar()
        }
    }

    private fun buscarDadosUsuario(id: Int) {
        RetrofitClient.instancia.obterPessoaFisicaPorId(id).enqueue(object : Callback<PessoaFisica> {
            override fun onResponse(call: Call<PessoaFisica>, response: Response<PessoaFisica>) {
                if (response.isSuccessful && response.body() != null) {
                    usuarioLogado = response.body()!!
                    preencherCampos(usuarioLogado)
                } else {
                    Toast.makeText(this@EditarPerfilPF, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PessoaFisica>, t: Throwable) {
                Toast.makeText(this@EditarPerfilPF, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun preencherCampos(pf: PessoaFisica) {
        binding.tietNome.setText(pf.nome)
        binding.tietEmail.setText(pf.login.email)
        binding.tietTelefone.setText(pf.telefone)
        binding.tietCep.setText(pf.cep)
        binding.tietRua.setText(pf.rua)
        binding.tietBairro.setText(pf.bairro)
        binding.tietCidade.setText(pf.cidade)
    }

    private fun validarEAtualizar() {
        val novaSenha = binding.tietNovaSenha.text.toString()
        val confSenha = binding.tietNovaSenhaconfirm.text.toString()

        // Validação de senhas
        if (novaSenha.isNotEmpty() || confSenha.isNotEmpty()) {
            if (novaSenha != confSenha) {
                binding.tietNovaSenhaconfirm.error = "As senhas não conferem"
                return
            }
            if (novaSenha.length < 6) {
                binding.tietNovaSenha.error = "A senha deve ter pelo menos 6 caracteres"
                return
            }
        }

        // Montagem do objeto atualizado preservando dados imutáveis
        val pfAtualizado = PessoaFisica(
            idPf = usuarioLogado.idPf,
            nome = binding.tietNome.text.toString(),
            dataNasc = usuarioLogado.dataNasc, // Mantém original
            cep = binding.tietCep.text.toString(),
            rua = binding.tietRua.text.toString(),
            bairro = binding.tietBairro.text.toString(),
            cidade = binding.tietCidade.text.toString(),
            numero = usuarioLogado.numero,
            telefone = binding.tietTelefone.text.toString(),
            sexo = usuarioLogado.sexo,
            cpf = usuarioLogado.cpf, // Mantém original
            usuarioLoginId = usuarioLogado.usuarioLoginId,
            login = usuarioLogado.login.copy(
                email = binding.tietEmail.text.toString(),
                senha = if (novaSenha.isNotEmpty()) novaSenha else usuarioLogado.login.senha
            )
        )

        salvarNoServidor(pfAtualizado)
    }

    private fun salvarNoServidor(pf: PessoaFisica) {
        // Assume que idPf não é nulo por causa da busca inicial
        RetrofitClient.instancia.atualizarPessoaFisica(pf.idPf!!, pf).enqueue(object : Callback<PessoaFisica> {
            override fun onResponse(call: Call<PessoaFisica>, response: Response<PessoaFisica>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditarPerfilPF, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                    // Fecha a tela e retorna para a anterior (PerfilUsuario)
                    finish()
                } else {
                    Toast.makeText(this@EditarPerfilPF, "Erro ao salvar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PessoaFisica>, t: Throwable) {
                Toast.makeText(this@EditarPerfilPF, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }
}