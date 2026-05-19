package com.example.cashswap.model

import com.example.cashswap.R

object CarteiraRepository {
    val moedas: MutableList<Moeda> = mutableListOf(
        Moeda("BRL", "Real Brasileiro", "R$", R.drawable.real, 2, 100_000.00),
        Moeda("USD", "Dólar Americano", "$", R.drawable.dolar, 2, 50_000.00),
        Moeda("BTC", "Bitcoin", "BTC", R.drawable.bitcoin, 6, 0.5)
    )
}
