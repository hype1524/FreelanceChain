package ie.app.freelanchaincode.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import ie.app.freelanchaincode.MainActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.databinding.ActivityEditBinding
import ie.app.freelanchaincode.models.UserModel

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    public var profilePicture: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profilePicture = it
            Glide.with(this@EditProfileActivity)
                .load(profilePicture)
                .into(binding.imageView)
            uploadImageToStorage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()

        binding.signOut.setOnClickListener {
            signOutUser()
        }

        binding.passwordEdit.setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
        }

        binding.changeAvater.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_profile_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveUserChanges()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val userData = document.toObject(UserModel::class.java)
                userData?.let {
                    binding.mobileNumberEdit.setText(it.number)
                    binding.mailAddressEdit.setText(it.email)
                    binding.userNameEdit.setText(it.name)
                    setupUI(it.profilePictureUrl)
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error loading user data: ${exception.message}")
            }
    }

    private fun setupUI(profilePictureUrl: String?) {
        if (!profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePictureUrl)
                .into(binding.imageView)
        } else {
            Glide.with(this)
                .load(R.drawable.default_profile_picture)
                .into(binding.imageView)
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun signOutUser() {
        val user = auth.currentUser ?: return
        val userData = hashMapOf("signIn" to false)
        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                auth.signOut()
                showToast("Signed out successfully")
                navigateToLogin()
            }
            .addOnFailureListener { exception ->
                showToast("Failed to sign out: ${exception.message}")
            }
    }

    private fun saveUserChanges() {
        val user = auth.currentUser ?: return
        val userUpdate = hashMapOf(
            "name" to binding.userNameEdit.text.toString(),
            "email" to binding.mailAddressEdit.text.toString(),
            "number" to binding.mobileNumberEdit.text.toString()
        )
        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .set(userUpdate, SetOptions.merge())
            .addOnSuccessListener {
                showToast("Profile updated successfully")
            }
            .addOnFailureListener { exception ->
                showToast("Update failed: ${exception.message}")
            }
    }

    private fun uploadImageToStorage(uri: Uri) {
        val user = auth.currentUser ?: return
        val reference = storage.reference.child("images/${user.uid}")

        reference.putFile(uri)
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfilePictureUrl(downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                showToast("Image upload failed: ${exception.message}")
            }
    }

    private fun updateProfilePictureUrl(url: String) {
        val user = auth.currentUser ?: return
        val userUpdate = hashMapOf("profilePictureUrl" to url)

        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .set(userUpdate, SetOptions.merge())
            .addOnSuccessListener {
                showToast("Profile picture updated successfully")
                Glide.with(this).load(url).into(binding.imageView)
            }
            .addOnFailureListener { exception ->
                showToast("Failed to update profile picture: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
