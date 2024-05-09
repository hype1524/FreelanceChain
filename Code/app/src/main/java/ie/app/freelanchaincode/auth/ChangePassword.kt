package ie.app.freelanchaincode.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import ie.app.freelanchaincode.databinding.ActivityChangepasswordBinding

class ChangePassword : AppCompatActivity() {

    private lateinit var binding:ActivityChangepasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangepasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.changePassword.setOnClickListener {
            val oldPassword = binding.oldPass.text.toString()
            val newPassword = binding.newPass.text.toString()
            val confirmPass = binding.confirmPass.text.toString()
            changePassword(this@ChangePassword, oldPassword, newPassword, confirmPass)
        }

        binding.goBack.setOnClickListener {
            Intent (
                this@ChangePassword,
                EditProfileActivity::class.java)
                .also { startActivity(it) }
        }
    }

    private fun changePassword(context: Context, oldPassword: String, newPassword: String, confirmPassword: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (newPassword != confirmPassword) {
            Toast.makeText(context, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            // Re-authenticate the user with their current password
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // Password re-authentication successful, now update the password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful) {
                                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to update password: ${updatePasswordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to re-authenticate: ${reAuthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}