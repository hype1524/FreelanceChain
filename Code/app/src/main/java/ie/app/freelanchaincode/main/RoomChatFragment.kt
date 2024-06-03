package ie.app.freelanchaincode.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import androidx.navigation.NavController
//import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ie.app.freelanchaincode.adapter.RoomChatAdapter
import ie.app.freelanchaincode.databinding.FragmentRoomchatBinding
import ie.app.freelanchaincode.models.MessageModel
import ie.app.freelanchaincode.models.RoomChatModel


class RoomChatFragment : Fragment() {
    private lateinit var adapter : RoomChatAdapter
    private lateinit var binding: FragmentRoomchatBinding
//    private lateinit var navController: NavController

    private var currentUser = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentRoomchatBinding.inflate(inflater, container, false)

        binding.rvChatList.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.roomChatCreateBtn.setOnClickListener {
            val temp = listOf(currentUser!!)
            onCreateRoomChat("Room chat demo", temp)
        }

//        navController = findNavController()
//        adapter = RoomChatAdapter(navController)
        adapter = RoomChatAdapter(requireContext())

        binding.rvChatList.adapter = adapter

        getRoomChatByUserId(currentUser!!)


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getRoomChatByUserId(userId: String) {
        var chatRooms : List<RoomChatModel>
        FirebaseFirestore.getInstance()
            .collection("RoomChat")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { documents ->
                chatRooms = documents.mapNotNull { document ->
                    document.toObject(RoomChatModel::class.java)
                }
                adapter.setList(chatRooms)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("GetRoomChat", "GetRoom chat fail: $e")
            }
    }

    private fun getRoomChatByRoomId(roomChatId: String, onComplete: (RoomChatModel?) -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("RoomChat")
            .document(roomChatId).get()
            .addOnSuccessListener { documents ->
                val roomChat = documents.toObject(RoomChatModel::class.java)
                onComplete(roomChat)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    private fun onCreateRoomChat(name: String, members: List<String>, messages: List<MessageModel> = emptyList()) {
        if (members.isEmpty()) {
            Log.w("On Create room chat", "members of room is empty")
        } else {
            val newRoomChat = RoomChatModel(
                name = name,
                members = members,
                messages = messages
            )

            FirebaseFirestore.getInstance()
                .collection("RoomChat")
                .add(newRoomChat)
                .addOnSuccessListener {
                    Log.d("On Create room chat", "")
                }
                .addOnFailureListener { e ->
                    Log.e("On Create room chat", "create new room chat fail $e")
                }
        }
    }
}