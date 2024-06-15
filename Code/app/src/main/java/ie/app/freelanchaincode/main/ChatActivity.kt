package ie.app.freelanchaincode.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ie.app.freelanchaincode.MainActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.adapter.MessageAdapter
import ie.app.freelanchaincode.databinding.ActivityChatBinding
//import ie.app.freelanchaincode.databinding.FragmentChatBinding
import ie.app.freelanchaincode.models.MessageModel

//class ChatActivity(private val roomChatId: String) : Fragment() {
//
//    lateinit var adapter : MessageAdapter
//    lateinit var toolbar: Toolbar
//    private lateinit var binding: FragmentChatBinding
//    private var auth = FirebaseAuth.getInstance().currentUser?.uid
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        binding = FragmentChatBinding.inflate(layoutInflater, container, false )
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        adapter = MessageAdapter()
//
//        binding.chatBackBtn.setOnClickListener {
//            onBackPressed()
//        }
//
//        binding.rvMessageList.adapter = adapter
//
//        binding.sendBtn.setOnClickListener{
//            val message = binding.editTextMessage.text.toString()
//            onCreateMessage(auth, message, roomChatId)
//            binding.editTextMessage.text.clear()
//        }
//
//        getMessageOfRoom(roomChatId)
//    }
//
//    private fun onBackPressed() {
//        val fragmentManager = parentFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.frame_layout, RoomChatFragment())
//        fragmentTransaction.commit()
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun getMessageOfRoom(roomChatId: String) {
//        FirebaseFirestore.getInstance()
//            .collection("RoomChat").document(roomChatId)
//            .collection("Message")
//            .orderBy("createdAt", Query.Direction.ASCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    Log.e("Get Message Of Room", "Get message error: $error")
//                }
//
//                if (snapshot != null) {
//                    val messages = snapshot.mapNotNull {document ->
//                        document.toObject(MessageModel::class.java)
//                    }
//                    adapter.setList(messages)
//                    adapter.notifyDataSetChanged()
//                }
//            }
//    }
//
//    private fun onCreateMessage(sender: String?, content: String?, roomChatId: String) {
//        val newMessage = MessageModel(
//            createdAt = null,
//            sender =  sender,
//            content = content,
//        )
//        FirebaseFirestore.getInstance()
//            .collection("RoomChat").document(roomChatId)
//            .collection("Message").add(newMessage)
//            .addOnSuccessListener {
//            }.addOnFailureListener { e ->
//                Log.e("Create Message", "Create Message error $e")
//            }
//    }
//}

class ChatActivity() : AppCompatActivity() {

    private lateinit var adapter : MessageAdapter
    private lateinit var binding: ActivityChatBinding
    private lateinit var roomChatId: String
    private var partnerProfilePictureUrl: String ? = null
    private var partnerName: String ? = null
    private var auth = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        val bundle = intent.extras
        if (bundle != null) {
            roomChatId = bundle.getString("roomChatId")!!
            partnerProfilePictureUrl = bundle.getString("partnerProfilePictureUrl")
            partnerName = bundle.getString("partnerName")
        }

        setContentView(binding.root)

        binding.rvMessageList.layoutManager = LinearLayoutManager(this)

        binding.chatBackBtn.setOnClickListener {
            onBackPressed()
        }

        if (partnerProfilePictureUrl != null) {
            Glide.with(this).load(partnerProfilePictureUrl).into(binding.chatImageViewUser)
        } else {
            Glide.with(this).load(R.drawable.default_profile_picture).into(binding.chatImageViewUser)
        }

        if (partnerName != null) {
            binding.chatUserName.text = partnerName
        } else {
            binding.chatUserName.text = "User Name"
        }




        adapter = MessageAdapter()

        binding.rvMessageList.adapter = adapter

        binding.sendBtn.setOnClickListener{
            val message = binding.editTextMessage.text.toString()
            onCreateMessage(auth, message, roomChatId)
            binding.editTextMessage.text.clear()
        }

        getMessageOfRoom(roomChatId)
    }

    override fun onBackPressed() {
        val intent = Intent(this@ChatActivity, MainActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMessageOfRoom(roomChatId: String) {
        FirebaseFirestore.getInstance()
            .collection("RoomChat").document(roomChatId)
            .collection("Message")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Get Message Of Room", "Get message error: $error")
                }

                if (snapshot != null) {
                    val messages = snapshot.mapNotNull {document ->
                        document.toObject(MessageModel::class.java)
                    }
                    adapter.setList(messages)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun onCreateMessage(sender: String?, content: String?, roomChatId: String) {
        val newMessage = MessageModel(
            createdAt = null,
            sender =  sender,
            content = content,
        )
        FirebaseFirestore.getInstance()
            .collection("RoomChat").document(roomChatId)
            .collection("Message").add(newMessage)
            .addOnSuccessListener {
            }.addOnFailureListener { e ->
                Log.e("Create Message", "Create Message error $e")
            }
    }
}