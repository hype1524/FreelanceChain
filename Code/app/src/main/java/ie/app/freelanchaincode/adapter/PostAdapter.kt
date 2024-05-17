package ie.app.freelanchaincode.adapter

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
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

    inner class ProjectModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImage: CircleImageView = itemView.findViewById(R.id.user_image)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var postTime: TextView = itemView.findViewById(R.id.post_time)
        var projName: TextView = itemView.findViewById(R.id.proj_name)
        var projBudget: TextView = itemView.findViewById(R.id.proj_budget)
        var projAuction: TextView = itemView.findViewById(R.id.proj_auction)
        var projDesc: TextView = itemView.findViewById(R.id.proj_desc)
        var tags: TextView = itemView.findViewById(R.id.proj_tags)
        var like: ImageView = itemView.findViewById(R.id.like)
        var comment: ImageView = itemView.findViewById(R.id.comment)
        var item: CardView = itemView.findViewById(R.id.post_item)
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
            holder.item.visibility = View.VISIBLE

            val db = FirebaseFirestore.getInstance()

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
                        holder.tags.text = "Tags: " + skillRequire.joinToString(", ")
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

        }

        holder.like.setOnClickListener {
        }

        holder.comment.setOnClickListener {
        }
    }

    override fun getItemCount(): Int {
        return projectList.size
    }
}