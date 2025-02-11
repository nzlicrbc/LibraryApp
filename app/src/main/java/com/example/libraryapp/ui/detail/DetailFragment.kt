package com.example.libraryapp.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.databinding.FragmentDetailBinding
import com.example.libraryapp.ui.detail.model.BookDetailState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        viewModel.getBookWithId(args.bookId)
    }

    private fun setupClickListeners() = with(binding) {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        fabFavorite.setOnClickListener {
            scaleAnimation(it)
            (viewModel.bookState.value as? BookDetailState.Success)?.book?.let { book ->
                viewModel.toggleFavorite(book)
            }
        }

        fabSave.setOnClickListener {
            scaleAnimation(it)
            (viewModel.bookState.value as? BookDetailState.Success)?.book?.let { book ->
                viewModel.toggleSave(book)
            }
        }

        fabReadStatus.setOnClickListener {
            scaleAnimation(it)
            (viewModel.bookState.value as? BookDetailState.Success)?.book?.let { book ->
                viewModel.toggleRead(book)
            }
        }
    }

    private fun setViews(book: GoogleBook) = with(binding) {
        fadeInView(imageViewBook)
        fadeInView(imageViewBookBackground)

        val authorName = book.volumeInfo.authors?.firstOrNull() ?: getString(R.string.unknown_author)

        textViewBookName.text = book.volumeInfo.title
        textViewAuthor.text = authorName
        textViewLanguages.text = book.volumeInfo.language
        textViewContent.text = book.volumeInfo.description?.let { description ->
            android.text.Html.fromHtml(description, android.text.Html.FROM_HTML_MODE_COMPACT)
                .toString()
                .replace(Regex("<[^>]*>"), "")
        } ?: ""

        textViewDownloadCount.text = book.volumeInfo.pageCount?.toString() ?: "-"

        imageViewBook.load(book.volumeInfo.imageLinks?.thumbnail) {
            placeholder(R.drawable.img_book_wallpaper)
            error(R.drawable.ic_error_image)
        }

        imageViewBookBackground.load(book.volumeInfo.imageLinks?.thumbnail) {
            placeholder(R.drawable.img_book_wallpaper)
            error(R.drawable.ic_error_image)
        }

        book.accessInfo?.webReaderLink?.let { link ->
            buttonRead.visibility = View.VISIBLE
            buttonRead.translationY = 100f
            buttonRead.animate()
                .translationY(0f)
                .setDuration(500)
                .start()
            buttonRead.setOnClickListener {
                findNavController().navigate(
                    DetailFragmentDirections.toReadingFragment(link, book.id)
                )
            }
        } ?: run {
            buttonRead.visibility = View.GONE
        }

        fadeInView(textViewBookName, 400)
        fadeInView(textViewAuthor, 600)
        fadeInView(textViewLanguages, 800)
        fadeInView(textViewContent, 1000)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bookState.collect { state ->
                when (state) {
                    is BookDetailState.Loading -> {
                        binding.progressBar.isVisible = true
                        fadeInView(binding.progressBar)
                    }
                    is BookDetailState.Success -> {
                        binding.progressBar.animate().alpha(0f).setDuration(300).withEndAction {
                            binding.progressBar.isVisible = false
                        }.start()
                        setViews(state.book)
                    }
                    is BookDetailState.Error -> {
                        binding.progressBar.animate().alpha(0f).setDuration(300).withEndAction {
                            binding.progressBar.isVisible = false
                        }.start()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFavorite.collect { isFavorite ->
                binding.fabFavorite.setImageResource(
                    if (isFavorite) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaved.collect { isSaved ->
                binding.fabSave.setImageResource(
                    if (isSaved) R.drawable.ic_bookmark
                    else R.drawable.ic_bookmark_border
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRead.collect { isRead ->
                binding.fabReadStatus.setImageResource(
                    if (isRead) R.drawable.ic_check
                    else R.drawable.ic_check_outline
                )
            }
        }
    }

    private fun fadeInView(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    private fun scaleAnimation(view: View, duration: Long = 100) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(duration)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .start()
            }
            .start()
    }
}
