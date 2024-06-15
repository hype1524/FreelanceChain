package ie.app.freelanchaincode

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.adapter.UserAdapter
import ie.app.freelanchaincode.databinding.FragmentUserSearchBinding
import ie.app.freelanchaincode.models.UserModel

class UserSearchFragment : Fragment() {
    private lateinit var binding: FragmentUserSearchBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: List<UserModel>
    private var data: String? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            data = it.getString("SEARCH_QUERY")
//        }
//        Log.d("SearchUserOnCreate", data.toString())
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserSearchBinding.inflate(inflater,container, false)


        binding.rvUserPost.setHasFixedSize(true)
        binding.rvUserPost.layoutManager = LinearLayoutManager(requireContext())

        userList = ArrayList()
        userAdapter = UserAdapter(requireContext())
        binding.rvUserPost.adapter = userAdapter

        return binding.root
    }

    fun setList(list : List<UserModel>) {
        userAdapter.setList(list)
    }

    companion object {
        fun newInstance()  =
            UserSearchFragment()
    }

    fun fetchUsersList(data: String) {
        Log.d("FetchUserList", data)
        ElasticsearchService.getSearchResult(data, "user", listOf("username","email")) { uidList ->
            fetchUsers(uidList) {result ->
                setList(result)
            }
        }
    }

    private fun fetchUsers(uids: List<String>, onResult: (List<UserModel>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val users = mutableListOf<UserModel>()
        val userCollection = db.collection("User")
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        // Fetch user details for each UID
        for (uid in uids) {
            val task = userCollection.document(uid).get()
            tasks.add(task)
        }

        // When all tasks are complete, return the list of users
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject(UserModel::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                onResult(users)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }


}