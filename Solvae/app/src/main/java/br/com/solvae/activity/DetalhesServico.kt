package br.com.solvae.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityDetalhesServicoBinding
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalhesServico : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetalhesServicoBinding.inflate(layoutInflater)
    }

    private var servicoSelecionado: Servico? = null
    private var valorUnitario: Double = 0.0
    private var idUsuarioLogado: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 1. Recuperar ID do Usuário Logado do SharedPreferences
        val sharedPreferences = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
        idUsuarioLogado = sharedPreferences.getInt("ID_USUARIO", -1)

        // 2. Recuperar o Serviço enviado do Histórico ou do Menu Principal
        servicoSelecionado = (intent.getSerializableExtra("SERVICO_SELECIONADO")
            ?: intent.getSerializableExtra("servico")) as? Servico

        if (servicoSelecionado != null) {
            preencherDadosDoServico()
            configurarCalculoTotal()
            configurarBotoesPorStatus()
        } else {
            Toast.makeText(this, "Erro ao carregar detalhes do serviço.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.menubar.setOnClickListener { finish() }
    }

    private fun preencherDadosDoServico() {
        servicoSelecionado?.let { servico ->
            binding.tvDetalheTipo.text = servico.tipoServ
            binding.tvDetalheEspec.text = servico.Espec
            binding.tvDetalheDescricao.text = servico.descricao

            valorUnitario = servico.valorServ
            val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvDetalheValor.text = "Valor Unitário: ${formatadorMoeda.format(valorUnitario)}"
            binding.tvValorTotalCalculado.text = "Valor Total: ${formatadorMoeda.format(valorUnitario)}"

            val textoData = if (!servico.dtContratacao.isNullOrEmpty() && servico.dtContratacao != "0000-00-00") {
                try {
                    val formatoOriginal = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val formatoDestino = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                    val dataParseada = formatoOriginal.parse(servico.dtContratacao)
                    "Contratado em: ${formatoDestino.format(dataParseada)}"
                } catch (e: Exception) {
                    "Data: ${servico.dtContratacao}"
                }
            } else {
                ""
            }

            if (!servico.solicitante.isNullOrEmpty()) {
                val partes = servico.solicitante.split("||")
                val nomeCliente = partes.getOrNull(0) ?: "Não informado"
                val telClienteRaw = partes.getOrNull(1) ?: "Não informado"
                val localCliente = partes.getOrNull(2) ?: "Não informado"

                val telClienteFormatado = formatarTelefone(telClienteRaw)

                binding.tvNomeAnunciante.text = "Cliente: $nomeCliente"
                binding.tvContatoAnunciante.text = "Telefone: $telClienteFormatado\n$textoData"
                binding.tvLocalizacaoAnunciante.text = "Endereço de Entrega: $localCliente"

                binding.tilQuantidadeServico.visibility = View.GONE
                binding.tilLocalServico.visibility = View.GONE
            } else {
                binding.tvNomeAnunciante.text = "Prestador ID: ${servico.idEmpresa ?: servico.idPF}"
                binding.tvContatoAnunciante.text = "Contato liberado após a solicitação."
                binding.tvLocalizacaoAnunciante.text = "Localização flexível conforme combinado."

                binding.tilQuantidadeServico.visibility = View.VISIBLE
                binding.tilLocalServico.visibility = View.VISIBLE
            }
        }
    }

    private fun configurarCalculoTotal() {
        binding.tietQuantidadeServico.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val quantidadeStr = s.toString().trim()
                val quantidade = quantidadeStr.toIntOrNull() ?: 1
                val valorTotal = valorUnitario * quantidade
                val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                binding.tvValorTotalCalculado.text = "Valor Total: ${formatadorMoeda.format(valorTotal)}"
            }
        })
    }

    private fun configurarBotoesPorStatus() {
        val servico = servicoSelecionado ?: return
        val status = servico.statusServ

        val souODono = (idUsuarioLogado == servico.idEmpresa || idUsuarioLogado == servico.idPF)

        binding.btnSolicitar.visibility = View.GONE
        binding.btnAceitar.visibility = View.GONE
        binding.btnConcluir.visibility = View.GONE
        binding.btnCancelar.visibility = View.GONE

        if (!souODono) {
            when (status) {
                0 -> {
                    binding.btnSolicitar.visibility = View.VISIBLE
                    binding.btnSolicitar.text = "Solicitar Serviço"
                    binding.btnSolicitar.setOnClickListener { alterarStatusServico(1) }
                }
                1, 2 -> {
                    binding.btnCancelar.visibility = View.VISIBLE
                    binding.btnCancelar.text = "Cancelar Solicitação"
                    binding.btnCancelar.setOnClickListener { alterarStatusServico(5) } // CORRIGIDO: Era 4 (concluído), agora é 5 (cancelado)
                }
            }
        } else {
            when (status) {
                0 -> {
                    binding.btnCancelar.visibility = View.VISIBLE
                    binding.btnCancelar.text = "Cancelar Anúncio"
                    binding.btnCancelar.setOnClickListener { alterarStatusServico(5) } // CORRIGIDO: Mudado de 4 para 5
                }
                1 -> {
                    binding.btnAceitar.visibility = View.VISIBLE
                    binding.btnCancelar.visibility = View.VISIBLE
                    binding.btnCancelar.text = "Recusar Solicitação"

                    binding.btnAceitar.setOnClickListener { alterarStatusServico(2) }
                    binding.btnCancelar.setOnClickListener { alterarStatusServico(5) } // CORRIGIDO: Mudado de 4 para 5
                }
                2 -> {
                    binding.btnConcluir.visibility = View.VISIBLE
                    binding.btnCancelar.visibility = View.VISIBLE
                    binding.btnCancelar.text = "Cancelar Serviço"

                    binding.btnConcluir.setOnClickListener { alterarStatusServico(4) } // ADICIONADO: Faltava a ação do botão Concluir (4)
                    binding.btnCancelar.setOnClickListener { alterarStatusServico(5) } // Mantido 5 para Cancelar
                }
                3 -> {
                    binding.btnSolicitar.visibility = View.VISIBLE
                    binding.btnSolicitar.text = "Confirmar"
                    binding.btnSolicitar.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))

                    binding.btnCancelar.setOnClickListener { alterarStatusServico(5) } // CORRIGIDO: Mudado de 4 para 5
                }
                4, 5 -> {
                    binding.btnSolicitar.visibility = View.VISIBLE
                    binding.btnSolicitar.text = "Anunciar Novamente"
                    binding.btnSolicitar.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                    binding.btnSolicitar.setOnClickListener { reanunciarServico() }
                }
            } // Fechamento do when(status) que estava faltando
        } // Fechamento do else que estava faltando
    }

    private fun alterarStatusServico(novoStatus: Int) {
        val servico = servicoSelecionado ?: return
        val localPrestacao = binding.tietLocalServico.text.toString().trim()
        val quantidadeStr = binding.tietQuantidadeServico.text.toString().trim()
        val quantidade = quantidadeStr.toIntOrNull() ?: 1

        if (novoStatus == 1 && localPrestacao.isEmpty()) {
            Toast.makeText(this, "Por favor, informe o local da prestação!", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
        val nomeUser = prefs.getString("NOME_USUARIO", "Usuário Solvae")
        val telUser = prefs.getString("TELEFONE_USUARIO", "(00) 00000-0000")

        val dadosSolicitante = if (novoStatus == 1) {
            "$nomeUser||$telUser||$localPrestacao"
        } else {
            servico.solicitante
        }

        val dataDoClique = if (novoStatus == 1 || novoStatus == 2) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        } else {
            servico.dtContratacao
        }

        val servicoAtualizado = servico.copy(
            statusServ = novoStatus,
            solicitante = dadosSolicitante,
            dtContratacao = dataDoClique,
            qtServ = quantidade
        )

        val api = RetrofitClient.instancia
        val idDoServico = servicoAtualizado.idServ ?: 0

        api.atualizarServico(idDoServico, servicoAtualizado).enqueue(object : Callback<Servico> {
            override fun onResponse(call: Call<Servico>, response: Response<Servico>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DetalhesServico, "Sucesso ao atualizar serviço!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalhesServico, "Erro ao atualizar dados no servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Servico>, t: Throwable) {
                Toast.makeText(this@DetalhesServico, "Erro de conexão com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    } // Fechamento da função alterarStatusServico que faltava no seu código

    private fun reanunciarServico() {
        val servico = servicoSelecionado ?: return

        val novoServicoReanunciado = servico.copy(
            idServ = null,
            statusServ = 0,
            solicitante = null,
            dtContratacao = "2026-06-19",
            qtServ = 1
        )

        val api = RetrofitClient.instancia
        api.criarServico(novoServicoReanunciado).enqueue(object : Callback<Servico> {
            override fun onResponse(call: Call<Servico>, response: Response<Servico>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DetalhesServico, "Anúncio republicado com sucesso no Menu!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalhesServico, "Erro ao processar nova publicação.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Servico>, t: Throwable) {
                Toast.makeText(this@DetalhesServico, "Sem conexão para reanunciar.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatarTelefone(telefone: String): String {
        val apenasNumeros = telefone.replace(Regex("[^0-9]"), "")
        return if (apenasNumeros.length == 11) {
            "(${apenasNumeros.substring(0, 2)}) ${apenasNumeros.substring(2, 7)}-${apenasNumeros.substring(7)}"
        } else {
            telefone
        }
    }
}