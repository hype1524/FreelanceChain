package ie.app.freelanchaincode.models

import android.service.autofill.CustomDescription

data class ProjectModel (
    var id: String? = null,
    var owner : String? = null,
    var name: String? = null,
    var description: String? = null,
    var budget: Int? = null,
    var kindOfPay: String? = null,
    var skillRequire: List<String> = emptyList()
)