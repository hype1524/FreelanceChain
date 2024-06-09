package ie.app.freelanchaincode.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.auth.EditProfileActivity
import ie.app.freelanchaincode.auth.LoginActivity
import ie.app.freelanchaincode.auth.ProfileActivity
import ie.app.freelanchaincode.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSettingBinding
    private var auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        loadUserProfile()

        binding.profileButton.setOnClickListener {
            Intent(this@SettingFragment.context, ProfileActivity::class.java).also { startActivity(it) }
        }

        binding.signOut.setOnClickListener {
            val userData = hashMapOf(
                "signIn" to false
            )
            FirebaseFirestore.getInstance().collection("User").document(user?.uid ?: "")
                .set(userData, SetOptions.merge()).addOnSuccessListener {
                    // Sign out the user from Firebase Authentication
                    auth.signOut()

                    Toast.makeText(
                        this@SettingFragment.context, "Signed out successfully", Toast.LENGTH_SHORT
                    ).show()

                    Intent(this@SettingFragment.context, LoginActivity::class.java).also { startActivity(it) }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@SettingFragment.context,
                        "Failed to sign out: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                binding.profileName.text = document.getString("name")
                val profilePictureUrl = document.getString("profilePictureUrl")
                if (!profilePictureUrl.isNullOrEmpty()) {
                    Glide.with(this@SettingFragment)
                        .load(profilePictureUrl)
                        .into(binding.profileImg)
                } else {
                    Glide.with(this@SettingFragment)
                        .load(R.drawable.default_profile_picture)
                        .into(binding.profileImg)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error loading user profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = SettingFragment().apply {

        }
    }
}
