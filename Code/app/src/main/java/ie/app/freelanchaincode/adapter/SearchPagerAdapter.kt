package ie.app.freelanchaincode.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import ie.app.freelanchaincode.ElasticsearchService
import ie.app.freelanchaincode.ProjectSearchFragment
import ie.app.freelanchaincode.UserSearchFragment
import ie.app.freelanchaincode.main.RoomChatFragment
import ie.app.freelanchaincode.posts.UserPostFragment

class SearchPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val tabTitles = arrayOf("Projects", "Users")
    private var query: String? = ""

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProjectSearchFragment.newInstance()
            else -> UserSearchFragment.newInstance()
        }
    }

    fun updateQuery(data: String) {
        val fragmentActivity = fragmentActivity
        for (fragment in fragmentActivity.supportFragmentManager.fragments) {
            when (fragment) {
                is ProjectSearchFragment -> fragment.fetchPostList(data)
                is UserSearchFragment -> fragment.fetchUsersList(data)
            }
        }
    }

    fun getTabTitle(position: Int): String = tabTitles[position]
}

