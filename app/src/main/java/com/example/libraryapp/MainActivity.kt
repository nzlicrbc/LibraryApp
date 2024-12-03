package com.example.libraryapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI öğelerini tanımla
        searchButton = findViewById(R.id.searchButton)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchBooks(query)  // sadece kitapları arayacağız
            } else {
                Toast.makeText(this, "Lütfen bir arama terimi girin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchBooks(query: String) {
        // API'ye arama isteği gönderiyoruz
        RetrofitClient.api.searchBooks(query, 1).enqueue(object : Callback<BookSearchResponse> {
            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val books = response.body()?.docs
                    books?.let {
                        // Kitapları RecyclerView ile listeleyelim
                        adapter = BookAdapter(it)
                        recyclerView.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API isteği başarısız: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
