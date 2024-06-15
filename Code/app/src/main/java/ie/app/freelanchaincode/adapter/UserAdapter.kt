package ie.app.freelanchaincode.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.auth.ProfileActivity
import ie.app.freelanchaincode.models.UserModel

class UserAdapter(private val context: Context): RecyclerView.Adapter<UserAdapter.MyUserListHolder>() {

    private var listOfUsers = listOf<UserModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyUserListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

        return MyUserListHolder(view)
    }

    override fun getItemCount(): Int = listOfUsers.size

    fun setList(list : List<UserModel>) {
        this.listOfUsers = list
        println(listOfUsers)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyUserListHolder, position: Int) {
        val user = listOfUsers[position]
        holder.itemView.setOnClickListener {
//            goToUserProfile(user)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            context.startActivity(intent)
        }

        Glide.with(context).load(user.profilePictureUrl).into(holder.imageView)
        holder.chatName.text = user.name

    }

    inner class MyUserListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: CircleImageView = itemView.findViewById(R.id.userAvatar)
        val chatName: TextView = itemView.findViewById(R.id.user_item_name)
    }

}