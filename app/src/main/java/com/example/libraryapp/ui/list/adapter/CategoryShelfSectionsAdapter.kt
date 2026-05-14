package com.example.libraryapp.ui.list.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libraryapp.R
import com.example.libraryapp.databinding.ItemCategoryShelfSectionBinding
import com.example.libraryapp.ui.list.model.CategoryShelfUi

class CategoryShelfSectionsAdapter(
    private val onBookClick: (String) -> Unit,
    private val onShelfNeedMore: (String) -> Unit
) : RecyclerView.Adapter<CategoryShelfSectionsAdapter.SectionVH>() {

    private var shelves: List<CategoryShelfUi> = emptyList()

    fun submitList(list: List<CategoryShelfUi>) {
        shelves = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = shelves.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionVH {
        val binding = ItemCategoryShelfSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SectionVH(binding)
    }

    override fun onBindViewHolder(holder: SectionVH, position: Int) {
        holder.bind(shelves[position])
    }

    inner class SectionVH(
        private val binding: ItemCategoryShelfSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val innerAdapter = ShelfBookHorizontalAdapter(onBookClick)
        private var boundShelf: CategoryShelfUi? = null

        private val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val ui = boundShelf ?: return
                if (!ui.canLoadMore || ui.isLoadingMore) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val last = lm.findLastVisibleItemPosition()
                val count = innerAdapter.itemCount
                if (last == RecyclerView.NO_POSITION) return
                if (count > 0 && last >= count - 2) {
                    onShelfNeedMore(ui.shelfId)
                }
            }
        }

        init {
            binding.recyclerHorizontalBooks.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.recyclerHorizontalBooks.adapter = innerAdapter
            binding.recyclerHorizontalBooks.addOnScrollListener(scrollListener)
        }

        fun bind(ui: CategoryShelfUi) = with(binding) {
            boundShelf = ui
            textCategoryTitle.text = root.context.getString(ui.titleRes)
            textBookCount.text = root.context.getString(R.string.books_count_format, ui.bookCount)

            progressShelfLoading.isVisible = ui.isLoadingMore

            val base = ContextCompat.getColor(root.context, ui.shelfColorRes)
            val drawable = viewShelfBar.background.mutate() as GradientDrawable
            drawable.setColor(ColorUtils.setAlphaComponent(base, 0xCC))

            innerAdapter.submit(ui.books)
            recyclerHorizontalBooks.scrollToPosition(0)

            val scrollStep = (root.resources.displayMetrics.density * 280f).toInt()
            buttonScrollPrev.setOnClickListener {
                recyclerHorizontalBooks.scrollBy(-scrollStep, 0)
            }
            buttonScrollNext.setOnClickListener {
                recyclerHorizontalBooks.scrollBy(scrollStep, 0)
            }

            val shelfIdForPost = ui.shelfId
            recyclerHorizontalBooks.post {
                val current = boundShelf ?: return@post
                if (current.shelfId != shelfIdForPost) return@post
                if (!current.canLoadMore || current.isLoadingMore) return@post
                val lm = recyclerHorizontalBooks.layoutManager as? LinearLayoutManager ?: return@post
                val last = lm.findLastVisibleItemPosition()
                val count = innerAdapter.itemCount
                if (count > 0 && last >= count - 2) {
                    onShelfNeedMore(current.shelfId)
                }
            }
        }
    }
}
