package ie.app.freelanchaincode.posts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.FragmentHomeBinding
import ie.app.freelanchaincode.databinding.FragmentUserPostBinding
import ie.app.freelanchaincode.models.ProjectModel
import java.util.Collections
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentUserPostBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var projectList: ArrayList<ProjectModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserPostBinding.inflate(inflater, container, false)

        getPostList()

        binding.rvUserPost.setHasFixedSize(true)
        binding.rvUserPost.layoutManager = LinearLayoutManager(requireContext())

        projectList = ArrayList()
        postAdapter = PostAdapter(requireContext())
        binding.rvUserPost.adapter = postAdapter

        return binding.root
    }

    private fun getPostList() {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("item")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val postList = mutableListOf<ProjectModel>()
                    val timeMarks = mutableListOf<String>()
                    val currentDate = Date()

                    for (doc in task.result) {
                        val skillRequire = doc["skillRequire"] as List<String>?
                        val projectModel = ProjectModel(
                            id = doc.id,
                            time = doc.getTimestamp("time")!!,
                            name = doc.getString("name")!!,
                            description = doc.getString("description")!!,
                            kindOfPay = doc.getString("kindOfPay")!!,
                            budget = doc.getLong("budget")!!.toInt(),
                            user_id = doc.getString("user_id")!!,
                            skillRequire = skillRequire ?: emptyList()
                        )
                        if (projectModel.time!!.toDate().compareTo(currentDate) > 0) {
                            Toast.makeText(
                                context,
                                "There is a notification coming from the future. Please check again",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Future notification", projectModel.id.toString())
                            System.exit(0)
                        } else {
                            postList += projectModel
                        }
                        timeMarks += ""
                    }
                    Collections.sort(
                        postList,
                        Comparator<ProjectModel> { o1, o2 -> o2.time!!.compareTo(o1.time!!) })
                    postAdapter.setProjectList(postList)
                    postAdapter.setTimeMarks(timeMarks)

                    if (postList.isNotEmpty()) {
                        binding.noPost.text = "No more posts found"
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "The server is experiencing an error. Please come back later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    activity,
                    "The server is experiencing an error. Please come back later",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}