package ie.app.freelanchaincode

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.databinding.ActivityPostDetailBinding
import ie.app.freelanchaincode.main.ChatActivity
import ie.app.freelanchaincode.models.LikeModel

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra("PROJ_NAME")

        val projectId = intent.getStringExtra("PROJECT_ID")
        val userId = intent.getStringExtra("USER_ID")
        val projName = intent.getStringExtra("PROJ_NAME")
        val projDesc = intent.getStringExtra("PROJ_DESC")
        val projBudget = intent.getIntExtra("PROJ_BUDGET", 0)
        val projAuction = intent.getStringExtra("PROJ_AUCTION")
        val postTime = intent.getStringExtra("POST_TIME")
        val userName = intent.getStringExtra("USER_NAME")
        val skillArray = intent.getStringArrayExtra("SKILL_REQUIRE")

        Log.d("PostDetailActivity", "Project ID: $projectId")
        Log.d("PostDetailActivity", "User ID: $userId")
        Log.d("PostDetailActivity", "Project Name: $projName")
        Log.d("PostDetailActivity", "Project Description: $projDesc")
        Log.d("PostDetailActivity", "Project Budget: $projBudget")
        Log.d("PostDetailActivity", "Project Auction: $projAuction")
        Log.d("PostDetailActivity", "Post Time: $postTime")
        Log.d("PostDetailActivity", "User Name: $userName")
        Log.d("PostDetailActivity", "Skills: ${skillArray?.joinToString()}")

        findViewById<TextView>(R.id.proj_name).text = projName
        findViewById<TextView>(R.id.proj_desc).text = projDesc
        findViewById<TextView>(R.id.proj_budget).text = "Budget: " + projBudget.toString()
        findViewById<TextView>(R.id.post_time).text = postTime
        findViewById<TextView>(R.id.user_name).text = userName

        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.e("FirebaseAuth", "Current user is not authenticated.")
            return
        }

        val likesRef = db.collection("Likes").document(projectId.toString())

        likesRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val likeModel = document.toObject(LikeModel::class.java)
                if (likeModel != null) {
                    val likeCount = likeModel.likes.size
                    binding.likeCount.text = when (likeCount) {
                        0 -> "0 like"
                        1 -> "1 like"
                        else -> "$likeCount likes"
                    }
                    if (likeModel.likes.contains(currentUserId)) {
                        binding.like.setColorFilter(binding.root.context.getColor(R.color.blue))
                    } else {
                        binding.like.setColorFilter(binding.root.context.getColor(R.color.gray))
                    }
                } else {
                    binding.like.setColorFilter(binding.root.context.getColor(R.color.gray))
                    binding.likeCount.text = "0 like"
                }
            } else {
                binding.like.setColorFilter(binding.root.context.getColor(R.color.gray))
                binding.likeCount.text = "0 like"
            }
        }

        binding.like.setOnClickListener {
            likesRef.get().addOnSuccessListener { document ->
                val likeModel: LikeModel
                if (document.exists()) {
                    likeModel = document.toObject(LikeModel::class.java)!!
                    if (likeModel.likes.contains(currentUserId)) {
                        likeModel.likes.remove(currentUserId)
                        binding.like.setColorFilter(binding.root.context.getColor(R.color.gray))
                    } else {
                        likeModel.likes.add(currentUserId)
                        binding.like.setColorFilter(binding.root.context.getColor(R.color.blue))
                    }
                } else {
                    likeModel = LikeModel()
                    likeModel.likes.add(currentUserId)
                    binding.like.setColorFilter(binding.root.context.getColor(R.color.blue))
                }

                likesRef.set(likeModel).addOnSuccessListener {
                    val likeCount = likeModel.likes.size
                    binding.likeCount.text = when (likeCount) {
                        0 -> "0 like"
                        1 -> "1 like"
                        else -> "$likeCount likes"
                    }
                    if (likeModel.likes.contains(currentUserId)) {
                    } else {
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error setting like: ", e)
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error getting document: ", e)
            }
        }

        val docRef = db.collection("User").document(userId.toString())
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                Log.d("Firestore", "DocumentSnapshot data: ${document.data}")
                binding.userName.text = document.getString("name").toString()
                val profilePictureUrl = document.getString("profilePictureUrl")
                // Load user image using Glide
                Glide.with(binding.root.context)
                    .load(profilePictureUrl)
                    .into(binding.userImage)
            } else {
                Log.d("Firestore", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "get failed with ", exception)
        }

        binding.projDesc.post {
            Log.d("PostDetailActivity", "Line count: ${binding.projDesc.lineCount}")
            if (binding.projDesc.lineCount > 5) {
                binding.readMore.visibility = View.VISIBLE
                binding.projDesc.maxLines = 5
                binding.projDesc.ellipsize = TextUtils.TruncateAt.END
            } else {
                binding.readMore.visibility = View.GONE
            }
        }

        binding.readMore.setOnClickListener {
            if (binding.readMore.text == "Xem thêm") {
                binding.projDesc.maxLines = Int.MAX_VALUE
                binding.projDesc.ellipsize = null
                binding.readMore.text = "Thu gọn"
            } else {
                binding.projDesc.maxLines = 5
                binding.projDesc.ellipsize = TextUtils.TruncateAt.END
                binding.readMore.text = "Xem thêm"
            }
        }

        val skillChipGroup: ChipGroup = findViewById(R.id.proj_chip_group)
        skillArray?.forEach { skill ->
            val chip = Chip(this)
            chip.text = skill
            skillChipGroup.addView(chip)
        }

        binding.chatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            val bundle = Bundle().apply {
                putString("roomChatId", projectId)
            }
            intent.putExtras(bundle)
            this.startActivity(intent)
        }
    }
}
