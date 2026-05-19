package com.example.cashswap.model

import com.example.cashswap.R
import java.math.BigDecimal

object CarteiraRepository {
    val moedas: MutableList<Moeda> = mutableListOf(
        Moeda("BRL", "Real Brasileiro", "R$", R.drawable.real, 2, BigDecimal("100000.00")),
        Moeda("USD", "Dólar Americano", "$", R.drawable.dolar, 2, BigDecimal("50000.00")),
        Moeda("BTC", "Bitcoin", "BTC", R.drawable.bitcoin, 6, BigDecimal("0.500000"))
    )
}