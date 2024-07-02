package ie.app.freelanchaincode.models

import java.util.Date

data class UserModel(
    var id: String? = null,
    var name: String? = null,
    var number: String? = null,
    var email: String? = null,
    var signIn: Boolean = false,
    var profilePictureUrl: String? = null,
    var bio: String? = null,
    var skills: List<String>? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var contacts: List<String>? = null,
    var works: Map<String, String>? = null,
)


