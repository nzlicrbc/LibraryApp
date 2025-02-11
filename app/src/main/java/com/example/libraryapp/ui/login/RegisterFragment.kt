package com.example.libraryapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.libraryapp.R
import com.example.libraryapp.data.auth.AuthManager
import com.example.libraryapp.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                registerUser(email, password)
            }
        }

        binding.textViewLogin.setOnClickListener {
            findNavController().navigate(R.id.to_loginFragment)
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            email.isEmpty() -> {
                binding.editTextEmailLayout.error = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmailLayout.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                binding.editTextPasswordLayout.error = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                binding.editTextPasswordLayout.error = "Password must be at least 6 characters"
                false
            }
            password != confirmPassword -> {
                binding.editTextConfirmPasswordLayout.error = "Passwords do not match"
                false
            }
            else -> {
                binding.editTextEmailLayout.error = null
                binding.editTextPasswordLayout.error = null
                binding.editTextConfirmPasswordLayout.error = null
                true
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = authManager.register(email, password)

            result.onSuccess {
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.to_listFragment)
            }.onFailure { exception ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Registration Failed: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}