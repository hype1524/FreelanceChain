package ie.app.freelanchaincode.models

data class RoomChatModel(
    val id : String = "",
    val name: String ? = "Chat Room",
//    var profilePictureUrl: String? = null,
    val members: List<String> ?= emptyList(),
)
