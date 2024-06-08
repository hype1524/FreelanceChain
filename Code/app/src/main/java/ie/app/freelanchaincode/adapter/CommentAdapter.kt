package ie.app.freelanchaincode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.app.freelanchaincode.R

class CommentAdapter(private val context: Context) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 10
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // bind data here
        }
    }
}