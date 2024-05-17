package ie.app.freelanchaincode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.models.ProjectModel

class PostAdapter(private val context: Context) :
    RecyclerView.Adapter<PostAdapter.ProjectModelViewHolder>() {

    private var projectList: List<ProjectModel> = ArrayList()
    private var timeMarks: List<String>? = null

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
        val item = projectList[position]

        // Bind user data
//        holder.userName.text = item.getUserName()
//        holder.postTime.text = item.getPostTime()
//        Glide.with(context).load(item.getUserImage()).into(holder.userImage)
//
//        // Bind project data
//        holder.projName.text = item.getProjectName()
//        holder.projBudget.text = item.getProjectBudget()
//        holder.projAuction.text = item.getProjectAuction()
//        holder.projDesc.text = item.getProjectDescription()
//        holder.tags.text = item.getProjectTags()

        holder.like.setOnClickListener {
        }

        holder.comment.setOnClickListener {
        }
    }

    override fun getItemCount(): Int {
        return projectList.size
    }

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
    }
}
