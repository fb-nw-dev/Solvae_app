package br.com.solvae.model


import android.provider.ContactsContract
import com.google.gson.annotations.SerializedName

import java.util.Date

data class Empresa(
    @SerializedName("idEmpresa") val idEmpresa: Int? = null, // null por causa do AUTO_INCREMENT
    @SerializedName("Nome") val nome: String,
    @SerializedName("Rua") val rua: String,
    @SerializedName("CNAE") val cnae: Int,
    @SerializedName("CNPJ") val cnpj: String,
    @SerializedName("Razao_social") val razaoSocial: String,
    @SerializedName("Data_abertura") val dataAbertura: String, // String funciona melhor com Retrofit para o formato DATE "YYYY-MM-DD"
    @SerializedName("Capital_social") val capitalSocial: Double,
    @SerializedName("Tipo_empresa") val tipoEmpresa: String,
    @SerializedName("Status") val statusEmp: Int, // No seu banco a coluna chama "Status"
    @SerializedName("Telefone") val telefone: String,
    @SerializedName("Numero") val numero: Int?, // No seu banco está NULLable
    @SerializedName("Bairro") val bairro: String,
    @SerializedName("Cidade") val cidade: String,
    @SerializedName("UF") val uf: String,
    @SerializedName("CEP") val cep: String,
    @SerializedName("Usuario_Login_idUsuario_Login") val usuarioLoginId: Int
    )
