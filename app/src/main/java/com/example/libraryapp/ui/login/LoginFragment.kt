package com.example.libraryapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.libraryapp.R
import com.example.libraryapp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.textViewSignUp.setOnClickListener {
            findNavController().navigate(R.id.to_registerFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.buttonLogin.isEnabled = false
                        clearErrors()
                    }
                    is LoginState.Success -> {
                        binding.progressBar.isVisible = false
                        findNavController().navigate(R.id.to_listFragment)
                    }
                    is LoginState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.buttonLogin.isEnabled = true
                        handleError(state.message)
                    }
                    is LoginState.Initial -> {
                        binding.progressBar.isVisible = false
                        binding.buttonLogin.isEnabled = true
                    }
                }
            }
        }
    }

    private fun handleError(message: String) {
        when {
            message.contains("email", ignoreCase = true) -> {
                binding.textInputLayoutEmail.error = message
            }
            message.contains("password", ignoreCase = true) -> {
                binding.textInputLayoutPassword.error = message
            }
            else -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearErrors() {
        binding.textInputLayoutEmail.error = null
        binding.textInputLayoutPassword.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}