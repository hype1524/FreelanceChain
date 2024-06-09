package ie.app.freelanchaincode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.models.ProjectModel
import ie.app.freelanchaincode.models.UserModel

//class SearchAdapter:  RecyclerView.Adapter<SearchAdapter.SearchModelViewHolder>() {
//    private var resultList: List<UserModel> = ArrayList()
//    private var timeMarks: List<String>? = null
//
//    fun setProjectList(resultList: List<UserModel>) {
//        this.resultList = resultList
//        notifyDataSetChanged()
//    }
//
//    fun setTimeMarks(timeMarks: List<String>?) {
//        this.timeMarks = timeMarks
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapter.SearchModelViewHolder {
//        val view =
//            LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
//        return SearchModelViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SearchAdapter.SearchModelViewHolder, position: Int) {
//        val item = resultList[position]
//
//        // Bind user data
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
//
//        holder.like.setOnClickListener {
//        }
//
//        holder.comment.setOnClickListener {
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return resultList.size
//    }
//    inner class SearchModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var userAvatar: CircleImageView = itemView.findViewById(R.id.user_image)
//        var userName: TextView = itemView.findViewById(R.id.userName)
//    }
//}

class SearchAdapter {

}