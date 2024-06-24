package ie.app.freelanchaincode.adapter

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.models.MessageModel
import ie.app.freelanchaincode.models.RoomChatModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter() : RecyclerView.Adapter<MessageHolder>() {

    private var listOfMessage = listOf<MessageModel>()

    private val LEFT = 0
    private val RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == RIGHT) {
            val view = inflater.inflate(R.layout.message_item_right, parent, false)
            MessageHolder(view)
        } else {
            val view = inflater.inflate(R.layout.message_item_left, parent, false)
            MessageHolder(view)
        }
    }

    override fun getItemCount() = listOfMessage.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        holder.messageText.text = message.content
        val timestamp: Date? = message.createdAt?.toDate()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedDate = timestamp?.let { sdf.format(it) } ?: "Unknown time"

        holder.timeOfSent.text = formattedDate

        // Align the item to the right if the message is sent by the current user
        if (getItemViewType(position) == RIGHT) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.width = RecyclerView.LayoutParams.WRAP_CONTENT
            holder.itemView.layoutParams = params
        }
    }

    override fun getItemViewType(position: Int) =
        if (
            listOfMessage[position].sender == FirebaseAuth.getInstance().currentUser?.uid.toString()
        ) RIGHT else LEFT

    fun setList(newList: List<MessageModel>) {
        this.listOfMessage = newList
        Log.d("set list", listOfMessage.toString())
    }

}

class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
    var messageText: TextView = itemView.findViewById(R.id.show_message)
    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
}