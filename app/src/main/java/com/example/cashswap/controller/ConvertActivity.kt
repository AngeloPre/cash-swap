package com.example.cashswap.controller

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cashswap.R
import com.example.cashswap.model.Moeda
import java.text.NumberFormat
import java.util.Locale

class ConvertActivity : AppCompatActivity() {

    private val moedas: List<Moeda> = listOf(
        Moeda("BRL", "Real Brasileiro", "R$", R.drawable.real, 2, 100_000.00),
        Moeda("USD", "Dólar Americano", "$", R.drawable.dolar, 2, 50_000.00),
        Moeda("BTC", "Bitcoin", "BTC", R.drawable.bitcoin, 6, 0.5)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_convert)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nomes = moedas.map { "${it.nome} (${it.simbolo})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerOrigem = findViewById<Spinner>(R.id.spinnerOrigem)
        spinnerOrigem.adapter = adapter
        findViewById<Spinner>(R.id.spinnerDestino).adapter = adapter

        val tvSaldoDisponivel = findViewById<TextView>(R.id.tvSaldoDisponivel)
        spinnerOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val moeda = moedas[position]
                tvSaldoDisponivel.text = "Saldo disponível: ${formatarSaldo(moeda)}"
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun formatarSaldo(moeda: Moeda): String {
        val fmt = NumberFormat.getNumberInstance(Locale("pt", "BR")).apply {
            minimumFractionDigits = moeda.casasDecimais
            maximumFractionDigits = moeda.casasDecimais
        }
        return if (moeda.codigo == "BTC")
            "${fmt.format(moeda.saldo)} ${moeda.simbolo}"
        else
            "${moeda.simbolo} ${fmt.format(moeda.saldo)}"
    }
}