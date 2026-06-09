package br.com.solvae.api

import br.com.solvae.model.Empresa
import br.com.solvae.model.Login
import br.com.solvae.model.PessoaFisica
import br.com.solvae.model.Servico
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // 1. Rota para criar um serviço
    @POST("servicos")
    fun criarServico(@Body novoServico: Servico): Call<Servico>

    // 2. Rota para realizar o Login
    @POST("login")
    fun realizarLogin(@Body dadosLogin: Login): Call<Login>

    // 3. Rota para cadastrar uma Pessoa Física (Atualizado com a nova composição)
    @POST("usuarios/pf")
    fun cadastrarPessoaFisica(@Body novaPf: PessoaFisica): Call<PessoaFisica>

    // 4. Rota para cadastrar uma Empresa (Atualizado com a nova composição)
    @POST("usuarios/empresa")
    fun cadastrarEmpresa(@Body novaEmpresa: Empresa): Call<Empresa>

    // Busca a lista de todos os serviços cadastrados no servidor
    @GET("servicos")
    fun listarServicos(): Call<List<Servico>>

    // Busca os dados de uma Pessoa Física específica usando o ID dela
    @GET("usuarios/pf/{id}")
    fun obterPessoaFisicaPorId(@Path("id") id: Int): Call<PessoaFisica>

    // Busca os dados de uma Empresa específica usando o ID dela
    @GET("usuarios/empresa/{id}")
    fun obterEmpresaPorId(@Path("id") id: Int): Call<Empresa>

    @GET("servicos/usuario/{usuarioId}")
    fun listarServicosPorUsuario(@Path("usuarioId") usuarioId: Int): Call<List<Servico>>
}