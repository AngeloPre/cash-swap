package com.example.cashswap.controller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashswap.R
import com.example.cashswap.model.CarteiraRepository
import com.example.cashswap.model.Currency
import com.example.cashswap.model.Moeda
import com.example.cashswap.services.ServiceProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.util.Locale

class ConvertActivity : AppCompatActivity() {

    private val moedas = CarteiraRepository.moedas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert)

        val spinnerOrigem = findViewById<Spinner>(R.id.spinnerOrigem)
        val spinnerDestino = findViewById<Spinner>(R.id.spinnerDestino)
        val editValor = findViewById<EditText>(R.id.editValor)
        val btnConverter = findViewById<Button>(R.id.btnConverter)
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val cardResultado = findViewById<View>(R.id.cardResultado)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        val tvSaldoDisponivel = findViewById<TextView>(R.id.tvSaldoDisponivel)
        val tvCotacao = findViewById<TextView>(R.id.tvCotacao)
        var valorOrigemCotado: BigDecimal? = null
        var valorDestinoCotado: BigDecimal? = null
        var posicaoOrigemCotada = -1
        var posicaoDestinoCotada = -1

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
                cardResultado.visibility = View.GONE
                btnConfirmar.visibility = View.GONE
                valorOrigemCotado = null
                valorDestinoCotado = null

                val valorDigitado = parseValorDigitado(editValor.text.toString()) ?: return
                val textoFormatado = formatarNumero(valorDigitado, moeda.casasDecimais)
                editValor.setText(textoFormatado)
                editValor.setSelection(textoFormatado.length)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerDestino.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                cardResultado.visibility = View.GONE
                btnConfirmar.visibility = View.GONE
                valorOrigemCotado = null
                valorDestinoCotado = null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val mascaraWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val textoAtual = s?.toString().orEmpty()
                cardResultado.visibility = View.GONE
                btnConfirmar.visibility = View.GONE
                valorOrigemCotado = null
                valorDestinoCotado = null

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
            val moedaOrigem = moedas[spinnerOrigem.selectedItemPosition]
            val moedaDestino = moedas[spinnerDestino.selectedItemPosition]

            if (valorBigDecimal != null && valorBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                if (moedaOrigem.codigo == moedaDestino.codigo) {
                    Snackbar.make(btnConverter, "Escolha moedas diferentes", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (valorBigDecimal > moedaOrigem.saldo) {
                    editValor.error = "Saldo insuficiente"
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        progressBar.visibility = View.VISIBLE
                        cardResultado.visibility = View.GONE
                        btnConfirmar.visibility = View.GONE

                        val de = Currency.valueOf(moedas[spinnerOrigem.selectedItemPosition].codigo)
                        val para = Currency.valueOf(moedas[spinnerDestino.selectedItemPosition].codigo)

                        val resultado = ServiceProvider.exchangeRateService.convert(valorBigDecimal, de, para)

                        valorOrigemCotado = valorBigDecimal
                        valorDestinoCotado = resultado
                        posicaoOrigemCotada = spinnerOrigem.selectedItemPosition
                        posicaoDestinoCotada = spinnerDestino.selectedItemPosition

                        tvResultado.text = formatarValor(resultado, moedaDestino)
                        tvCotacao.text = "${formatarValor(valorBigDecimal, moedaOrigem)} para ${formatarValor(resultado, moedaDestino)}"
                        cardResultado.visibility = View.VISIBLE
                        btnConfirmar.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        val msg = when (e) {
                            is UnknownHostException ->
                                "Sem conexão com a internet. Verifique sua rede e tente novamente."
                            is SocketTimeoutException ->
                                "A conexão demorou demais para responder. Tente novamente."
                            is HttpException ->
                                "O servidor de cotações retornou um erro (${e.code()}). Tente novamente em instantes."
                            is IOException ->
                                "Falha ao acessar o servidor de cotações. Verifique sua conexão."
                            else ->
                                "Não foi possível obter a cotação no momento. Tente novamente."
                        }
                        Snackbar.make(btnConverter, msg, Snackbar.LENGTH_LONG).show()
                    } finally {
                        progressBar.visibility = View.GONE
                    }
                }
            } else {
                editValor.error = "Digite um valor maior que zero"
            }
        }

        btnConfirmar.setOnClickListener {
            val valorOrigem = valorOrigemCotado
            val valorDestino = valorDestinoCotado

            if (valorOrigem == null || valorDestino == null) {
                Snackbar.make(btnConfirmar, "Busque a cotação antes de confirmar", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val moedaOrigem = moedas[posicaoOrigemCotada]
            val moedaDestino = moedas[posicaoDestinoCotada]

            if (valorOrigem > moedaOrigem.saldo) {
                editValor.error = "Saldo insuficiente"
                return@setOnClickListener
            }

            moedaOrigem.saldo = moedaOrigem.saldo
                .subtract(valorOrigem)
                .setScale(moedaOrigem.casasDecimais, RoundingMode.HALF_UP)
            moedaDestino.saldo = moedaDestino.saldo
                .add(valorDestino)
                .setScale(moedaDestino.casasDecimais, RoundingMode.HALF_UP)
            tvSaldoDisponivel.text = "Saldo disponível: ${formatarSaldo(moedaOrigem)}"
            btnConfirmar.visibility = View.GONE

            Snackbar.make(btnConfirmar, "Conversão confirmada", Snackbar.LENGTH_LONG).show()
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
