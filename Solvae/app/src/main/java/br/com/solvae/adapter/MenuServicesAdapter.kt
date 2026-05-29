package br.com.solvae.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.solvae.databinding.ItemServicoBinding
import br.com.solvae.model.Servico

class MenuServicesAdapter: ListAdapter<Servico, MenuServicesAdapter.MenuServicesViewHolder>(DIFF) {

    private var itemClickServico: ((posicao: Int)-> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuServicesViewHolder {
        return MenuServicesViewHolder.create(parent, itemClickServico)
    }

    override fun onBindViewHolder(holder: MenuServicesViewHolder, position: Int) {
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
                return oldItem.idServ == newItem.idServ
            }

        }
    }

    class MenuServicesViewHolder(
        private val itemServicoBinding: ItemServicoBinding,
        private val itemClickServico:((posicao: Int)-> Unit)? = null
    ): RecyclerView.ViewHolder(itemServicoBinding.root){

        fun bind(servico: Servico , posicao: Int){

            itemServicoBinding.tvTipoServ.text = servico.tipoServ
            itemServicoBinding.tVEspec.text = servico.Espec
            itemServicoBinding.tVValor.text = servico.valorServ
            itemServicoBinding.llDetalhes.setOnClickListener{
                itemClickServico?.invoke(posicao)
            }

        }

        companion object{
            fun create(
                parent: ViewGroup,
                itemClickServico: ((posicao: Int) -> Unit)? = null
            ):MenuServicesViewHolder{
                val itemServicoBinding = ItemServicoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return MenuServicesViewHolder(itemServicoBinding ,itemClickServico)
            }
        }

    }

}