package ie.app.freelanchaincode.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import ie.app.freelanchaincode.MainActivity
import ie.app.freelanchaincode.databinding.ActivityEditBinding
import ie.app.freelanchaincode.models.UserModel


class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEditBinding
    private var auth = FirebaseAuth.getInstance()
    private var storage = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

                    val reference = storage.reference.child("images/${user.uid}")
                    reference.downloadUrl.addOnSuccessListener {
                        binding.imageView.setImageURI(it)
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
            // Update user's sign-in state in Firestore
                val userData = hashMapOf(
                    "signIn" to false
                )
                FirebaseFirestore.getInstance().collection("User").document(user?.uid ?: "")
                .set(userData,SetOptions.merge())
                    .addOnSuccessListener {
                        // Sign out the user from Firebase Authentication
                        auth.signOut()

                        Toast.makeText(this@EditProfileActivity, "Signed out successfully", Toast.LENGTH_SHORT).show()

                         Intent(this@EditProfileActivity, LoginActivity::class.java).also { startActivity(it) }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@EditProfileActivity, "Failed to sign out: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
        }

        binding.passwordEdit.setOnClickListener{
            Intent (
                this@EditProfileActivity,
                ChangePassword::class.java
            ).also { startActivity(it) }
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

        binding.changeAvater.setOnClickListener {
            changeAvatar(this@EditProfileActivity)
        }
    }

    private fun changeAvatar(context: Context) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, 3);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && requestCode == 3) {
            val selectedImage = data.data
            binding.imageView.setImageURI(selectedImage)
            val reference = storage.reference.child(("images/${FirebaseAuth.getInstance().uid}"))
            reference.delete()
                .addOnSuccessListener {
                    val newStorageReference = storage.reference.child("images/${FirebaseAuth.getInstance().uid}")
                    if (selectedImage != null) {
                        newStorageReference.putFile(selectedImage)
                    }
                }

        }
    }
}