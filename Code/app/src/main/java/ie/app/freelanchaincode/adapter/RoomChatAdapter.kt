package ie.app.freelanchaincode.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
//import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.main.ChatActivity
import ie.app.freelanchaincode.models.RoomChatModel
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class RoomChatAdapter(private val context: Context) : RecyclerView.Adapter<MyChatListHolder>() {

    private var listOfChats = listOf<RoomChatModel>()
    private var chatShitModal = RoomChatModel()


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
        holder.chatName.text = roomChat.name
        holder.itemView.setOnClickListener {
            goToDetailedNotification(roomChat)
        }

//        Glide.with(holder.itemView.context).load(chatlist.friendsimage).into(holder.imageView)
    }


//    fun setOnItemClickListener(listener: (RoomChatModel) -> Unit) {
//        onClickListener = listener
//    }

    private fun goToDetailedNotification(item: RoomChatModel) {
        val intent = Intent(context, ChatActivity::class.java)
        val bundle = Bundle().apply {
            putString("roomChatId", item.id)
        }
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

}

class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
    val chatName: TextView = itemView.findViewById(R.id.recentChatTextName)
}

