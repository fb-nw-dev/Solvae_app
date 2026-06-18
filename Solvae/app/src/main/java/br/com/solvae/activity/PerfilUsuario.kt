package br.com.solvae.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityPerfilUsuarioBinding
import br.com.solvae.model.Empresa
import br.com.solvae.model.PessoaFisica
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class PerfilUsuario : AppCompatActivity() {

    private val binding by lazy { ActivityPerfilUsuarioBinding.inflate(layoutInflater) }
    private var tipoUsuario: String? = null
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val prefs = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
        idUsuario = prefs.getInt("ID_USUARIO", -1)
        tipoUsuario = prefs.getString("TIPO_USUARIO", null)

        // FEEDBACK DE DIAGNÓSTICO: Se os dados sumirem, esse Toast vai te dizer se o ID veio zerado/vazio do Login
        if (idUsuario == -1 || tipoUsuario.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: Usuário não identificado no SharedPreferences.", Toast.LENGTH_LONG).show()
        }

        binding.btnVoltarPerfil.setOnClickListener {
            val intent = Intent(this, MenuBar::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.btnIrParaEditar.setOnClickListener {
            val intent = if (tipoUsuario == "PF") {
                Intent(this, EditarPerfilPF::class.java)
            } else {
                Intent(this, EditarPerfilPJ::class.java)
            }
            // Passa o ID adiante para a tela de edição saber quem alterar
            intent.putExtra("ID_USUARIO", idUsuario)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (idUsuario != -1) {
            if (tipoUsuario == "PF") buscarPessoaFisica() else buscarEmpresa()
        }
    }

    private fun buscarPessoaFisica() {
        RetrofitClient.instancia.obterPessoaFisicaPorId(idUsuario).enqueue(object : Callback<PessoaFisica> {
            override fun onResponse(call: Call<PessoaFisica>, response: Response<PessoaFisica>) {
                if (response.isSuccessful && response.body() != null) {
                    preencherPF(response.body()!!)
                } else {
                    Toast.makeText(this@PerfilUsuario, "PF não encontrada. Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PessoaFisica>, t: Throwable) {
                Toast.makeText(this@PerfilUsuario, "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarEmpresa() {
        RetrofitClient.instancia.obterEmpresaPorId(idUsuario).enqueue(object : Callback<Empresa> {
            override fun onResponse(call: Call<Empresa>, response: Response<Empresa>) {
                if (response.isSuccessful && response.body() != null) {
                    preencherPJ(response.body()!!)
                } else {
                    Toast.makeText(this@PerfilUsuario, "Empresa não encontrada. Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Empresa>, t: Throwable) {
                Toast.makeText(this@PerfilUsuario, "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun preencherPF(pf: PessoaFisica) {
        binding.tvPerfilNome.text = pf.nome
        binding.tvPerfilDocumento.text = "CPF: ${pf.cpf}"
        binding.tvPerfilTelefone.text = "Telefone: ${pf.telefone}"
        binding.tvPerfilCidade.text = "Cidade: ${pf.cidade}"
        binding.tvPerfilCep.text = "CEP: ${pf.cep}"
        binding.tvPerfilRua.text = "Rua: ${pf.rua}"
        binding.tvPerfilBairro.text = "Bairro: ${pf.bairro}"
        binding.tvPerfilDataNasc.text = "Nascimento: ${pf.dataNasc}"
        binding.tvPerfilSexo.text = "Sexo: ${pf.sexo}"

        binding.tvPerfilDataNasc.visibility = View.VISIBLE
        binding.tvPerfilSexo.visibility = View.VISIBLE
        binding.tvPerfilRazaoSocial.visibility = View.GONE
        binding.tvPerfilCnae.visibility = View.GONE
        binding.tvPerfilUf.visibility = View.GONE
    }

    private fun preencherPJ(emp: Empresa) {
        binding.tvPerfilNome.text = emp.nome
        binding.tvPerfilDocumento.text = "CNPJ: ${emp.cnpj}"
        binding.tvPerfilTelefone.text = "Telefone: ${emp.telefone}"
        binding.tvPerfilCidade.text = "Cidade: ${emp.cidade}"
        binding.tvPerfilCep.text = "CEP: ${emp.cep}"
        binding.tvPerfilRua.text = "Rua: ${emp.rua}"
        binding.tvPerfilBairro.text = "Bairro: ${emp.bairro}"
        binding.tvPerfilRazaoSocial.text = "Razão Social: ${emp.razaoSocial}"
        binding.tvPerfilCnae.text = "CNAE: ${emp.cnae}"
        binding.tvPerfilUf.text = "UF: ${emp.uf}"

        // BÔNUS EXTRA: Formata o Capital Social recebido da API de volta para "R$ X.XXX,XX" na visualização
        try {
            val formatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(emp.capitalSocial)
            // Se você tiver um TextView para o capital social no XML, mude o ID abaixo para o correto:
            // binding.tvPerfilCapitalSocial.text = "Capital Social: $formatado"
        } catch (e: Exception) { e.printStackTrace() }

        binding.tvPerfilDataNasc.visibility = View.GONE
        binding.tvPerfilSexo.visibility = View.GONE
        binding.tvPerfilRazaoSocial.visibility = View.VISIBLE
        binding.tvPerfilCnae.visibility = View.VISIBLE
        binding.tvPerfilUf.visibility = View.VISIBLE
    }
}