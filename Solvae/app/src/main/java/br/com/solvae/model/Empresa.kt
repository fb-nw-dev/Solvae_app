
package br.com.solvae.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Empresa(
    @SerializedName("idEmpresa") val idEmpresa: Int? = null,
    @SerializedName("Nome") val nome: String,
    @SerializedName("Rua") val rua: String,
    @SerializedName("CNAE") val cnae: Int,
    @SerializedName("CNPJ") val cnpj: String,
    @SerializedName("Razao_social") val razaoSocial: String,
    @SerializedName("Data_abertura") val dataAbertura: String,
    @SerializedName("Capital_social") val capitalSocial: Double,
    @SerializedName("Tipo_empresa") val tipoEmpresa: String,
    @SerializedName("Status") val statusEmp: Int,
    @SerializedName("Telefone") val telefone: String,
    @SerializedName("Numero") val numero: Int?,
    @SerializedName("Bairro") val bairro: String,
    @SerializedName("Cidade") val cidade: String,
    @SerializedName("UF") val uf: String,
    @SerializedName("CEP") val cep: String,
    @SerializedName("Usuario_Login_idUsuario_Login") val usuarioLoginId: Int,

    // COMPOSIÇÃO: Diz ao Retrofit para enviar os dados de login juntos
    @SerializedName("login") val login: Login
) : Serializable