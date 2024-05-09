package ie.app.freelanchaincode.models

data class UserModel(
    var id: String? = null,
    var name: String? = null,
    var number: String? = null,
    var email: String? = null,
    var signIn: Boolean = false
)