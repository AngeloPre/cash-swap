package com.example.cashswap.controller

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashswap.R
import com.example.cashswap.model.Currency
import com.example.cashswap.model.Moeda
import com.example.cashswap.services.ServiceProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ConvertActivity : AppCompatActivity() {

    private val moedas = listOf(
        Moeda("BRL", "Real Brasileiro", "R$", R.drawable.real, 2, 100_000.00),
        Moeda("USD", "Dólar Americano", "$", R.drawable.dolar, 2, 50_000.00),
        Moeda("BTC", "Bitcoin", "BTC", R.drawable.bitcoin, 6, 0.5)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert)

        val spinnerOrigem = findViewById<Spinner>(R.id.spinnerOrigem)
        val spinnerDestino = findViewById<Spinner>(R.id.spinnerDestino)
        val editValor = findViewById<EditText>(R.id.editValor)
        val btnConverter = findViewById<Button>(R.id.btnConverter)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val cardResultado = findViewById<View>(R.id.cardResultado)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        // Configura Spinners
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, moedas.map { it.nome })
        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter
        spinnerDestino.setSelection(1)

        findViewById<Button>(R.id.btnVoltar).setOnClickListener { finish() }

        btnConverter.setOnClickListener {
            val valorString = editValor.text.toString()
            val valorBigDecimal = valorString.toBigDecimalOrNull()

            if (valorBigDecimal != null && valorBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                lifecycleScope.launch {
                    try {
                        progressBar.visibility = View.VISIBLE
                        cardResultado.visibility = View.GONE

                        val de = Currency.valueOf(moedas[spinnerOrigem.selectedItemPosition].codigo)
                        val para = Currency.valueOf(moedas[spinnerDestino.selectedItemPosition].codigo)

                        val resultado = ServiceProvider.exchangeRateService.convert(valorBigDecimal, de, para)

                        tvResultado.text = resultado.toPlainString()
                        cardResultado.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        val msg = "Erro: ${e.localizedMessage ?: "Falha na conexão"}"
                        Snackbar.make(btnConverter, msg, Snackbar.LENGTH_LONG).show()
                    } finally {
                        progressBar.visibility = View.GONE
                    }
                }
            } else {
                editValor.error = "Digite um valor maior que zero"
            }
        }
    }
}
