package com.example.cashswap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashswap.R
import com.example.cashswap.model.Moeda
import java.util.Locale

class MoedasAdapter(
    private var moedas: List<Moeda>,
    private val context: Context
) : RecyclerView.Adapter<MoedasAdapter.MoedaViewHolder>() {

    fun updateList(newList: List<Moeda>) {
        moedas = newList
        notifyDataSetChanged()
    }

    inner class MoedaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcone = itemView.findViewById<ImageView>(R.id.ivIconeMoeda)
        private val tvNome = itemView.findViewById<TextView>(R.id.tvNomeMoeda)
        private val tvCodigo = itemView.findViewById<TextView>(R.id.tvCodigoMoeda)
        private val tvSaldo = itemView.findViewById<TextView>(R.id.tvSaldoMoeda)

        fun bind(moeda: Moeda) {
            ivIcone.setImageResource(moeda.iconeResId)
            tvNome.text = moeda.nome
            tvCodigo.text = moeda.codigo
            tvSaldo.text = formatarSaldo(moeda)
        }

        private fun formatarSaldo(moeda: Moeda): String {
            val locale = Locale("pt", "BR")
            return if (moeda.codigo == "BTC") {
                String.format(locale, "%,.6f %s", moeda.saldo, moeda.codigo)
            } else {
                String.format(locale, "%s %,.2f", moeda.simbolo, moeda.saldo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoedaViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return MoedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoedaViewHolder, position: Int) {
        holder.bind(moedas[position])
    }

    override fun getItemCount(): Int = moedas.size
}
