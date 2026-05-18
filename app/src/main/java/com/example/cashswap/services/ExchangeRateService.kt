package com.example.cashswap.services

import com.example.cashswap.model.Currency
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class ExchangeRateService (
    private val api: ExchangeRateApi
) {
    private val mathContext = MathContext(20, RoundingMode.HALF_UP)

    suspend fun convert(
        amount: BigDecimal,
        from: Currency,
        to: Currency
    ): BigDecimal {
        if (from == to) return amount

        val quotes = api.getQuote("USD-BRL,BTC-BRL")

        val usdBrl = quotes["USDBRL"]
            ?.bid
            ?.toBigDecimal()
            ?: throw IllegalStateException("USD-BRL quote not found")

        val btcBrl = quotes["BTCBRL"]
            ?.bid
            ?.toBigDecimal()
            ?: throw IllegalStateException("BTC-BRL quote not found")

        val btcUsd = btcBrl.divide(usdBrl, mathContext)

        val amountInUsd = when (from) {
            Currency.USD -> amount
            Currency.BRL -> amount.divide(usdBrl, mathContext)
            Currency.BTC -> amount.multiply(btcUsd, mathContext)
        }

        return when (to) {
            Currency.USD -> amountInUsd
            Currency.BRL -> amountInUsd.multiply(usdBrl, mathContext)
            Currency.BTC -> amountInUsd.divide(btcUsd, mathContext)
        }
    }
}