package ie.app.freelanchaincode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.models.CommentModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CommentAdapter(private val context: Context) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private var commentList: List<CommentModel> = ArrayList()
    private val userNameCache = mutableMapOf<String, String>()
    private val userProfilePictureCache = mutableMapOf<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(comment: CommentModel) {
            itemView.findViewById<TextView>(R.id.comment_content).text = comment.commentContent
            itemView.findViewById<TextView>(R.id.comment_time).text = formatTimestamp(comment.timestamp)

            val userNameView = itemView.findViewById<TextView>(R.id.user_name)
            val userImageView = itemView.findViewById<ImageView>(R.id.user_image)

            val cachedUserName = userNameCache[comment.userId]
            if (cachedUserName != null) {
                userNameView.text = cachedUserName
            } else {
                fetchAndSetUserName(comment.userId, userNameView)
            }

            val cachedProfilePictureUrl = userProfilePictureCache[comment.userId]
            if (cachedProfilePictureUrl != null) {
                Glide.with(context).load(cachedProfilePictureUrl).into(userImageView)
            } else {
                fetchAndSetUserProfilePicture(comment.userId, userImageView)
            }
        }

        private fun formatTimestamp(timestamp: Date): String {
            val now = Date()
            val diff = now.time - timestamp.time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                minutes < 60 -> "$minutes mins ago"
                hours < 24 -> "$hours hours ago"
                else -> {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.format(timestamp)
                }
            }
        }

        private fun fetchAndSetUserName(userId: String, textView: TextView) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("name") ?: "Unknown"
                        textView.text = userName
                        userNameCache[userId] = userName
                    } else {
                        textView.text = "Unknown"
                    }
                }
                .addOnFailureListener {
                    textView.text = "Unknown"
                }
        }

        private fun fetchAndSetUserProfilePicture(userId: String, imageView: ImageView) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                        Glide.with(context).load(profilePictureUrl).into(imageView)
                        userProfilePictureCache[userId] = profilePictureUrl
                    } else {
                        Glide.with(context).load(R.drawable.default_profile_picture).into(imageView)
                    }
                }
                .addOnFailureListener {
                    Glide.with(context).load(R.drawable.default_profile_picture).into(imageView)
                }
        }
    }

    fun setCommentList(comments: List<CommentModel>) {
        commentList = comments.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}
