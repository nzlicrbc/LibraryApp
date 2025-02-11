package com.example.libraryapp.ui.profile.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.libraryapp.ui.profile.FavoriteBooksFragment
import com.example.libraryapp.ui.profile.ReadBooksFragment
import com.example.libraryapp.ui.profile.SavedBooksFragment

class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SavedBooksFragment()
            1 -> FavoriteBooksFragment()
            2 -> ReadBooksFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}