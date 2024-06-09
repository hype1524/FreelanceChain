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
import ie.app.freelanchaincode.RoomChatUtil
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
            RoomChatUtil.onCreateRoomChat(temp)
        }
        adapter = RoomChatAdapter(requireContext())

        binding.rvChatList.adapter = adapter

        RoomChatUtil.getRoomChatByUserId(currentUser!!, { chatRooms ->
            adapter.setList(chatRooms)
            adapter.notifyDataSetChanged()
        })


    }
}