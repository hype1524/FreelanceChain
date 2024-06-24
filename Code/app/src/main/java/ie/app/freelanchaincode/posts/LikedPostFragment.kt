package ie.app.freelanchaincode.posts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.FragmentLikedPostBinding
import ie.app.freelanchaincode.models.ProjectModel
import java.util.Collections
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LikedPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LikedPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentLikedPostBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var projectList: ArrayList<ProjectModel>
    private var userId: String? = null

    private var sweetAlertDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("USER_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikedPostBinding.inflate(inflater, container, false)

        getPostList()
        Log.d("USERPOSTFRAGMENT", "onCreate: $userId")

        binding.rvLikedPost.setHasFixedSize(true)
        binding.rvLikedPost.layoutManager = LinearLayoutManager(requireContext())

        projectList = ArrayList()
        postAdapter = PostAdapter(requireContext())
        binding.rvLikedPost.adapter = postAdapter

        return binding.root
    }

    private fun getPostList() {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog?.show()

        currentUserId?.let { uid ->
            db.collection("Likes")
                .whereArrayContains("likes", currentUserId)
                .get()
                .addOnSuccessListener { likesQuerySnapshot ->
                    val likedProjectIds = likesQuerySnapshot.documents.map { it.id }

                    Log.d("LikedPostFragment", "Liked project IDs: $likedProjectIds")


                    val postList = mutableListOf<ProjectModel>()

                    sweetAlertDialog?.dismiss()

                    if (likedProjectIds.isNotEmpty()) {
                        val tasks = if (likedProjectIds.size > 10) {
                            likedProjectIds.chunked(10).map { chunk ->
                                db.collection("Project").whereIn(FieldPath.documentId(), chunk)
                                    .get()
                            }
                        } else {
                            listOf(
                                db.collection("Project")
                                    .whereIn(FieldPath.documentId(), likedProjectIds).get()
                            )
                        }

                        Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val postList = mutableListOf<ProjectModel>()
                                val timeMarks = mutableListOf<String>()
                                val currentDate = Date()

                                for (querySnapshot in task.result) {
                                    for (doc in querySnapshot.documents) {
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
                                        if (projectModel.time!!.toDate()
                                                .compareTo(currentDate) > 0
                                        ) {
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
                                }

                                Collections.sort(
                                    postList,
                                    Comparator<ProjectModel> { o1, o2 -> o2.time!!.compareTo(o1.time!!) })
                                postAdapter.setProjectList(postList)
                                postAdapter.setTimeMarks(timeMarks)

                                if (postList.isEmpty()) {
                                    binding.noPost.text = "No more posts found"
                                }
                            } else {
                                Toast.makeText(
                                    activity,
                                    "The server is experiencing an error. Please come back later",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            sweetAlertDialog?.dismiss()
                        }.addOnFailureListener {
                            Toast.makeText(
                                activity,
                                "The server is experiencing an error. Please come back later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        activity,
                        "Error getting liked projects: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(
                activity,
                "User ID is null.",
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
         * @return A new instance of fragment LikedPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userId: String?) =
            LikedPostFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                }
            }
    }
}