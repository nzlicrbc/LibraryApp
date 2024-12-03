package com.example.libraryapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI öğelerini tanımla
        searchButton = findViewById(R.id.searchButton)
        searchEditText = findViewById(R.id.searchEditText)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchBooks(query)
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
                        // Burada kitapları RecyclerView veya ListView ile listeleyebilirsiniz
                        // Örneğin, Toast ile sadece başlıkları gösterebiliriz:
                        val bookTitles = it.joinToString("\n") { book -> book.title }
                        Toast.makeText(this@MainActivity, bookTitles, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API isteği başarısız: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
