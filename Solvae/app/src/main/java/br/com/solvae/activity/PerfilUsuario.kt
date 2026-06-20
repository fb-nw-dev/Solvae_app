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
import java.text.SimpleDateFormat
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

        // FORMATADO: CPF, Telefone e CEP com máscara na visualização
        binding.tvPerfilDocumento.text = "CPF: ${formatarCpf(pf.cpf)}"
        binding.tvPerfilTelefone.text = "Telefone: ${formatarTelefone(pf.telefone)}"
        binding.tvPerfilCidade.text = "Cidade: ${pf.cidade}"
        binding.tvPerfilCep.text = "CEP: ${formatarCep(pf.cep)}"

        binding.tvPerfilRua.text = "Rua: ${pf.rua}"
        binding.tvPerfilBairro.text = "Bairro: ${pf.bairro}"
        binding.tvPerfilDataNasc.text = "Nascimento: ${formatarData(pf.dataNasc)}"
        binding.tvPerfilSexo.text = "Sexo: ${pf.sexo}"

        binding.tvPerfilDataNasc.visibility = View.VISIBLE
        binding.tvPerfilSexo.visibility = View.VISIBLE
        binding.tvPerfilRazaoSocial.visibility = View.GONE
        binding.tvPerfilCnae.visibility = View.GONE
        binding.tvPerfilUf.visibility = View.GONE
    }

    private fun preencherPJ(emp: Empresa) {
        binding.tvPerfilNome.text = emp.nome

        // FORMATADO: CNPJ, Telefone e CEP com máscara na visualização
        binding.tvPerfilDocumento.text = "CNPJ: ${formatarCnpj(emp.cnpj)}"
        binding.tvPerfilTelefone.text = "Telefone: ${formatarTelefone(emp.telefone)}"
        binding.tvPerfilCidade.text = "Cidade: ${emp.cidade}"
        binding.tvPerfilCep.text = "CEP: ${formatarCep(emp.cep)}"

        binding.tvPerfilRua.text = "Rua: ${emp.rua}"
        binding.tvPerfilBairro.text = "Bairro: ${emp.bairro}"
        binding.tvPerfilRazaoSocial.text = "Razão Social: ${emp.razaoSocial}"
        binding.tvPerfilCnae.text = "CNAE: ${emp.cnae}"
        binding.tvPerfilUf.text = "UF: ${emp.uf}"

        try {
            val formatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(emp.capitalSocial)
            // binding.tvPerfilCapitalSocial.text = "Capital Social: $formatado"
        } catch (e: Exception) { e.printStackTrace() }

        binding.tvPerfilDataNasc.visibility = View.GONE
        binding.tvPerfilSexo.visibility = View.GONE
        binding.tvPerfilRazaoSocial.visibility = View.VISIBLE
        binding.tvPerfilCnae.visibility = View.VISIBLE
        binding.tvPerfilUf.visibility = View.VISIBLE
    }

    // --- FUNÇÕES DE FORMATAÇÃO (MÁSCARAS VISUAIS) ---

    private fun formatarData(dataRaw: String?): String {
        if (dataRaw.isNullOrEmpty() || dataRaw == "0000-00-00") return "Não informada"
        return try {
            val formatoOriginal = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatoDestino = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val dataParseada = formatoOriginal.parse(dataRaw)
            formatoDestino.format(dataParseada)
        } catch (e: Exception) { dataRaw }
    }

    private fun formatarCpf(cpfRaw: String?): String {
        if (cpfRaw.isNullOrEmpty()) return ""
        val digitos = cpfRaw.replace(Regex("[^0-9]"), "")
        return if (digitos.length == 11) {
            "${digitos.substring(0, 3)}.${digitos.substring(3, 6)}.${digitos.substring(6, 9)}-${digitos.substring(9)}"
        } else cpfRaw
    }

    private fun formatarCnpj(cnpjRaw: String?): String {
        if (cnpjRaw.isNullOrEmpty()) return ""
        val digitos = cnpjRaw.replace(Regex("[^0-9]"), "")
        return if (digitos.length == 14) {
            "${digitos.substring(0, 2)}.${digitos.substring(2, 5)}.${digitos.substring(5, 8)}/${digitos.substring(8, 12)}-${digitos.substring(12)}"
        } else cnpjRaw
    }

    private fun formatarTelefone(telRaw: String?): String {
        if (telRaw.isNullOrEmpty()) return ""
        val digitos = telRaw.replace(Regex("[^0-9]"), "")
        return when (digitos.length) {
            11 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}" // Celular: (XX) XXXXX-XXXX
            10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}" // Fixo: (XX) XXXX-XXXX
            else -> telRaw
        }
    }

    private fun formatarCep(cepRaw: String?): String {
        if (cepRaw.isNullOrEmpty()) return ""
        val digitos = cepRaw.replace(Regex("[^0-9]"), "")
        return if (digitos.length == 8) {
            "${digitos.substring(0, 5)}-${digitos.substring(5)}" // XXXXX-XXX
        } else cepRaw
    }
}