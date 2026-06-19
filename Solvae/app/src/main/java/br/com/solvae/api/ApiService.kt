package br.com.solvae.api

import br.com.solvae.model.Empresa
import br.com.solvae.model.Login
import br.com.solvae.model.PessoaFisica
import br.com.solvae.model.Servico
import br.com.solvae.model.CepResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {

    // 1. Rota para criar um serviço
    @POST("servicos")
    fun criarServico(@Body novoServico: Servico): Call<Servico>

    // 2. Rota para realizar o Login
    @POST("login")
    fun realizarLogin(@Body dadosLogin: Login): Call<Login>

    // 3. Rota para cadastrar uma Pessoa Física
    @POST("usuarios/pf")
    fun cadastrarPessoaFisica(@Body novaPf: PessoaFisica): Call<PessoaFisica>

    // 4. Rota para cadastrar uma Empresa
    @POST("usuarios/empresa")
    fun cadastrarEmpresa(@Body novaEmpresa: Empresa): Call<Empresa>

    // --- ATUALIZAÇÃO (PUT) ---

    // 🌟 ADICIONADO: Rota para atualizar um serviço existente (Evita a clonagem no banco de dados)
    @PUT("servicos/{id}")
    fun atualizarServico(@Path("id") id: Int, @Body servicoAtualizado: Servico): Call<Servico>

    // 5. Rota para atualizar uma Pessoa Física
    @PUT("usuarios/pf/{id}")
    fun atualizarPessoaFisica(@Path("id") id: Int, @Body pfAtualizada: PessoaFisica): Call<PessoaFisica>

    // 6. Rota para atualizar uma Empresa
    @PUT("usuarios/empresa/{id}")
    fun atualizarEmpresa(@Path("id") id: Int, @Body empresaAtualizada: Empresa): Call<Empresa>

    // --- BUSCAS E LISTAGENS ---

    // Busca a lista de todos os serviços cadastrados no servidor
    @GET("servicos")
    fun listarServicos(): Call<List<Servico>>

    // Busca os dados de uma Pessoa Física específica usando o ID dela
    @GET("usuarios/pf/{id}")
    fun obterPessoaFisicaPorId(@Path("id") id: Int): Call<PessoaFisica>

    // Busca os dados de uma Empresa específica usando o ID dela
    @GET("usuarios/empresa/{id}")
    fun obterEmpresaPorId(@Path("id") id: Int): Call<Empresa>

    // Busca os serviços vinculados a um usuário específico
    @GET("servicos/usuario/{usuarioId}")
    fun listarServicosPorUsuario(@Path("usuarioId") usuarioId: Int): Call<List<Servico>>

    // Rota corrigida para o ViaCEP
    @GET
    fun buscarCep(@Url url: String): Call<CepResponse>
}