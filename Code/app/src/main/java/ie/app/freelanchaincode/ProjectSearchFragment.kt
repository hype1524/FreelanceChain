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
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.FragmentProjectSearchBinding
import ie.app.freelanchaincode.models.ProjectModel
import ie.app.freelanchaincode.posts.UserPostFragment

class ProjectSearchFragment : Fragment(){

    private lateinit var binding: FragmentProjectSearchBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var projectList: List<ProjectModel>

    private var data: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = it.getString("SEARCH_QUERY")
        }
        Log.d("ProjectSearchOnCreate", data.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProjectSearchBinding.inflate(inflater, container, false)

        data?.let { fetchPostList(it) }

        binding.rvSearchPost.setHasFixedSize(true)
        binding.rvSearchPost.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(requireContext())
        binding.rvSearchPost.adapter = postAdapter

        return binding.root
    }

    fun setList(result: List<ProjectModel>) {
        projectList = result
    }

     fun fetchPostList(data : String) {
         Log.d("FetchPostList", data)
        ElasticsearchService.getSearchResult(data, "project", listOf("name","description","skillRequire")) { uidList ->
//            fetchProject(uidList) {result ->
////                setList(result)
//                println(result)
//            }
            println(uidList)
        }
    }

    companion object {
        fun  newInstance() =
            ProjectSearchFragment()
    }

    private fun fetchProject(uids: List<String>, onResult: (List<ProjectModel>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val projects = mutableListOf<ProjectModel>()
        val userCollection = db.collection("Project")
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
                    val project = document.toObject(ProjectModel::class.java)
                    if (project != null) {
                        projects.add(project)
                    }
                }
                onResult(projects)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

}