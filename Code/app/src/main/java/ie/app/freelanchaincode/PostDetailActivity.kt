package ie.app.freelanchaincode

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.adapter.CommentAdapter
import ie.app.freelanchaincode.adapter.PostAdapter
import ie.app.freelanchaincode.databinding.ActivityPostDetailBinding
import ie.app.freelanchaincode.main.ChatActivity
import ie.app.freelanchaincode.models.CommentModel
import ie.app.freelanchaincode.models.LikeModel
import java.util.Date

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private var sweetAlertDialog: SweetAlertDialog? = null
    private var commentList: ArrayList<CommentModel>? = null
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share_post_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareProjectDetails()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareProjectDetails() {
        val projectId = intent.getStringExtra("PROJECT_ID")
        val userId = intent.getStringExtra("USER_ID")
        val projName = intent.getStringExtra("PROJ_NAME")
        val projDesc = intent.getStringExtra("PROJ_DESC")
        val projBudget = intent.getIntExtra("PROJ_BUDGET", 0)
        val projAuction = intent.getStringExtra("PROJ_AUCTION")
        val postTime = intent.getStringExtra("POST_TIME")
        val userName = intent.getStringExtra("USER_NAME")
        val skillArray = intent.getStringArrayExtra("SKILL_REQUIRE")

        val shareContent = StringBuilder()
        shareContent.append("PROJECT NAME: $projName\n")
        shareContent.append("\nDESCRIPTION: $projDesc\n")
        shareContent.append("\nBUDGET: $projBudget\n")
        shareContent.append("\nAUCTION: $projAuction\n")
        shareContent.append("\nPOSTED AT: $postTime\n")
        shareContent.append("\nPOSTED BY: $userName\n")
        shareContent.append("\nREQUIRED SKILLS: ${skillArray?.joinToString(", ")}")

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContent.toString())
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share project details via"))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sweetAlertDialog = SweetAlertDialog(this@PostDetailActivity, SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog?.show()

        supportActionBar?.title = intent.getStringExtra("PROJ_NAME")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        val commentRef = db.collection("Comments").document(projectId.toString()).collection("comment")

        commentRef.get()
            .addOnSuccessListener { result ->
                val commentCount = result.size()
                val formattedCommentCount = when {
                    commentCount == 0 -> "0 comment"
                    commentCount == 1 -> "1 comment"
                    else -> "$commentCount comments"
                }
                binding.commentCount.text = formattedCommentCount
            }
            .addOnFailureListener { exception ->
                Log.e("PostDetailActivity", "Error getting comment count", exception)
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
            sweetAlertDialog?.dismiss()
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
                Glide.with(binding.root.context)
                    .load(profilePictureUrl)
                    .into(binding.userImage)
            } else {
                Log.d("Firestore", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "get failed with ", exception)
        }

        db.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).get().addOnSuccessListener { document ->
            if (document != null) {
                val profilePictureUrl = document.getString("profilePictureUrl")
                Glide.with(binding.root.context)
                    .load(profilePictureUrl)
                    .into(binding.userImage2)
            } else {
                Log.d("Firestore", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "get failed with ", exception)
        }

        binding.sendComment.setOnClickListener {
            val commentContent = binding.commentContent.text.toString().trim()
            if (projectId != null && currentUserId != null && commentContent.isNotEmpty()) {
                saveComment(db, projectId, currentUserId, commentContent)
            } else {
            }
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
            if (userId != null) {
                if (userId != currentUserId) {
                    val members: List<String> = listOf(currentUserId, userId)
                    RoomChatUtil.getOrCreateRoomChatByMembers(members) { roomChatId ->
                        val bundle = Bundle().apply {
                            putString("roomChatId", roomChatId)
                        }
                        intent.putExtras(bundle)
                        this.startActivity(intent)
                    }
                } else {
                    Toast.makeText(this,"You can't message with yourself",Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText( this,"Owner of this project not found", Toast.LENGTH_SHORT).show()
            }
        }

        getCommentList()

        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.layoutManager = LinearLayoutManager(this)

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this@PostDetailActivity)
        binding.rvComment.adapter = commentAdapter
    }

    private fun saveComment(db: FirebaseFirestore, projectId: String, userId: String, commentContent: String) {
        val commentData = hashMapOf(
            "userId" to userId,
            "commentContent" to commentContent,
            "timestamp" to Timestamp.now()
        )

        db.collection("Comments").document(projectId)
            .collection("comment")
            .add(commentData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this@PostDetailActivity, "Bình luận đã được đăng thành công.", Toast.LENGTH_SHORT).show()
                binding.commentContent.setText("")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding comment", e)
                Toast.makeText(this@PostDetailActivity, "Đã xảy ra lỗi khi đăng bình luận.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCommentList() {
        val db = FirebaseFirestore.getInstance()
        val projectId = intent.getStringExtra("PROJECT_ID")

        projectId?.let {
            val commentRef = db.collection("Comments").document(projectId).collection("comment")
            commentRef.addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (value != null) {
                    val comments = ArrayList<CommentModel>()
                    for (doc in value) {
                        val userId = doc.getString("userId") ?: ""
                        val commentContent = doc.getString("commentContent") ?: ""
                        val timestamp = doc.getDate("timestamp") ?: Date()
                        val comment = CommentModel(userId, commentContent, timestamp)
                        comments.add(comment)
                    }
                    commentAdapter.setCommentList(comments)
                } else {
                    Log.d("Firestore", "No comments")
                }
            }
        }
    }
}
