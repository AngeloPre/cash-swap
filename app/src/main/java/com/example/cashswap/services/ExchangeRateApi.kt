package com.example.cashswap.services

import com.example.cashswap.model.CurrencyQuote
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("json/last/{pair}")
    suspend fun getQuote(
        @Path("pair") pairs: String
    ): Map<String, CurrencyQuote>
}