package ie.app.freelanchaincode.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.ElasticsearchService
import ie.app.freelanchaincode.models.UserModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.tabs.TabLayoutMediator
import ie.app.freelanchaincode.ProjectSearchFragment
import ie.app.freelanchaincode.adapter.SearchPagerAdapter
import ie.app.freelanchaincode.auth.ProfilePagerAdapter
import ie.app.freelanchaincode.databinding.ActivitySearchBinding
import ie.app.freelanchaincode.models.ProjectModel


class SearchActivity : AppCompatActivity() {
    private var currentUser = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var binding: ActivitySearchBinding
    private val PROJECT : Int = 0
    private val USER : Int = 1


//    override fun onCreateView(savedInstanceState: Bundle?) {
//        // Inflate the layout for this fragment
//        binding = FragmentSearchBinding.inflate(inflater, container, false)
//        setContentView(binding.root)
//        val viewPage = binding
//        binding.rvSearchList.layoutManager = LinearLayoutManager(requireContext())
//
//        return binding.root
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = SearchPagerAdapter( this)
        binding.pager.adapter = pagerAdapter
        val bundle = Bundle()
        bundle.putString("SEARCH_QUERY", "")
        for (i in 0 until pagerAdapter.itemCount) {
            val fragment = pagerAdapter.createFragment(i)
            fragment.arguments = bundle
        }

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = pagerAdapter.getTabTitle(position)
        }.attach()

        binding.searchBar.isSubmitButtonEnabled = true

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println(query)
                if (query != null) {
                    pagerAdapter.updateQuery(query)
                    pagerAdapter.createFragment(PROJECT)
                }
                binding.searchBar.clearFocus()
                binding.searchBar.setQuery("", false)
                // Clear focus after submission
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text change (optional)
                return false
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}