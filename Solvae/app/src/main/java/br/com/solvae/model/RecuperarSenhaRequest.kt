package br.com.solvae.model

import com.google.gson.annotations.SerializedName

data class RecuperarSenhaRequest(
    @SerializedName("CPF") val cpf: String,
    @SerializedName("Senha_Usuario") val novaSenha: String,
    @SerializedName("CNPJ") val cnpj: String
)
