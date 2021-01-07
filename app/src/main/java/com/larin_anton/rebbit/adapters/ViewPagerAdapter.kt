package com.larin_anton.rebbit.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// TODO Refactoring REQUIRE
class ViewPagerAdapter(private val listOfFragments: List<com.larin_anton.rebbit.models.Fragment>, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return listOfFragments.size
    }

    override fun getItem(position: Int): Fragment {
        return listOfFragments[position].fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listOfFragments[position].title
    }
}