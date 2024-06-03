package ie.app.freelanchaincode.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class MessageModel(
    @ServerTimestamp val createdAt: Timestamp?= null,
    val sender : String ?= "",
    val content : String ?= "",
)