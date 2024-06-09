package ie.app.freelanchaincode.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ie.app.freelanchaincode.posts.CommentedPostFragment
import ie.app.freelanchaincode.posts.LikedPostFragment
import ie.app.freelanchaincode.posts.UserPostFragment

class ProfilePagerAdapter(private val userId: String?, fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val tabTitles = arrayOf("Personal", "Liked", "Commented")

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserPostFragment.newInstance(userId)
            1 -> LikedPostFragment.newInstance(userId)
            else -> CommentedPostFragment.newInstance(userId)
        }
    }

    fun getTabTitle(position: Int): String {
        return tabTitles[position]
    }
}

class ProfilePagerAdapterSingleFragment(private val userId: String?, fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return UserPostFragment.newInstance(userId ?: "")
    }
}

