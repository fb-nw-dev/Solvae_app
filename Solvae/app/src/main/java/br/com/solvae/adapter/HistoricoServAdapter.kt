package br.com.solvae.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.solvae.adapter.MenuServicesAdapter.MenuServicesViewHolder
import br.com.solvae.databinding.ItemHistoricoBinding
import br.com.solvae.databinding.ItemServicoBinding
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
                return oldItem == newItem
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
            itemHistoricoBinding.tvStatusServ.text = historico.statusServ
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