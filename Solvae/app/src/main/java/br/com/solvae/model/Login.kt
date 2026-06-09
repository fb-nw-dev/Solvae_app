
package br.com.solvae.model

import com.google.gson.annotations.SerializedName

data class Login(

    @SerializedName("idUsuario_Login") val idUsuarioLogin : Int? = null,
    @SerializedName("Email") val email : String ,
    @SerializedName("Senha_Usuario") val senha : String ,
    @SerializedName("Tipo_Usuario") val tipoUsuario : String
    )
