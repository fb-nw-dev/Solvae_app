package br.com.solvae.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://heavily-vienna-tracked-sit.trycloudflare.com/api/"

    val instancia: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Diz ao Retrofit para usar o Gson que você adicionou
            .build()

        retrofit.create(ApiService::class.java)
    }
}