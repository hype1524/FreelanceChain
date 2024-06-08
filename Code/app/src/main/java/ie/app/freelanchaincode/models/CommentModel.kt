package ie.app.freelanchaincode.models

import java.util.Date

data class CommentModel(
    val userId: String = "",
    val commentContent: String = "",
    val timestamp: Date = Date()
)
