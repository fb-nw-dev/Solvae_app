
package br.com.solvae.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PessoaFisica(

    @SerializedName("idPF") val idPf : Int? = null,
    @SerializedName("Nome") val nome : String,
    @SerializedName("Data_Nasc") val dataNasc : String,
    @SerializedName("CEP") val cep : String,
    @SerializedName("Rua") val rua : String,
    @SerializedName("Bairro") val bairro : String,
    @SerializedName("Cidade") val cidade : String,
    @SerializedName("Numero") val numero : Int? = null,
    @SerializedName("Telefone") val telefone : String,
    @SerializedName("Sexo") val sexo : String,
    @SerializedName("CPF") val cpf : String,
    @SerializedName("Usuario_Login_idUsuario_Login") val usuarioLoginId: Int
)
