package com.example.libraryapp.ui.list.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.databinding.BookItemBinding

class BookAdapter(
    private var bookList: List<GoogleBook>?,
    val listener: RecyclerViewEvent
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context))
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList?.get(position)
        holder.bind(book)
    }

    override fun getItemCount(): Int = bookList?.size ?: 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<GoogleBook>) {
        bookList = newList
        notifyDataSetChanged()
    }

    inner class BookViewHolder(val binding: BookItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: GoogleBook?) = with(binding) {
            book?.let {
                val authorName = book.volumeInfo.authors?.firstOrNull() ?:
                root.context.getString(R.string.unknown_author)

                textViewName.text = book.volumeInfo.title
                textViewAuthor.text = authorName

                root.setOnClickListener {
                    listener.onItemClick(book.id)
                }

                imageViewBook.load(book.volumeInfo.imageLinks?.thumbnail) {
                    placeholder(R.drawable.img_book_wallpaper)
                    error(R.drawable.ic_error_image)
                    allowHardware(false)
                    listener(
                        onSuccess = { _, result ->
                            Palette.Builder(result.drawable.toBitmap()).generate { palette ->
                                palette?.let {
                                    val color = it.getDominantColor(
                                        ContextCompat.getColor(root.context, R.color.background)
                                    )
                                    val gradientDrawable = GradientDrawable().apply {
                                        setColor(color)
                                        cornerRadius = root.context.resources.getDimension(R.dimen.margin_12)
                                    }
                                    constraintLayoutContainer.background = gradientDrawable
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    interface RecyclerViewEvent {
        fun onItemClick(bookId: String)
    }
}