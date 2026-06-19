package br.com.solvae.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Servico(
    @SerializedName("idServicos") val idServ: Int? = null,
    @SerializedName("Tipo_Servico") val tipoServ: String, // CORRIGIDO: Adicionado o "r" em Tipo_Servico
    @SerializedName("especialidade") val Espec: String,
    @SerializedName("Valor_Servico") val valorServ: Double,
    @SerializedName("Status") val statusServ: Int,
    @SerializedName("Data_Contratacao") val dtContratacao: String, // "YYYY-MM-DD"
    @SerializedName("Descricao") val descricao: String,

    // Quem criou (um deles vai preenchido, o outro null)
    @SerializedName("idPF") val idPF: Int? = null,
    @SerializedName("idEmpresa") val idEmpresa: Int? = null,
    @SerializedName("Local") val local: String? = null, // Perfeito! Bate com a Activity e com o PHP

    // O seu novo campo de texto para o solicitante!
    @SerializedName("Solicitante") val solicitante: String? = null,

    @SerializedName("quantidadeServico") val qtServ: Int? = null
) : Serializable