package com.example.libraryapp.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.databinding.ItemBookCoverHorizontalBinding

class ShelfBookHorizontalAdapter(
    private val onBookClick: (String) -> Unit
) : RecyclerView.Adapter<ShelfBookHorizontalAdapter.CoverVH>() {

    private var books: List<GoogleBook> = emptyList()

    fun submit(list: List<GoogleBook>) {
        books = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = books.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoverVH {
        val binding = ItemBookCoverHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CoverVH(binding)
    }

    override fun onBindViewHolder(holder: CoverVH, position: Int) {
        holder.bind(books[position])
    }

    inner class CoverVH(
        private val binding: ItemBookCoverHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: GoogleBook) = with(binding) {
            imageCover.load(book.volumeInfo.imageLinks?.thumbnail) {
                placeholder(R.drawable.img_book_wallpaper)
                error(R.drawable.ic_error_image)
                allowHardware(false)
            }
            root.setOnClickListener { onBookClick(book.id) }
        }
    }
}
