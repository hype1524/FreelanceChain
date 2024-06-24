package ie.app.freelanchaincode.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
//import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.main.ChatActivity
import ie.app.freelanchaincode.models.RoomChatModel
import ie.app.freelanchaincode.models.UserModel

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class RoomChatAdapter(private val context: Context) : RecyclerView.Adapter<RoomChatAdapter.MyChatListHolder>() {

    private var listOfChats = listOf<RoomChatModel>()
    private var chatShitModal = RoomChatModel()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomchat_item, parent, false)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomchat_item, parent, false)

        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfChats.size
    }


    fun setList(list: List<RoomChatModel>) {
        this.listOfChats = list
    }

    override fun onBindViewHolder(holder: MyChatListHolder, position: Int) {
        val roomChat = listOfChats[position]
        chatShitModal = roomChat
        holder.itemView.setOnClickListener {
            goToDetailedNotification(roomChat)
        }

        fetchPartner(roomChat, currentUser?.uid.toString()) { user ->
            if (user!= null) {
                holder.chatName.text = user.name
                if (user.profilePictureUrl != null) {
                    Glide.with(context).load(user.profilePictureUrl).into(holder.imageView)
                }
                else {
                    Glide.with(context).load(R.drawable.default_profile_picture).into(holder.imageView)
                }
            }
            else {
                Glide.with(context).load(R.drawable.default_profile_picture).into(holder.imageView)
            }
        }
    }


    private fun goToDetailedNotification(item: RoomChatModel) {
        val intent = Intent(context, ChatActivity::class.java)
        fetchPartner(item, currentUser?.uid.toString()) { user ->
            if (user != null) {
                val bundle = Bundle().apply {
                    putString("roomChatId", item.id)
                    putString("partnerProfilePictureUrl", user.profilePictureUrl)
                    putString("partnerName", user.name)
                }
                intent.putExtras(bundle)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Not found user of room chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPartner(roomChat: RoomChatModel, currentUser: String, onResult: (UserModel?) -> Unit) {
        val userId: String
        if (roomChat.members?.size  == 2) {
            userId = if (roomChat.members[0] != currentUser) {
                roomChat.members[0]
            } else {
                roomChat.members[1]
            }
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        onResult(document.toObject(UserModel::class.java))
                    } else {
                        onResult(null)
                        Log.e("fetchUser", "Not found User")
                    }
                }
                .addOnFailureListener { e ->
                    onResult(null)
                    Log.e("fetchUser", "$e")
                }
        } else {
            onResult(null)
            Log.e("fetchUser", "Members of roomchat not validate")
        }
    }


    inner class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
        val chatName: TextView = itemView.findViewById(R.id.recentChatTextName)
    }
}


