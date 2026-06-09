package br.com.solvae.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged // Importante para a pesquisa
import br.com.solvae.adapter.MenuServicesAdapter
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityMenuServicesBinding
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuSevices : AppCompatActivity() {

    private val binding by lazy {
        ActivityMenuServicesBinding.inflate(layoutInflater)
    }

    // Lista original que vem do servidor (nunca muda após o carregamento)
    private var menuServicos: List<Servico> = emptyList()
    private lateinit var menuServicesAdapter: MenuServicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configuração do Adapter
        menuServicesAdapter = MenuServicesAdapter()
        menuServicesAdapter.run {
            itemClickServico = { posicao: Int ->
                // IMPORTANTE: Pegamos o item atual que está visível no adapter,
                // assim funciona certo mesmo se a lista estiver filtrada!
                val servicoSelecionado = menuServicesAdapter.currentList[posicao]
                val intent = Intent(this@MenuSevices, DetalhesServico::class.java)
                intent.putExtra("SERVIÇO_SELECIONADO", servicoSelecionado)
                startActivity(intent)
            }
        }
        binding.rvServico.adapter = menuServicesAdapter

        // ========================================================
        // CONFIGURAÇÃO DOS BOTÕES DO TOPO (MENU E PESQUISA)
        // ========================================================

        // 1. Botão de Menu Lateral -> Abre MenuBar
        binding.menubar.setOnClickListener {
            val intent = Intent(this, MenuBar::class.java) // Nome da classe associada ao activity_menu_bar.xml
            startActivity(intent)
        }

        // 2. Botão de Lupa (Mostrar/Esconder Barra de Pesquisa)
        binding.searchButton.setOnClickListener {
            if (binding.editSearch.visibility == View.GONE) {
                binding.editSearch.visibility = View.VISIBLE
                binding.editSearch.requestFocus()
            } else {
                binding.editSearch.visibility = View.GONE
                binding.editSearch.text?.clear() // Limpa o texto e volta a lista original
            }
        }

        // LÓGICA DA PESQUISA: Filtra conforme o usuário digita
        binding.editSearch.doAfterTextChanged { texto ->
            val query = texto.toString().trim()

            if (query.isEmpty()) {
                // Se o campo estiver vazio, mostra a lista completa original
                menuServicesAdapter.submitList(menuServicos)
            } else {
                // Filtra se o tipo ou a especialidade contiverem o texto digitado (ignorando maiúsculas/minúsculas)
                val listaFiltrada = menuServicos.filter { servico ->
                    servico.tipoServ.contains(query, ignoreCase = true) ||
                            servico.Espec.contains(query, ignoreCase = true)
                }
                menuServicesAdapter.submitList(listaFiltrada)
            }
        }

        // ========================================================
        // CONFIGURAÇÃO DOS BOTÕES DO MENU INFERIOR
        // ========================================================

        // 3. Botão Serviços (Esta própria tela)
        binding.btnServicos.setOnClickListener {
            // Recarrega os dados do servidor para atualizar a lista
            recuperarMenuServico()
        }

        // 4. Botão Adicionar -> Abre AdcServ
        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, AdcServ::class.java) // Nome da classe do activity_adc_serv.xml
            startActivity(intent)
        }

        // 5. Botão Contratos -> Abre ServicosHistorico
        binding.btnContratos.setOnClickListener {
            val intent = Intent(this, ServicosHistorico::class.java) // Nome da classe do acticity_servicos_historico.xml
            startActivity(intent)
        }

        // ========================================================

        recuperarMenuServico()
    }

    private fun recuperarMenuServico() {
        val api = RetrofitClient.instancia

        api.listarServicos().enqueue(object : Callback<List<Servico>> {
            override fun onResponse(call: Call<List<Servico>>, response: Response<List<Servico>>) {
                if (response.isSuccessful) {
                    val listaRetornada = response.body()
                    if (listaRetornada != null) {
                        menuServicos = listaRetornada
                        menuServicesAdapter.submitList(menuServicos)
                    }
                } else {
                    Toast.makeText(this@MenuSevices, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Servico>>, t: Throwable) {
                Toast.makeText(this@MenuSevices, "Não foi possível conectar ao servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}