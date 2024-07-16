package ie.app.freelanchaincode.models

import android.service.autofill.CustomDescription
import com.google.firebase.Timestamp

data class ProjectModel (
    var user_id : String? = null,
    var time : Timestamp? = null,
    var id: String? = null,
    var owner : String? = null,
    var name: String? = null,
    var description: String? = null,
    var budget: Int? = null,
    var kindOfPay: String? = null,
    var skillRequire: List<String> = emptyList(),
    var isBidded: Boolean = false,
)