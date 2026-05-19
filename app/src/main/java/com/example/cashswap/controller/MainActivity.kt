package com.example.cashswap.controller

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashswap.R
import com.example.cashswap.adapter.MoedasAdapter
import com.example.cashswap.model.CarteiraRepository

class MainActivity : AppCompatActivity() {

    private lateinit var moedasRV: RecyclerView
    private lateinit var adapter: MoedasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        moedasRV = findViewById(R.id.moedasRV)
        moedasRV.layoutManager = LinearLayoutManager(this)
        moedasRV.setHasFixedSize(true)
        moedasRV.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        adapter = MoedasAdapter(CarteiraRepository.moedas, this)
        moedasRV.adapter = adapter

        val btnConverter = findViewById<Button>(R.id.btnConverter)
        btnConverter.setOnClickListener {
            val intent = Intent(this, ConvertActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.updateList(CarteiraRepository.moedas)
    }
}
