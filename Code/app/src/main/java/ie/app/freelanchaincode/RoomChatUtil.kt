package ie.app.freelanchaincode

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.models.MessageModel
import ie.app.freelanchaincode.models.RoomChatModel

class RoomChatUtil {

    companion object {
        fun onCreateRoomChat(members: List<String>) {
            if (members.isEmpty()) {
                Log.w("On Create room chat", "members of room is empty")
            } else {
                var id: String = ""
                for (member in members) {
                    id += "$member|"
                }
                val newRoomChat = RoomChatModel(
                    id = id,
                    members = members,
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

        fun getOrCreateRoomChatByMembers(members: List<String>, onResult: (String) -> Unit) {
            if (members.size < 2) {
                throw IllegalArgumentException("Need at least 2 member in a room")
            }
            val sortedMemberIds = members.sorted()
            val roomChatId = generateRoomChatId(sortedMemberIds)

            val roomChatRef =
                FirebaseFirestore.getInstance().collection("RoomChat").document(roomChatId)

            // Check if the RoomChat document exists
            roomChatRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Room chat exists, return the roomChatId
                        onResult(roomChatId)
                    } else {
                        // Room chat doesn't exist, create a new one
                        createNewRoomChat(roomChatRef, sortedMemberIds) { newRoomChatId ->
                            onResult(newRoomChatId)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    throw exception
                }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun getRoomChatByUserId(userId: String, onResult: (List<RoomChatModel>) -> Unit) {
            var chatRooms: List<RoomChatModel>
            FirebaseFirestore.getInstance()
                .collection("RoomChat")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener { documents ->
                    chatRooms = documents.mapNotNull { document ->
                        document.toObject(RoomChatModel::class.java)
                    }
                    onResult(chatRooms)
                }
                .addOnFailureListener { e ->
                    Log.e("GetRoomChat", "GetRoom chat fail: $e")
                }
        }

        private fun generateRoomChatId(memberIds: List<String>): String {
            return memberIds.joinToString(separator = "_")
        }

        private fun createNewRoomChat(
            roomChatRef: DocumentReference,
            memberIds: List<String>,
            onResult: (String) -> Unit
        ) {
            val newRoomChat = RoomChatModel(
                members = memberIds,
                id = roomChatRef.id
            )

            roomChatRef.set(newRoomChat)
                .addOnSuccessListener {
                    onResult(roomChatRef.id)
                }
                .addOnFailureListener { exception ->
                    throw exception
                }
        }
    }
}