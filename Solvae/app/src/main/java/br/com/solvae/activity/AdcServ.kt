package br.com.solvae.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityAdcServBinding
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdcServ : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdcServBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Inicializa as funções de apoio e navegação da tela
        configurarDropdownTipoServico()
        aplicarMascaraMonetaria()
        configurarNavegacao()

        binding.btnCadastrar.setOnClickListener {
            executarCadastroServico()
        }
    }

    /**
     * Preenche o AutoCompleteTextView (Dropdown) com as categorias reais do seu app.
     */
    private fun configurarDropdownTipoServico() {
        // Nova lista com os serviços que você especificou:
        val tiposDeServico = arrayOf(
            "Informática e TI",
            "Elétrica",
            "Encanamento e Hidráulica",
            "Construção Civil e Reformas",
            "Estética e Beleza",
            "Mecânica Automotiva",
            "Gastronomia e Eventos",
            "Educação e Aulas",
            "Design e Multimídia",
            "Serviços Domésticos e Reformas Gerais",
            "Saúde e Bem-Estar",
            "Logística e Transporte"
        )

        // O próprio Android se encarrega de desenhar a lista com essa linha aqui:
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDeServico)
        binding.actvTipoServico.setAdapter(adapter)
    }

    /**
     * Aplica formatação monetária automática (R$) em tempo de execução no campo de valor
     */
    private fun aplicarMascaraMonetaria() {
        binding.tietValor.addTextChangedListener(object : TextWatcher {
            private var atual = ""

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != atual) {
                    binding.tietValor.removeTextChangedListener(this)

                    // Remove tudo que não for número inteiro
                    val limpo = s.toString().replace(Regex("[^0-9]"), "")

                    if (limpo.isNotEmpty()) {
                        val parsed = limpo.toDouble()
                        // Formata no padrão de moeda brasileira: R$ 0,00
                        val formatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed / 100)

                        atual = formatado
                        binding.tietValor.setText(formatado)
                        binding.tietValor.setSelection(formatado.length)
                    } else {
                        atual = ""
                        binding.tietValor.setText("")
                    }

                    binding.tietValor.addTextChangedListener(this)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Centraliza as ações de clique para a MenuBar e o BottomMenu utilizando as suas classes reais
     */
    private fun configurarNavegacao() {

        // --- MENU BAR SUPERIOR ---
        binding.menubar.setOnClickListener {
            val intent = Intent(this, MenuBar::class.java)
            startActivity(intent)
        }

        // --- BOTTOM MENU INFERIOR ---

        // Botão Serviços -> Abre MenuServices
        binding.btnServicos.setOnClickListener {
            val intent = Intent(this, MenuServices::class.java)
            startActivity(intent)
            finish()
        }

        // Botão Adicionar -> Já estamos nela
        binding.btnAdicionar.setOnClickListener {
            // Nenhuma ação necessária
        }

        // Botão Contratos -> Abre ServicosHistorico
        binding.btnContratos.setOnClickListener {
            val intent = Intent(this, ServicosHistorico::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Captura os dados, trata a string monetária e envia para a API do Retrofit
     */
    private fun executarCadastroServico() {
        val tipoServico = binding.actvTipoServico.text.toString().trim()
        val specialty = binding.tietEspecialidade.text.toString().trim()
        val descricao = binding.tietDescricao.text.toString().trim()
        val valorFormatado = binding.tietValor.text.toString().trim()

        if (tipoServico.isEmpty() || specialty.isEmpty() || valorFormatado.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // CONVERSÃO MONETÁRIA: Como o seu Model pede String, limpamos "R$ 1.250,50" para "1250.50"
        val valorLimpoStr = valorFormatado
            .replace(Regex("[^0-9,]"), "") // Remove símbolos e espaços
            .replace(",", ".")             // Ajusta para ponto decimal padrão americano/Kotlin

        // Obtém a data atual formatada como "yyyy-MM-dd" requerido pelo modelo
        val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        // 1. Instancia o objeto utilizando o mapeamento exato do seu Servico.kt
        val novoServico = Servico(
            tipoServ = tipoServico,
            Espec = specialty,
            valorServ = valorLimpoStr,
            statusServ = "Ativo",
            dtContratacao = dataAtual,
            descricao = descricao
            // Os outros campos opcionais já são preenchidos com null por padrão no Model
        )

        // 2. Chamada real do Retrofit
        val api = RetrofitClient.instancia

        api.criarServico(novoServico).enqueue(object : Callback<Servico> {
            override fun onResponse(call: Call<Servico>, response: Response<Servico>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdcServ, "Serviço cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Limpa os campos da tela após o sucesso para novos cadastros
                    binding.actvTipoServico.setText("")
                    binding.tietEspecialidade.setText("")
                    binding.tietValor.setText("")
                    binding.tietDescricao.setText("")
                } else {
                    Toast.makeText(this@AdcServ, "Erro ao cadastrar serviço: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Servico>, t: Throwable) {
                Toast.makeText(this@AdcServ, "Falha ao conectar com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}