package br.com.solvae.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityEmpresaCadBinding
import br.com.solvae.model.CepResponse
import br.com.solvae.model.Empresa
import br.com.solvae.model.Login
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EmpresaCad : AppCompatActivity() {

    private val binding by lazy {
        ActivityEmpresaCadBinding.inflate(layoutInflater)
    }

    private var tipoUsuario: String = "EMPRESA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        tipoUsuario = intent.getStringExtra("TIPO_USUARIO") ?: "EMPRESA"

        // Inicializa as funções do calendário, busca de CEP e aplica as máscaras nos inputs
        configurarCalendario()
        configurarBuscaCep()
        aplicarMascarasNosCampos()

        binding.btnCadastrar.setOnClickListener {
            executarCadastro()
        }
    }

    /**
     * Configura as máscaras para atualizarem o texto de forma limpa enquanto o usuário digita
     */
    private fun aplicarMascarasNosCampos() {
        binding.tietCnpj.addTextChangedListener(criarMascara(binding.tietCnpj, "##.###.###/####-##"))
        binding.tietTelefone.addTextChangedListener(criarMascara(binding.tietTelefone, "(##) #####-####"))
        binding.tietCep.addTextChangedListener(criarMascara(binding.tietCep, "#####-###"))
        binding.tietDataAbertura.addTextChangedListener(criarMascara(binding.tietDataAbertura, "##/##/####"))
    }

    /**
     * Escuta o campo de CEP e busca o endereço automaticamente ao digitar 8 números
     */
    private fun configurarBuscaCep() {
        binding.tietCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Remove a máscara hífen para fazer a contagem dos 8 números puros exigidos pelo ViaCEP
                val cep = s.toString().replace(Regex("[^0-9]"), "").trim()

                if (cep.length == 8) {
                    buscarEnderecoPorCep(cep)
                }
            }
        })
    }

    /**
     * Faz a requisição na API do ViaCEP usando a nova estrutura de @Url da ApiService
     */
    private fun buscarEnderecoPorCep(cep: String) {
        val api = RetrofitClient.instancia

        // CORREÇÃO: Passando a URL completa dinamicamente para respeitar a anotação @Url da interface
        api.buscarCep("https://viacep.com.br/ws/$cep/json/").enqueue(object : Callback<CepResponse> {
            override fun onResponse(call: Call<CepResponse>, response: Response<CepResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val dadosEndereco = response.body()!!

                    if (dadosEndereco.logradouro.isNullOrEmpty() && dadosEndereco.localidade.isNullOrEmpty()) {
                        Toast.makeText(this@EmpresaCad, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Preenche os campos automaticamente com os dados retornados
                    binding.tietRua.setText(dadosEndereco.logradouro)
                    binding.tietBairro.setText(dadosEndereco.bairro)
                    binding.tietCidade.setText(dadosEndereco.localidade)
                    binding.tietUf.setText(dadosEndereco.uf)

                    // Move o foco para o campo de número da empresa
                    binding.tietNumero.requestFocus()

                } else {
                    Toast.makeText(this@EmpresaCad, "Erro ao buscar CEP.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CepResponse>, t: Throwable) {
                Toast.makeText(this@EmpresaCad, "Falha ao conectar para buscar CEP.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Configura o DatePickerDialog para abrir ao interagir com o campo Data de Abertura
     */
    private fun configurarCalendario() {
        val abrirDatePicker = {
            val calendarioAtual = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    val dataSelecionada = Calendar.getInstance().apply {
                        set(Calendar.YEAR, ano)
                        set(Calendar.MONTH, mes)
                        set(Calendar.DAY_OF_MONTH, dia)
                    }

                    val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatador.format(dataSelecionada.time)

                    binding.tietDataAbertura.setText(dataFormatada)
                },
                calendarioAtual.get(Calendar.YEAR),
                calendarioAtual.get(Calendar.MONTH),
                calendarioAtual.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        try {
            binding.tilDataAbertura.setEndIconOnClickListener { abrirDatePicker() }
        } catch (e: Exception) {}

        binding.tietDataAbertura.setOnClickListener { abrirDatePicker() }
    }

    private fun executarCadastro() {
        val nome = binding.tietNome.text.toString().trim()
        val email = binding.tietEmail.text.toString().trim()
        val senha = binding.tietSenha.text.toString().trim()

        // Mantemos os valores puros sem formatação visual para salvar de forma limpa no Banco de Dados
        val cnpjComMascara = binding.tietCnpj.text.toString().trim()
        val cnpjPuro = cnpjComMascara.replace(Regex("[^0-9]"), "")

        val razaoSocial = binding.tietRazaoSocial.text.toString().trim()
        val tipoEmpresa = binding.tietTipoEmpresa.text.toString().trim()
        val cnaeStr = binding.tietCnae.text.toString().trim()
        val telefonePuro = binding.tietTelefone.text.toString().replace(Regex("[^0-9]"), "").trim()
        val uf = binding.tietUf.text.toString().trim()
        val city = binding.tietCidade.text.toString().trim()
        val bairro = binding.tietBairro.text.toString().trim()
        val rua = binding.tietRua.text.toString().trim()
        val numStr = binding.tietNumero.text.toString().trim()
        val cepPuro = binding.tietCep.text.toString().replace(Regex("[^0-9]"), "").trim()
        val dataAbertura = binding.tietDataAbertura.text.toString().trim()
        val capitalStr = binding.tietCapitalSocial.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpjComMascara.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // EXECUÇÃO DO VALIDADOR MATEMÁTICO DE CNPJ
        if (!validarCNPJ(cnpjPuro)) {
            binding.tietCnpj.error = "CNPJ inválido!"
            binding.tietCnpj.requestFocus()
            Toast.makeText(this, "Por favor, digite um CNPJ válido.", Toast.LENGTH_LONG).show()
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
            cnpj = cnpjPuro, // Envia limpo para a API
            razaoSocial = razaoSocial,
            dataAbertura = dataAbertura,
            capitalSocial = capitalStr.toDoubleOrNull() ?: 0.0,
            tipoEmpresa = tipoEmpresa,
            statusEmp = 1,
            telefone = telefonePuro, // Envia limpo para a API
            numero = numStr.toIntOrNull(),
            bairro = bairro,
            cidade = city,
            uf = uf,
            cep = cepPuro, // Envia limpo para a API
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

    // =========================================================================
    // MOTORES AUXILIARES: Gerenciador de Máscaras e Validador Lógico do CNPJ
    // =========================================================================

    /**
     * CORREÇÃO: Alinhado a tipagem do parâmetro para receber um TextInputEditText,
     * evitando conflitos de compilação com os listeners criados via ViewBinding.
     */
    private fun criarMascara(editText: com.google.android.material.textfield.TextInputEditText, mask: String): TextWatcher {
        return object : TextWatcher {
            var isUpdating: Boolean = false
            var oldText = ""

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[^0-9]"), "")
                var textComMascara = ""

                if (isUpdating) {
                    oldText = str
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#' && str.length > oldText.length) {
                        textComMascara += m
                        continue
                    }
                    try {
                        textComMascara += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                editText.setText(textComMascara)
                editText.setSelection(textComMascara.length)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    /**
     * Validador matemático oficial do dígito verificador do CNPJ brasileiro
     */
    private fun validarCNPJ(cnpj: String): Boolean {
        if (cnpj.length != 14 || cnpj.all { it == cnpj[0] }) return false

        try {
            val pesoDigito1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            val pesoDigito2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

            // Cálculo do Primeiro Dígito Verificador
            var soma = 0
            for (i in 0..11) {
                soma += cnpj[i].toString().toInt() * pesoDigito1[i]
            }
            var resto = soma % 11
            val digito1 = if (resto < 2) 0 else 11 - resto

            // Cálculo do Segundo Dígito Verificador
            soma = 0
            for (i in 0..12) {
                soma += cnpj[i].toString().toInt() * pesoDigito2[i]
            }
            resto = soma % 11
            val digito2 = if (resto < 2) 0 else 11 - resto

            return cnpj[12].toString().toInt() == digito1 && cnpj[13].toString().toInt() == digito2
        } catch (e: Exception) {
            return false
        }
    }
}