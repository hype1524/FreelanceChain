package ie.app.freelanchaincode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.adapter.BiddingAdapter
import ie.app.freelanchaincode.databinding.ActivityBiddingListBinding
import ie.app.freelanchaincode.models.BiddingModel

class BiddingListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiddingListBinding
    private lateinit var adapter: BiddingAdapter
    private lateinit var biddingList: ArrayList<BiddingModel>
    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiddingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Bidding List"

        // Initialize RecyclerView
        binding.rvBiddingList.layoutManager = LinearLayoutManager(this)
        biddingList = ArrayList()
        adapter = BiddingAdapter(this, biddingList)
        binding.rvBiddingList.adapter = adapter

        // Get project ID from Intent
        val projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId != null) {
            fetchBiddingList(projectId)
        }
    }

    private fun fetchBiddingList(projectId: String) {
        adapter.setProjectId(projectId)
        val db = FirebaseFirestore.getInstance()
        db
            .collection("Bidding")
            .whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvNoBiddingFound.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvNoBiddingFound.visibility = android.view.View.GONE
                    for (document in documents) {
                        val bid = document.toObject(BiddingModel::class.java)
                        biddingList.add(bid)
                    }
                    adapter.notifyDataSetChanged()
                }
            }.addOnFailureListener { exception -> }
    }
}
