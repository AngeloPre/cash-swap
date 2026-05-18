package com.example.cashswap.services

object ServiceProvider {
    val exchangeRateService: ExchangeRateService =
        ExchangeRateService(
            api = RetrofitProvider.exchangeRateApi
        )
}