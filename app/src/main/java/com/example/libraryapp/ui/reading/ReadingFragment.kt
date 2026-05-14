package com.example.libraryapp.ui.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.libraryapp.databinding.FragmentReadingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingFragment : Fragment() {

    private lateinit var binding: FragmentReadingBinding
    private val args: ReadingFragmentArgs by navArgs()
    private val viewModel: ReadingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupListeners()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupWebView() {
        binding.webView.apply {
            loadUrl(args.url)

            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                viewModel.saveScrollPosition(args.bookId, scrollY)
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    view?.loadUrl(url)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val scrollPosition = viewModel.getScrollPosition(args.bookId)
                        delay(500)
                        view?.scrollTo(0, scrollPosition)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    Toast.makeText(
                        context,
                        "An error occurred while loading the page",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progress.apply {
                        progress = newProgress
                        visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        binding.webView.apply {
            clearCache(true)
            clearHistory()
            clearFormData()
            destroy()
        }
        super.onDestroy()
    }
}
