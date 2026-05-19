package com.example.cashswap.controller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.text.NumberFormat
import java.util.Locale

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
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val cardResultado = findViewById<View>(R.id.cardResultado)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        val tvSaldoDisponivel = findViewById<TextView>(R.id.tvSaldoDisponivel)

        // Configura Spinners
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, moedas.map { it.nome })
        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter
        spinnerDestino.setSelection(1)

        // Listener para atualizar o saldo disponível conforme a moeda de origem selecionada
        spinnerOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val moeda = moedas[position]
                tvSaldoDisponivel.text = "Saldo disponível: ${formatarSaldo(moeda)}"

                val valorDigitado = parseValorDigitado(editValor.text.toString()) ?: return
                val textoFormatado = formatarNumero(valorDigitado, moeda.casasDecimais)
                editValor.setText(textoFormatado)
                editValor.setSelection(textoFormatado.length)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val mascaraWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val textoAtual = s?.toString().orEmpty()
                val moedaOrigem = moedas[spinnerOrigem.selectedItemPosition]
                val textoFormatado = mascararValorDigitado(textoAtual, moedaOrigem.casasDecimais)

                if (textoFormatado == textoAtual) return

                editValor.removeTextChangedListener(this)
                editValor.setText(textoFormatado)
                editValor.setSelection(textoFormatado.length)
                editValor.addTextChangedListener(this)
            }
        }
        editValor.addTextChangedListener(mascaraWatcher)

        btnVoltar.setOnClickListener { finish() }

        btnConverter.setOnClickListener {
            val valorString = editValor.text.toString()
            val valorBigDecimal = parseValorDigitado(valorString)

            if (valorBigDecimal != null && valorBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                lifecycleScope.launch {
                    try {
                        progressBar.visibility = View.VISIBLE
                        cardResultado.visibility = View.GONE

                        val de = Currency.valueOf(moedas[spinnerOrigem.selectedItemPosition].codigo)
                        val para = Currency.valueOf(moedas[spinnerDestino.selectedItemPosition].codigo)
                        val moedaDestino = moedas[spinnerDestino.selectedItemPosition]

                        val resultado = ServiceProvider.exchangeRateService.convert(valorBigDecimal, de, para)

                        tvResultado.text = formatarValor(resultado, moedaDestino)
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

    private fun mascararValorDigitado(valor: String, casasDecimais: Int): String {
        val digitos = valor.filter { it.isDigit() }
        if (digitos.isEmpty() || digitos.all { it == '0' }) return ""

        val divisor = BigDecimal.TEN.pow(casasDecimais)
        val valorDecimal = BigDecimal(digitos).divide(divisor)
        return formatarNumero(valorDecimal, casasDecimais)
    }

    private fun parseValorDigitado(valor: String): BigDecimal? {
        val valorLimpo = valor.trim().replace(Regex("[^\\d,.]"), "")
        if (valorLimpo.isBlank()) return null

        val valorNormalizado = if (valorLimpo.contains(",")) {
            valorLimpo.replace(".", "").replace(",", ".")
        } else {
            valorLimpo
        }

        return valorNormalizado.toBigDecimalOrNull()
    }

    private fun formatarValor(valor: BigDecimal, moeda: Moeda): String {
        val valorFormatado = formatarNumero(valor, moeda.casasDecimais)
        return if (moeda.codigo == "BTC") "$valorFormatado ${moeda.simbolo}" else "${moeda.simbolo} $valorFormatado"
    }

    private fun formatarNumero(valor: BigDecimal, casasDecimais: Int): String {
        val fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = casasDecimais
            maximumFractionDigits = casasDecimais
        }
        return fmt.format(valor)
    }

    private fun formatarSaldo(moeda: Moeda): String {
        val fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = moeda.casasDecimais
            maximumFractionDigits = moeda.casasDecimais
        }
        val valorFormatado = fmt.format(moeda.saldo)
        return if (moeda.codigo == "BTC") "$valorFormatado ${moeda.simbolo}" else "${moeda.simbolo} $valorFormatado"
    }
}
