package br.com.solvae.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityClienteCadastroBinding
import br.com.solvae.model.CepResponse
import br.com.solvae.model.Login
import br.com.solvae.model.PessoaFisica
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClienteCad : AppCompatActivity() {

    private val binding by lazy {
        ActivityClienteCadastroBinding.inflate(layoutInflater)
    }

    private var tipoUsuario: String = "PF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        tipoUsuario = intent.getStringExtra("TIPO_USUARIO") ?: "PF"

        // Inicializa as funções de auxílio da tela e aplica as máscaras
        configurarCalendario()
        configurarBuscaCep()
        aplicarMascarasNosCampos()

        binding.btnCadastrar.setOnClickListener {
            executarCadastro()
        }
    }

    /**
     * Configura as máscaras para atualizarem o texto visual enquanto o usuário digita
     */
    private fun aplicarMascarasNosCampos() {
        binding.tietCpf.addTextChangedListener(criarMascara(binding.tietCpf, "###.###.###-##"))
        binding.tietTelefone.addTextChangedListener(criarMascara(binding.tietTelefone, "(##) #####-####"))
        binding.tietCep.addTextChangedListener(criarMascara(binding.tietCep, "#####-###"))
        binding.tietDataNascimento.addTextChangedListener(criarMascara(binding.tietDataNascimento, "##/##/####"))
    }

    /**
     * Escuta o campo de CEP e busca o endereço automaticamente ao digitar 8 números
     */
    private fun configurarBuscaCep() {
        binding.tietCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Remove a formatação visual (hífen) para validar o comprimento numérico puro
                val cep = s.toString().replace(Regex("[^0-9]"), "").trim()

                // O ViaCEP exige exatamente 8 dígitos
                if (cep.length == 8) {
                    buscarEnderecoPorCep(cep)
                }
            }
        })
    }

    /**
     * Faz a requisição na API do ViaCEP utilizando a nova estrutura de @Url da ApiService
     */
    private fun buscarEnderecoPorCep(cep: String) {
        val api = RetrofitClient.instancia

        // Passando a URL completa dinamicamente para respeitar a anotação @Url da interface
        api.buscarCep("https://viacep.com.br/ws/$cep/json/").enqueue(object : Callback<CepResponse> {
            override fun onResponse(call: Call<CepResponse>, response: Response<CepResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val dadosEndereco = response.body()!!

                    if (dadosEndereco.logradouro.isNullOrEmpty() && dadosEndereco.localidade.isNullOrEmpty()) {
                        Toast.makeText(this@ClienteCad, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Preenche os campos automaticamente
                    binding.tietRua.setText(dadosEndereco.logradouro)
                    binding.tietBairro.setText(dadosEndereco.bairro)

                    // Como seu modelo pede Cidade, podemos juntar com o Estado "Cidade - UF" ou usar só a cidade
                    binding.tietCidade.setText("${dadosEndereco.localidade} - ${dadosEndereco.uf}")

                    // Move o foco direto para o campo de Número, que o usuário precisa digitar
                    binding.tietNumero.requestFocus()

                } else {
                    Toast.makeText(this@ClienteCad, "Erro ao buscar CEP.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CepResponse>, t: Throwable) {
                Toast.makeText(this@ClienteCad, "Falha ao conectar para buscar CEP.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Configura o DatePickerDialog para abrir ao clicar no ícone ou na caixa de texto.
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

                    binding.tietDataNascimento.setText(dataFormatada)
                },
                calendarioAtual.get(Calendar.YEAR),
                calendarioAtual.get(Calendar.MONTH),
                calendarioAtual.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        binding.tilDataNascimento.setEndIconOnClickListener { abrirDatePicker() }
        binding.tietDataNascimento.setOnClickListener { abrirDatePicker() }
    }

    private fun executarCadastro() {
        var formularioValido = true

        // 1. Limpa os erros visuais antes de fazer a validação
        binding.tilNome.error = null
        binding.tilEmail.error = null
        binding.tilCpf.error = null
        binding.tilDataNascimento.error = null
        binding.tilTelefone.error = null
        binding.tilCep.error = null
        binding.tilNumero.error = null
        binding.tilSenha.error = null

        // 2. Coleta dos dados
        val nome = binding.tietNome.text.toString().trim()
        val email = binding.tietEmail.text.toString().trim()
        val senha = binding.tietSenha.text.toString().trim()

        val cpfComMascara = binding.tietCpf.text.toString().trim()
        val cpfPuro = cpfComMascara.replace(Regex("[^0-9]"), "")

        val dataNasc = binding.tietDataNascimento.text.toString().trim()
        val telefonePuro = binding.tietTelefone.text.toString().replace(Regex("[^0-9]"), "").trim()
        val cidade = binding.tietCidade.text.toString().trim()
        val bairro = binding.tietBairro.text.toString().trim()
        val rua = binding.tietRua.text.toString().trim()
        val numeroStr = binding.tietNumero.text.toString().trim()
        val cepPuro = binding.tietCep.text.toString().replace(Regex("[^0-9]"), "").trim()

        // 3. Validação dos campos obrigatórios e formatos

        if (nome.isEmpty()) { binding.tilNome.error = "Campo obrigatório"; formularioValido = false }

        // Validação de Email (vazio ou formato incorreto)
        if (email.isEmpty()) {
            binding.tilEmail.error = "Campo obrigatório"
            formularioValido = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Digite um formato de email válido"
            formularioValido = false
        }

        if (dataNasc.isEmpty()) { binding.tilDataNascimento.error = "Campo obrigatório"; formularioValido = false }
        if (telefonePuro.isEmpty()) { binding.tilTelefone.error = "Campo obrigatório"; formularioValido = false }
        if (cepPuro.isEmpty()) { binding.tilCep.error = "Campo obrigatório"; formularioValido = false }
        if (numeroStr.isEmpty()) { binding.tilNumero.error = "Campo obrigatório"; formularioValido = false }
        if (senha.isEmpty()) { binding.tilSenha.error = "Campo obrigatório"; formularioValido = false }

        // Validação do CPF (se estiver vazio OU inválido na matemática)
        if (cpfPuro.isEmpty()) {
            binding.tilCpf.error = "Campo obrigatório"
            formularioValido = false
        } else if (!validarCPF(cpfPuro)) {
            binding.tilCpf.error = "CPF inválido!"
            formularioValido = false
            Toast.makeText(this, "Por favor, digite um CPF válido.", Toast.LENGTH_LONG).show()
        }

        // Validação do RadioGroup (Gênero)
        if (binding.rgSexo.checkedRadioButtonId == -1) {
            formularioValido = false
            Toast.makeText(this, "Por favor, selecione o Gênero.", Toast.LENGTH_SHORT).show()
        }

        // Se encontrou algum erro, para a execução e não envia para a API
        if (!formularioValido) {
            Toast.makeText(this, "Verifique os campos obrigatórios em vermelho.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- DAQUI PARA BAIXO O CÓDIGO SÓ RODA SE TUDO ESTIVER PREENCHIDO CORRETAMENTE ---

        // Desativa o botão para bloquear clicks duplos/múltiplos na requisição assíncrona
        binding.btnCadastrar.isEnabled = false

        // Mapeamento do sexo após confirmar que algo foi selecionado
        val sexo = when (binding.rgSexo.checkedRadioButtonId) {
            binding.rbMasculino.id -> "Masculino"
            binding.rbFeminino.id -> "Feminino"
            binding.rbOutros.id -> "Outros"
            else -> "Não Informado"
        }

        val dadosLogin = Login(
            email = email,
            senha = senha,
            tipoUsuario = tipoUsuario
        )

        // Formata a data de dd/MM/yyyy para yyyy-MM-dd antes de mandar pro backend
        val dataNascFormatada = formatarDataParaAPI(dataNasc)

        val novaPf = PessoaFisica(
            nome = nome,
            dataNasc = dataNascFormatada,
            cep = cepPuro,
            rua = rua,
            bairro = bairro,
            cidade = cidade,
            numero = numeroStr.toIntOrNull(),
            telefone = telefonePuro,
            sexo = sexo,
            cpf = cpfPuro,
            usuarioLoginId = 0,
            login = dadosLogin
        )

        val api = RetrofitClient.instancia
        api.cadastrarPessoaFisica(novaPf).enqueue(object : Callback<PessoaFisica> {
            override fun onResponse(call: Call<PessoaFisica>, response: Response<PessoaFisica>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ClienteCad, "Cadastro de ${response.body()?.nome} realizado!", Toast.LENGTH_SHORT).show()

                    val intent = android.content.Intent(this@ClienteCad, LoginActv::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnCadastrar.isEnabled = true
                    android.util.Log.e("ERRO_API", "Código: ${response.code()} | Detalhe: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ClienteCad, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PessoaFisica>, t: Throwable) {
                binding.btnCadastrar.isEnabled = true
                android.util.Log.e("ERRO_CONEXAO", "Falha na requisição", t)
                Toast.makeText(this@ClienteCad, "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // =========================================================================
    // MOTORES AUXILIARES: Gerenciador de Máscaras e Validador Lógico do CPF
    // =========================================================================

    /**
     * Converte a data do formato BR (dd/MM/yyyy) para o formato internacional (yyyy-MM-dd)
     */
    private fun formatarDataParaAPI(dataBr: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val data = formatoEntrada.parse(dataBr)
            formatoSaida.format(data!!)
        } catch (e: Exception) {
            dataBr
        }
    }

    /**
     * Gerenciador dinâmico de máscaras rodando dentro do loop afterTextChanged.
     */
    private fun criarMascara(editText: com.google.android.material.textfield.TextInputEditText, mask: String): TextWatcher {
        return object : TextWatcher {
            private var isUpdating = false
            private var oldString = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val strPura = s.toString().replace(Regex("[^0-9]"), "")

                if (isUpdating || strPura == oldString) return

                isUpdating = true
                oldString = strPura

                val mascara = StringBuilder()
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        if (i < strPura.length) {
                            mascara.append(m)
                        }
                        continue
                    }
                    try {
                        mascara.append(strPura[i])
                        i++
                    } catch (e: Exception) {
                        break
                    }
                }

                if (s.toString() != mascara.toString()) {
                    editText.setText(mascara.toString())
                    editText.setSelection(mascara.length)
                }

                isUpdating = false
            }
        }
    }

    /**
     * Validador matemático oficial do dígito verificador de CPF
     */
    private fun validarCPF(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false

        return try {
            var soma = 0
            for (i in 0 until 9) {
                soma += cpf[i].toString().toInt() * (10 - i)
            }
            var resto = soma % 11
            val digito1 = if (resto < 2) 0 else 11 - resto

            soma = 0
            for (i in 0 until 10) {
                soma += cpf[i].toString().toInt() * (11 - i)
            }
            resto = soma % 11
            val digito2 = if (resto < 2) 0 else 11 - resto

            cpf[9].toString().toInt() == digito1 && cpf[10].toString().toInt() == digito2
        } catch (e: Exception) {
            false
        }
    }
}