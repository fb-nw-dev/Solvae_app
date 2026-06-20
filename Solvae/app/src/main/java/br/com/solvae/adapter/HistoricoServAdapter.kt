package br.com.solvae.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.solvae.databinding.ItemHistoricoBinding
import br.com.solvae.model.Servico

class HistoricoServAdapter: ListAdapter<Servico , HistoricoServAdapter.HistoricoViewHolder>(DIFF) {

    var itemClickHistorico: ((posicao: Int)-> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        return HistoricoViewHolder.create(parent, itemClickHistorico)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        try {
            holder.bind(getItem(position), position)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<Servico>() {
            override fun areItemsTheSame(oldItem: Servico, newItem: Servico): Boolean {
                return oldItem.idServ == newItem.idServ
            }

            override fun areContentsTheSame(oldItem: Servico, newItem: Servico): Boolean {
                return oldItem == newItem
            }
        }
    }

    class HistoricoViewHolder(
        private val itemHistoricoBinding: ItemHistoricoBinding,
        private val itemClickHistorico:((posicao: Int)-> Unit)? = null
    ): RecyclerView.ViewHolder(itemHistoricoBinding.root){

        fun bind(historico: Servico , posicao: Int){
            itemHistoricoBinding.tvTipoServ.text = historico.tipoServ
            itemHistoricoBinding.tVEspec.text = historico.Espec

            // CORRIGIDO: Agora comparando com inteiros (Int) de 0 a 5 em vez de Strings
            when (historico.statusServ) {
                0 -> {
                    itemHistoricoBinding.tvStatusServ.text = "Aberto"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.parseColor("#757575")) // Vermelho
                }
                1 -> {
                    itemHistoricoBinding.tvStatusServ.text = "Aguardando Aprovação"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.parseColor("#FF9800")) // Laranja
                }
                2 -> {
                    itemHistoricoBinding.tvStatusServ.text = "Em Andamento"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.parseColor("#2196F3")) // Azul
                }
                3 -> {
                    itemHistoricoBinding.tvStatusServ.text = "Confirmado"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.parseColor("#2E7D32")) // verde esmeralda
                }
                4 -> {
                    itemHistoricoBinding.tvStatusServ.text = "Concluído"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.parseColor("#4CAF50")) // Verde
                }
                5 -> {
                    itemHistoricoBinding.tvStatusServ.text = "cancelado"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.RED) // Cinza
                }
                else -> {
                    itemHistoricoBinding.tvStatusServ.text = "Desconhecido"
                    itemHistoricoBinding.tvStatusServ.setTextColor(Color.BLACK)
                }
            }

            // Clique utilizando o ID correto que você definiu
            itemHistoricoBinding.lldetalhes2.setOnClickListener {
                itemClickHistorico?.invoke(posicao)
            }
        }

        companion object{
            fun create(
                parent: ViewGroup,
                itemClickHistorico: ((posicao: Int) -> Unit)? = null
            ): HistoricoViewHolder{
                val itemHistoricoBinding = ItemHistoricoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return HistoricoViewHolder(itemHistoricoBinding ,itemClickHistorico)
            }
        }
    }
}