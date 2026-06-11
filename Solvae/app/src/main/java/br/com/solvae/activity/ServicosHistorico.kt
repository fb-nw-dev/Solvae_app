package br.com.solvae.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.solvae.adapter.HistoricoServAdapter
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityServicosHistoricoBinding
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServicosHistorico : AppCompatActivity() {

    private val binding by lazy {
        ActivityServicosHistoricoBinding.inflate(layoutInflater)
    }

    private var listaOriginal: List<Servico> = emptyList()
    private val historicoAdapter by lazy { HistoricoServAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvHistorico.adapter = historicoAdapter

        historicoAdapter.itemClickHistorico = { posicao ->
            val servicoClicado = historicoAdapter.currentList[posicao]
            val intent = Intent(this@ServicosHistorico, DetalhesServico::class.java)
            intent.putExtra("SERVICO_SELECIONADO", servicoClicado)
            startActivity(intent)
        }

        // 1. Controle do Botão de Pesquisa
        binding.searchButton.setOnClickListener {
            if (binding.editSearch.visibility == View.GONE) {
                binding.editSearch.visibility = View.VISIBLE
                binding.editSearch.requestFocus()
            } else {
                binding.editSearch.text.clear()
                binding.editSearch.visibility = View.GONE
                filtrarLista("")
            }
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 3. Recuperar ID do Usuário
        val sharedPreferences = getSharedPreferences("SOLVAE_PREFS", Context.MODE_PRIVATE)
        val idUsuarioLogado = sharedPreferences.getInt("ID_USUARIO", -1)

        if (idUsuarioLogado != -1) {
            carregarHistoricoDoUsuario(idUsuarioLogado)
        } else {
            Toast.makeText(this, "Usuário não identificado. Faça login novamente.", Toast.LENGTH_SHORT).show()
        }

        configurarMenuInferior()
    }

    private fun carregarHistoricoDoUsuario(usuarioId: Int) {
        val api = RetrofitClient.instancia

        api.listarServicosPorUsuario(usuarioId).enqueue(object : Callback<List<Servico>> {
            override fun onResponse(call: Call<List<Servico>>, response: Response<List<Servico>>) {
                if (response.isSuccessful) {
                    listaOriginal = response.body() ?: emptyList()
                    if (listaOriginal.isEmpty()) {
                        Toast.makeText(this@ServicosHistorico, "Você ainda não cadastrou nenhum serviço.", Toast.LENGTH_SHORT).show()
                    }
                    historicoAdapter.submitList(listaOriginal)
                } else {
                    Toast.makeText(this@ServicosHistorico, "Erro ao carregar histórico: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Servico>>, t: Throwable) {
                Toast.makeText(this@ServicosHistorico, "Falha ao conectar com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarLista(texto: String) {
        val textoMinusculo = texto.lowercase().trim()
        val listaFiltrada = listaOriginal.filter { servico ->
            servico.tipoServ?.lowercase()?.contains(textoMinusculo) == true ||
                    servico.Espec?.lowercase()?.contains(textoMinusculo) == true
        }
        historicoAdapter.submitList(listaFiltrada)
    }

    private fun configurarMenuInferior() {
        // Menu Superior (MenuBar)
        binding.menubar.setOnClickListener {
            startActivity(Intent(this, MenuBar::class.java))
            // Opcional: finish() aqui se quiser fechar o histórico ao abrir o menu
        }

        // Botões do Menu Inferior
        binding.btnServicos.setOnClickListener {
            startActivity(Intent(this, MenuServices::class.java))
            finish()
        }

        binding.btnAdicionar.setOnClickListener {
            startActivity(Intent(this, AdcServ::class.java))
            finish()
        }

        binding.btnContratos.setOnClickListener {
            Toast.makeText(this, "Você já está na tela de histórico!", Toast.LENGTH_SHORT).show()
        }
    }
}