package ie.app.freelanchaincode.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.FragmentHomeBinding
import ie.app.freelanchaincode.models.ProjectModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.rvHomePost.setHasFixedSize(true)
        binding.rvHomePost.layoutManager = LinearLayoutManager(requireContext())

        projectList = ArrayList()
        postAdapter = PostAdapter(requireContext())
        binding.rvHomePost.adapter = postAdapter

        val sampleProject = ProjectModel(
            id = "1",
            owner = "1",
            name = "Sample Project",
            description = "This is a sample project",
            budget = 100,
            kindOfPay = "Hourly",
            skillRequire = listOf("Java", "Kotlin", "Android"))
        projectList.add(sampleProject)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
