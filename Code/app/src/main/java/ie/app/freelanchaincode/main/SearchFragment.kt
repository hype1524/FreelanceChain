package ie.app.freelanchaincode.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.GetResponse
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.firebase.auth.FirebaseAuth
import ie.app.freelanchaincode.ElasticsearchService
import ie.app.freelanchaincode.adapter.SearchAdapter
import ie.app.freelanchaincode.databinding.FragmentSearchBinding
import io.grpc.okhttp.internal.Platform.logger


class SearchFragment : Fragment() {
    private lateinit var adapter : SearchAdapter
    private lateinit var binding: FragmentSearchBinding

    private var currentUser = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.rvSearchList.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        adapter = RoomChatAdapter(requireContext())
//
//        binding.rvSearchList.adapter = adapter
        binding.searchBar.isSubmitButtonEnabled = true

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                Thread {
                    // Perform your search logic here
                    getSearchResult(query ?: "")
                }.start()

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

    fun getSearchResult(data : String) {
        val client : ElasticsearchClient = ElasticsearchService.client

        println(client.info())

        val responses: SearchResponse<ObjectNode> = client.search(
            { s ->
                s
                    .index("user")
                    .query { q ->
                        q
                            .match { t ->
                                t
                                    .field("username")
                                    .query(data)
                                    .fuzziness("Auto")
                            }
                    }
            },
            ObjectNode::class.java
        )

        for (response in responses.hits().hits()) {
            println(response.toString())
        }

    }

}