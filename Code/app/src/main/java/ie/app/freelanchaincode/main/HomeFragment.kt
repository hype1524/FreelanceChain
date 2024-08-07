package ie.app.freelanchaincode.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.FragmentHomeBinding
import ie.app.freelanchaincode.models.ProjectModel
import java.lang.System.exit
import java.util.Collections
import java.util.Date

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var projectList: ArrayList<ProjectModel>
    val db = FirebaseFirestore.getInstance()
    private var sweetAlertDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        getPostList()

        binding.rvHomePost.setHasFixedSize(true)
        binding.rvHomePost.layoutManager = LinearLayoutManager(requireContext())

        projectList = ArrayList()
        postAdapter = PostAdapter(requireContext())
        binding.rvHomePost.adapter = postAdapter
        binding.search.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getPostList() {
        sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog?.show()

        db.collection("Project")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val postList = mutableListOf<ProjectModel>()
                    val timeMarks = mutableListOf<String>()
                    val currentDate = Date()

                    for (doc in task.result) {
                        val skillRequire = doc["skillRequire"] as List<String>?
                        val isBidded = doc.getBoolean("isBidded") ?: false

                        val projectModel = ProjectModel(
                            id = doc.id,
                            time = doc.getTimestamp("time")!!,
                            name = doc.getString("name")!!,
                            description = doc.getString("description")!!,
                            kindOfPay = doc.getString("kindOfPay")!!,
                            budget = doc.getLong("budget")!!.toInt(),
                            user_id = doc.getString("user_id")!!,
                            skillRequire = skillRequire ?: emptyList(),
                            isBidded = isBidded // Gán giá trị đã kiểm tra
                        )

                        if (!isBidded) {
                            postList += projectModel
                        }
                        timeMarks += ""
                    }

                    Collections.sort(postList, Comparator<ProjectModel> { o1, o2 -> o2.time!!.compareTo(o1.time!!) })
                    postAdapter.setProjectList(postList)
                    postAdapter.setTimeMarks(timeMarks)

                    if (postList.isNotEmpty()) {
                        binding.noPost.text = "No more posts found"
                    }
                } else {
                    Toast.makeText(activity, "The server is experiencing an error. Please come back later", Toast.LENGTH_SHORT).show()
                }
                sweetAlertDialog?.dismiss()
            }.addOnFailureListener {
                Toast.makeText(activity, "The server is experiencing an error. Please come back later", Toast.LENGTH_SHORT).show()
                sweetAlertDialog?.dismiss()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}
