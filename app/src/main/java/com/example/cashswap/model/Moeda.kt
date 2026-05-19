package com.example.cashswap.model

import java.math.BigDecimal

data class Moeda(
    val codigo: String,
    val nome: String,
    val simbolo: String,
    val iconeResId: Int,
    val casasDecimais: Int,
    var saldo: BigDecimal
)
