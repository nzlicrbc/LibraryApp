package com.example.libraryapp.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.libraryapp.R
import com.example.libraryapp.data.auth.AuthManager
import com.example.libraryapp.databinding.FragmentProfileBinding
import com.example.libraryapp.ui.detail.DetailViewModel
import com.example.libraryapp.ui.profile.adapter.ProfilePagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var pagerAdapter: ProfilePagerAdapter

    @Inject
    lateinit var authManager: AuthManager

    private val pickVisualMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                copyUriToAvatarFile(uri)
                loadAvatarIntoView()
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                loadAvatarIntoView()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.profile_photo_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAvatarIntoView()
        setupProfilePhoto()
        setupViewPager()
        setupTabLayout()
        observeViewModel()
        observeDetailUpdates()
        setupLogout()
        animateInitialViews()
    }

    private fun setupProfilePhoto() {
        binding.profileImage.setOnClickListener {
            val options = arrayOf(
                getString(R.string.profile_photo_gallery),
                getString(R.string.profile_photo_camera)
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_photo_prompt)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
                .show()
        }
    }

    private fun openGallery() {
        pickVisualMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun openCamera() {
        val ctx = requireContext()
        when {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCameraCapture()

            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        val ctx = requireContext()
        val file = avatarFile()
        try {
            if (file.exists()) {
                file.delete()
            }
            file.parentFile?.mkdirs()
            file.createNewFile()
        } catch (_: Exception) {
            Toast.makeText(ctx, R.string.profile_photo_save_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            file
        )
        takePicture.launch(uri)
    }

    private fun copyUriToAvatarFile(source: Uri) {
        try {
            requireContext().contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(avatarFile()).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.profile_photo_save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun avatarFile(): File = File(requireContext().filesDir, PROFILE_AVATAR_FILE_NAME)

    private fun loadAvatarIntoView() {
        val file = avatarFile()
        if (file.exists() && file.length() > 0L) {
            val pad = 0
            binding.profileImage.setPadding(pad, pad, pad, pad)
            binding.profileImage.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.profileImage.imageTintList = null
            binding.profileImage.load(file) {
                crossfade(true)
                listener(
                    onError = { _, _ -> resetDefaultAvatar() }
                )
            }
        } else {
            resetDefaultAvatar()
        }
    }

    private fun resetDefaultAvatar() {
        val padPx = resources.getDimensionPixelSize(R.dimen.margin_16)
        binding.profileImage.setPadding(padPx, padPx, padPx, padPx)
        binding.profileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.profileImage.setImageResource(R.drawable.ic_person)
        binding.profileImage.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
    }

    private fun setupLogout() {
        binding.fabLogout.setOnClickListener {
            animateClickEffect(binding.fabLogout)
            authManager.logout()
            findNavController().navigate(R.id.to_loginFragment)
        }
    }

    private fun observeDetailUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            val detailViewModel: DetailViewModel by viewModels(ownerProducer = { requireActivity() })
            detailViewModel.updateProfile.collect {
                viewModel.loadBooks()
            }
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        fadeInView(binding.viewPager)
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.saved_books_label)
                1 -> getString(R.string.favorite_count_label)
                2 -> getString(R.string.read_books_label)
                else -> throw IllegalStateException("Invalid position")
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                fadeInView(binding.textReadingCount)
                fadeInView(binding.textFavoriteCount)
                fadeInView(binding.textReadCount)
                binding.textReadingCount.text = stats.savedCount.toString()
                binding.textFavoriteCount.text = stats.favoriteCount.toString()
                binding.textReadCount.text = stats.readCount.toString()
            }
        }
    }

    private fun animateInitialViews() {
        fadeInView(binding.tabLayout)
        fadeInView(binding.profileImage)
    }

    private fun fadeInView(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.isVisible = true
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    private fun animateClickEffect(view: View, duration: Long = 150) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
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

    companion object {
        private const val PROFILE_AVATAR_FILE_NAME = "profile_avatar.jpg"
    }
}
