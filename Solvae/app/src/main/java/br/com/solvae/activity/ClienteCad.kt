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
        val nome = binding.tietNome.text.toString().trim()
        val email = binding.tietEmail.text.toString().trim()
        val senha = binding.tietSenha.text.toString().trim()

        // Coleta o valor com máscara para validação de campos vazios
        val cpfComMascara = binding.tietCpf.text.toString().trim()
        // Limpa a string para validação matemática e envio limpo para a API
        val cpfPuro = cpfComMascara.replace(Regex("[^0-9]"), "")

        val dataNasc = binding.tietDataNascimento.text.toString().trim()
        val telefonePuro = binding.tietTelefone.text.toString().replace(Regex("[^0-9]"), "").trim()
        val cidade = binding.tietCidade.text.toString().trim()
        val bairro = binding.tietBairro.text.toString().trim()
        val rua = binding.tietRua.text.toString().trim()
        val numeroStr = binding.tietNumero.text.toString().trim()
        val cepPuro = binding.tietCep.text.toString().replace(Regex("[^0-9]"), "").trim()

        // Mapeamento atualizado incluindo o botão Novo (rbOutros)
        val sexo = when (binding.rgSexo.checkedRadioButtonId) {
            binding.rbMasculino.id -> "Masculino"
            binding.rbFeminino.id -> "Feminino"
            binding.rbOutros.id -> "Outros"
            else -> "Não Informado"
        }

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpfComMascara.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // EXECUÇÃO DO VALIDADOR MATEMÁTICO DE CPF
        if (!validarCPF(cpfPuro)) {
            binding.tietCpf.error = "CPF inválido!"
            binding.tietCpf.requestFocus()
            Toast.makeText(this, "Por favor, digite um CPF válido.", Toast.LENGTH_LONG).show()
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
            cep = cepPuro,          // Salva apenas números
            rua = rua,
            bairro = bairro,
            cidade = cidade,
            numero = numeroStr.toIntOrNull(),
            telefone = telefonePuro,  // Salva apenas números
            sexo = sexo,
            cpf = cpfPuro,            // Salva apenas números
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

    // =========================================================================
    // MOTORES AUXILIARES: Gerenciador de Máscaras e Validador Lógico do CPF
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
     * Validador matemático oficial do dígito verificador de CPF
     */
    private fun validarCPF(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false

        return try {
            // Validação do primeiro dígito
            var soma = 0
            for (i in 0 until 9) {
                soma += cpf[i].toString().toInt() * (10 - i)
            }
            var resto = soma % 11
            val digito1 = if (resto < 2) 0 else 11 - resto

            // Validação do segundo dígito
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