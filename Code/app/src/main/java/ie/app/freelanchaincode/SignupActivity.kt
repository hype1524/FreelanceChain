package ie.app.freelanchaincode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ie.app.freelanchaincode.databinding.ActivitySignupBinding
import ie.app.freelanchaincode.models.UserModel

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val allCountryRegex = Regex("0\\d{9}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toLoginActivity.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.visibleButton.setOnClickListener {
            val isPasswordVisible = binding.password.transformationMethod == null

            if (isPasswordVisible) {
                binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.visibleButton.setImageResource(R.drawable.invisible)
            } else {
                binding.password.transformationMethod = null
                binding.visibleButton.setImageResource(R.drawable.visible)
            }
        }

        binding.visibleButtonConfirm.setOnClickListener {
            val isPasswordVisible = binding.confirmPassword.transformationMethod == null

            if (isPasswordVisible) {
                binding.confirmPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.visibleButtonConfirm.setImageResource(R.drawable.invisible)
            } else {
                binding.confirmPassword.transformationMethod = null
                binding.visibleButtonConfirm.setImageResource(R.drawable.visible)
            }
        }

        binding.signupButton.setOnClickListener {
            // Get user input
            val fullName = binding.fullName.text.toString()
            val number = binding.mobileNumber.text.toString()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            // Check if all fields are filled
            if (password.isNotBlank() && email.isNotBlank() && confirmPassword.isNotBlank() && fullName.isNotBlank() && number.isNotBlank()) {
                if (number.matches(allCountryRegex)) {
                    if (password == confirmPassword) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                // Sign up successful
                                Toast.makeText(
                                    this@SignupActivity, "Sign up successfully", Toast.LENGTH_SHORT
                                ).show()

                                // Start LoginActivity
                                val loginIntent =
                                    Intent(this@SignupActivity, LoginActivity::class.java)
                                startActivity(loginIntent)

                                // Add user data to Firestore
                                firebaseAuth.currentUser?.uid?.let { uid ->
                                    val user = UserModel(uid, fullName, number, email)
                                    firebaseFirestore.collection("User").document(uid).set(user)
                                        .addOnSuccessListener {
                                            // User data added successfully
                                        }.addOnFailureListener { e ->
                                            // Failed to add user data
                                            Toast.makeText(
                                                this@SignupActivity,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }

                                // Upload default profile image to Firebase Storage
                                val defaultImage =
                                    Uri.parse("android.resource://${packageName}/${R.drawable.default_profile_picture}")
                                val reference =
                                    storage.reference.child("images/${FirebaseAuth.getInstance().uid}")
                                reference.putFile(defaultImage)
                            }.addOnFailureListener { e ->
                                // Sign up failed
                                Toast.makeText(this@SignupActivity, e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } else {
                        // Passwords do not match
                        Toast.makeText(
                            this@SignupActivity, "Reconfirm your password", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Invalid phone number format
                    Toast.makeText(
                        this@SignupActivity,
                        "The phone number is badly formatted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Not all fields are filled
                Toast.makeText(
                    this@SignupActivity, "Please fill in all the information", Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
}
