//class EditProfileActivity : AppCompatActivity() {
////    private  lateinit var auth: FirebaseAuth
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_edit)
//    }
////        auth = FirebaseAuth.getInstance()
////        var user = auth.currentUser

//}

package ie.app.freelanchaincode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import ie.app.freelanchaincode.databinding.ActivityEditBinding
import ie.app.freelanchaincode.models.UserModel


class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEditBinding
    private lateinit var myText: TextView
//    private lateinit var
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            Intent (
                this@EditProfileActivity,
                LoginActivity::class.java
            ).also { startActivity(it) }
        } else {
            FirebaseFirestore.getInstance().collection("User").document(user?.uid ?: "")
                .get()
                .addOnSuccessListener { document ->
                    val userData = document.toObject(UserModel::class.java)
                    if (userData != null) {
                        binding.mobileNumberEdit.setText(userData.number)
                        binding.mailAddressEdit.setText(userData.email)
                        binding.userNameEdit.setText(userData.name)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@EditProfileActivity, "Get error",Toast.LENGTH_SHORT).show()
                }
        }

        binding.goMain.setOnClickListener {
            Intent (
                this@EditProfileActivity,
                MainActivity::class.java
            ).also { startActivity(it) }
        }

        binding.signOut.setOnClickListener {

        }

        binding.saveChange.setOnClickListener {
            val userUpdate = hashMapOf(
                "name" to binding.userNameEdit.text.toString(),
                "email" to binding.mailAddressEdit.text.toString(),
                "number" to binding.mobileNumberEdit.text.toString(),
            )
            FirebaseFirestore.getInstance().collection("User").document(user?.uid ?: "")
                .set(userUpdate, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this@EditProfileActivity, "Success update your profile", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{exeption ->
                    Toast.makeText(this@EditProfileActivity, "Update Profile faile with ${exeption.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}