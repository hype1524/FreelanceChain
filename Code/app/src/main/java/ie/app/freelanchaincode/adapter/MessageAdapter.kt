package ie.app.freelanchaincode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.models.MessageModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageHolder>() {

    private var listOfMessage = listOf<MessageModel>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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
    }

    override fun getItemViewType(position: Int): Int {
        val message = listOfMessage[position]
        return if (message.sender == currentUserId) RIGHT else LEFT
    }

    fun setList(newList: List<MessageModel>) {
        this.listOfMessage = newList
        notifyDataSetChanged()
    }

    class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById(R.id.show_message)
        val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
    }
}
