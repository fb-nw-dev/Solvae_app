package br.com.solvae.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import br.com.solvae.adapter.MenuServicesAdapter
import br.com.solvae.api.RetrofitClient
import br.com.solvae.databinding.ActivityMenuServicesBinding
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuServices : AppCompatActivity() {

    private val binding by lazy {
        ActivityMenuServicesBinding.inflate(layoutInflater)
    }

    private var menuServicos: List<Servico> = emptyList()
    private lateinit var menuServicesAdapter: MenuServicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configuração do Adapter
        menuServicesAdapter = MenuServicesAdapter()
        menuServicesAdapter.itemClickServico = { posicao: Int ->
            val servicoSelecionado = menuServicesAdapter.currentList[posicao]
            val intent = Intent(this@MenuServices, DetalhesServico::class.java)
            intent.putExtra("SERVICO_SELECIONADO", servicoSelecionado)
            startActivity(intent)
        }
        binding.rvServico.adapter = menuServicesAdapter

        // ========================================================
        // CONFIGURAÇÃO DOS BOTÕES DO TOPO (MENU E PESQUISA)
        // ========================================================

        // AQUI: Abrimos a MenuBar SEM o finish(), para que o usuário
        // possa voltar para esta tela naturalmente.
        binding.menubar.setOnClickListener {
            val intent = Intent(this, MenuBar::class.java)
            startActivity(intent)
        }

        binding.searchButton.setOnClickListener {
            if (binding.editSearch.visibility == View.GONE) {
                binding.editSearch.visibility = View.VISIBLE
                binding.editSearch.requestFocus()
            } else {
                binding.editSearch.visibility = View.GONE
                binding.editSearch.text?.clear()
            }
        }

        binding.editSearch.doAfterTextChanged { texto ->
            val query = texto.toString().trim()

            if (query.isEmpty()) {
                menuServicesAdapter.submitList(menuServicos)
            } else {
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

        binding.btnServicos.setOnClickListener {
            recuperarMenuServico()
        }

        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, AdcServ::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnContratos.setOnClickListener {
            val intent = Intent(this, ServicosHistorico::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        recuperarMenuServico()
    }

    private fun recuperarMenuServico() {
        val api = RetrofitClient.instancia

        api.listarServicos().enqueue(object : Callback<List<Servico>> {
            override fun onResponse(call: Call<List<Servico>>, response: Response<List<Servico>>) {
                if (response.isSuccessful) {
                    val listaRetornada = response.body()
                    if (listaRetornada != null) {
                        menuServicos = listaRetornada.filter { it.statusServ == "0" }
                        menuServicesAdapter.submitList(menuServicos)
                    }
                } else {
                    Toast.makeText(this@MenuServices, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Servico>>, t: Throwable) {
                Toast.makeText(this@MenuServices, "Não foi possível conectar ao servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}