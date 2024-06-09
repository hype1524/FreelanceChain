package ie.app.freelanchaincode.adapter

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.PostDetailActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.auth.ProfileActivity
import ie.app.freelanchaincode.models.CommentModel
import ie.app.freelanchaincode.models.LikeModel
import ie.app.freelanchaincode.models.ProjectModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PostAdapter(private val context: Context) :
    RecyclerView.Adapter<PostAdapter.ProjectModelViewHolder>() {

    private var projectList: List<ProjectModel> = ArrayList()
    private var timeMarks: List<String>? = null

    private var sweetAlertDialog: SweetAlertDialog? = null

    inner class ProjectModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImage: CircleImageView = itemView.findViewById(R.id.user_image)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var postTime: TextView = itemView.findViewById(R.id.post_time)
        var projName: TextView = itemView.findViewById(R.id.proj_name)
        var projBudget: TextView = itemView.findViewById(R.id.proj_budget)
        var projAuction: TextView = itemView.findViewById(R.id.proj_auction)
        var projDesc: TextView = itemView.findViewById(R.id.proj_desc)
        var tags: ChipGroup = itemView.findViewById(R.id.proj_chip_group)
        var like: ImageView = itemView.findViewById(R.id.like)
        var comment: ImageView = itemView.findViewById(R.id.comment)
        var item: CardView = itemView.findViewById(R.id.post_item)
        var likeCount: TextView = itemView.findViewById(R.id.like_count)
        var cardView: CardView = itemView.findViewById(R.id.post_item)
        var userInfo:LinearLayout = itemView.findViewById(R.id.user_info)
        var commentCount: TextView = itemView.findViewById(R.id.comment_count)
    }

    fun setProjectList(projectList: List<ProjectModel>) {
        this.projectList = projectList
        notifyDataSetChanged()
    }

    fun setTimeMarks(timeMarks: List<String>?) {
        this.timeMarks = timeMarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectModelViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return ProjectModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectModelViewHolder, position: Int) {
        val item = this.projectList[position]

        if (item.id != "") {
            sweetAlertDialog = SweetAlertDialog(holder.itemView.context, SweetAlertDialog.PROGRESS_TYPE)
            sweetAlertDialog?.show()
            holder.item.visibility = View.VISIBLE

            val db = FirebaseFirestore.getInstance()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId == null) {
                Log.e("FirebaseAuth", "Current user is not authenticated.")
                return
            }

            val commentRef = db.collection("Comments").document(item.id.toString()).collection("comment")

            commentRef.get()
                .addOnSuccessListener { result ->
                    var isUserCommented = false
                    for (document in result) {
                        val commentModel = document.toObject(CommentModel::class.java)
                        if (commentModel.userId == currentUserId) {
                            isUserCommented = true
                            break
                        }
                    }
                    if (isUserCommented) {
                        holder.comment.setColorFilter(context.getColor(R.color.blue))
                    } else {
                        holder.comment.setColorFilter(context.getColor(R.color.gray))
                    }

                    val commentCount = result.size()
                    val formattedCommentCount = when {
                        commentCount == 0 -> "0 comment"
                        commentCount == 1 -> "1 comment"
                        else -> "$commentCount comments"
                    }
                    holder.commentCount.text = formattedCommentCount
                }
                .addOnFailureListener { exception ->
                    Log.e("PostDetailActivity", "Error getting comment count", exception)
                }


            val likesRef = db.collection("Likes").document(item.id.toString())

            likesRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val likeModel = document.toObject(LikeModel::class.java)
                    if (likeModel != null) {
                        val likeCount = likeModel.likes.size
                        holder.likeCount.text = when (likeCount) {
                            0 -> "0 like"
                            1 -> "1 like"
                            else -> "$likeCount likes"
                        }
                        if (likeModel.likes.contains(currentUserId)) {
                            holder.like.setColorFilter(context.getColor(R.color.blue))
                        } else {
                            holder.like.setColorFilter(context.getColor(R.color.gray))
                        }
                    } else {
                        holder.like.setColorFilter(context.getColor(R.color.gray))
                        holder.likeCount.text = "0 like"
                    }
                } else {
                    holder.like.setColorFilter(context.getColor(R.color.gray))
                    holder.likeCount.text = "0 like"
                }
            }

            holder.like.setOnClickListener {
                likesRef.get().addOnSuccessListener { document ->
                    val likeModel: LikeModel
                    if (document.exists()) {
                        likeModel = document.toObject(LikeModel::class.java)!!
                        if (likeModel.likes.contains(currentUserId)) {
                            likeModel.likes.remove(currentUserId)
                            holder.like.setColorFilter(context.getColor(R.color.gray))
                        } else {
                            likeModel.likes.add(currentUserId)
                            holder.like.setColorFilter(context.getColor(R.color.blue))
                        }
                    } else {
                        likeModel = LikeModel()
                        likeModel.likes.add(currentUserId)
                        holder.like.setColorFilter(context.getColor(R.color.blue))
                    }

                    likesRef.set(likeModel).addOnSuccessListener {
                        val likeCount = likeModel.likes.size
                        holder.likeCount.text = when (likeCount) {
                            0 -> "0 like"
                            1 -> "1 like"
                            else -> "$likeCount likes"
                        }
                        if (likeModel.likes.contains(currentUserId)) {
                        } else {
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error liking post: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error setting like: ", e)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error getting likes: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error getting document: ", e)
                }
            }

            db.collection("Project").document(item.user_id.toString()).collection("item")
                .document(item.id.toString()).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            holder.projName.text = document.getString("name").toString()
                            holder.projDesc.text = document.getString("description").toString()
                            val budget = document.getLong("budget")
                            val timestamp = document.getTimestamp("time")?.toDate()
                            val now = Date()
                            val diff = now.time - timestamp!!.time

                            val postTimeText = when {
                                diff < DateUtils.HOUR_IN_MILLIS -> {
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                                    "$minutes mins ago"
                                }
                                diff < DateUtils.DAY_IN_MILLIS -> {
                                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                                    "$hours hours ago"
                                }
                                else -> {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    sdf.format(timestamp)
                                }
                            }

                            holder.postTime.text = postTimeText
                            val formattedBudget = NumberFormat.getNumberInstance(Locale.US).format(budget)
                            holder.projBudget.text = "Budget: $formattedBudget đ"
                            holder.projAuction.text = "Auction: "
                            val skillRequire = document.get("skillRequire") as List<String>
                            holder.tags.removeAllViews()

                            for (skill in skillRequire) {
                                val chip = Chip(context)
                                chip.text = skill
                                holder.tags.addView(chip)
                            }
                        } else {
                            Log.d("Firestore", "No such document")
                        }
                    } else {
                        Log.d("Firestore", "get failed with ", task.exception)
                    }
                }

            val docRef = db.collection("User").document(item.user_id.toString())
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firestore", "DocumentSnapshot data: ${document.data}")
                    holder.userName.text = document.getString("name").toString()
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    // Load user image using Glide
                    Glide.with(context)
                        .load(profilePictureUrl)
                        .into(holder.userImage)
                } else {
                    Log.d("Firestore", "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }

            holder.comment.setOnClickListener {
                val intent = Intent(context, PostDetailActivity::class.java)
                intent.putExtra("PROJECT_ID", item.id)
                intent.putExtra("USER_ID", item.user_id)
                intent.putExtra("PROJ_NAME", item.name)
                intent.putExtra("PROJ_DESC", item.description)
                intent.putExtra("PROJ_BUDGET", item.budget)
                intent.putExtra("PROJ_AUCTION", "")
                intent.putExtra("POST_TIME", holder.postTime.text.toString())
                intent.putExtra("USER_NAME", holder.userName.text.toString())
                val skillArray = item.skillRequire.toTypedArray()
                intent.putExtra("SKILL_REQUIRE", skillArray)
                context.startActivity(intent)
            }

            holder.userInfo.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("USER_ID", item.user_id)
                context.startActivity(intent)
                Log.d("PUTTTTTT", "User ID: ${item.user_id}")
            }

            holder.cardView.setOnClickListener {
                val intent = Intent(context, PostDetailActivity::class.java)
                intent.putExtra("PROJECT_ID", item.id)
                intent.putExtra("USER_ID", item.user_id)
                intent.putExtra("PROJ_NAME", item.name)
                intent.putExtra("PROJ_DESC", item.description)
                intent.putExtra("PROJ_BUDGET", item.budget)
                intent.putExtra("PROJ_AUCTION", "")
                intent.putExtra("POST_TIME", holder.postTime.text.toString())
                intent.putExtra("USER_NAME", holder.userName.text.toString())
                val skillArray = item.skillRequire.toTypedArray()
                intent.putExtra("SKILL_REQUIRE", skillArray)
                context.startActivity(intent)
            }

            sweetAlertDialog?.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return projectList.size
    }
}
