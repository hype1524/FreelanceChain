package ie.app.freelanchaincode.models

import com.google.firebase.firestore.DocumentId

data class RoomChatModel(
    @DocumentId val id : String = "",
    val name: String ? = "Chat Room",
//    var profilePictureUrl: String? = null,
    val members: List<String> ?= emptyList(),
    val messages: List<MessageModel> ?= emptyList(),
)
