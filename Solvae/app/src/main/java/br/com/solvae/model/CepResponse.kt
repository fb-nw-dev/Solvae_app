package br.com.solvae.model

data class CepResponse(
    val cep: String,
    val logradouro: String, // Rua
    val bairro: String,
    val localidade: String, // Cidade
    val uf: String          // Estado
)