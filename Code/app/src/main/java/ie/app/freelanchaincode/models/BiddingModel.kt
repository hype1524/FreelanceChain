package ie.app.freelanchaincode.models

import com.google.firebase.Timestamp

class BiddingModel {
    var id: String? = null
    var projectId: String? = null
    var ownerId: String? = null
    var userId: String? = null
    var bidAmount: String? = null
    var bidTime: Timestamp? = null
    var bidStatus: Boolean? = null
    var bidDescription: String? = null
    var bidTitle: String? = null
}