package com.example.cashswap.model

import com.google.gson.annotations.SerializedName

data class CurrencyQuote(
    val code: String,
    val codein: String,
    val name: String,
    val high: String,
    val low: String,

    @SerializedName("varBid")
    val varBid: String,

    val pctChange: String,
    val bid: String,
    val ask: String,
    val timestamp: String,

    @SerializedName("create_date")
    val createDate: String
)
