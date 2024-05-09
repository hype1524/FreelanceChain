package ie.app.freelanchaincode.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ie.app.freelanchaincode.MainActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.databinding.ActivityLoginBinding
import ie.app.freelanchaincode.models.UserModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Đặt hằng số RC_SIGN_IN
    private val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

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

        // Initialize googleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .requestEmail().requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.toSignupActivity.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        //Đăng nhập bằng google với firebase auth
        binding.ggButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("checkUser", "newUser")
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email or password is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        FirebaseFirestore.getInstance().collection("User").document(user?.uid ?: "")
                            .get().addOnSuccessListener { documentSnapshot ->
                            val tempGGUser = documentSnapshot.toObject(UserModel::class.java)
                            if (tempGGUser != null && tempGGUser.signIn) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "This account already signed in!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    recreate()
                                }
                            } else {
                                val userId = tempGGUser?.id ?: ""
                                if (userId.isNotBlank()) {
                                    FirebaseFirestore.getInstance().collection("User")
                                        .document(userId).update("signIn", true)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val intent = Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                                )
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Log.e(
                                                    TAG,
                                                    "Failed to update user sign-in status",
                                                    task.exception
                                                )
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Login failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    Log.e(TAG, "User ID is null or empty")
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to sign in with email and password", task.exception)
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.resetPassword.setOnClickListener {
            val email = binding.email.text.toString()

            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@LoginActivity,
                            "Password reset sent to email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Fill in email to send verification",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    // Handle the result of the sign-in process
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    // Authenticate with Firebase using the Google token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    val profileImage =
                        Uri.parse("android.resource://ie.app.freelanchaincode/${R.drawable.default_profile_picture}")

                    user?.let {
                        if (isNewUser) {
                            val name = it.displayName ?: ""
                            val phoneNumber = it.phoneNumber ?: ""
                            val email = it.email ?: ""

                            val ggUser = UserModel(it.uid, name, phoneNumber, email)
                            val storageReference =
                                FirebaseStorage.getInstance().reference.child("images/${ggUser.id}")

                            val userId = ggUser.id ?: ""

                            FirebaseFirestore.getInstance().collection("User").document(userId)
                                .set(ggUser)
                                .addOnSuccessListener { _ ->
                                    storageReference.putFile(profileImage)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val checkUser = intent.getStringExtra("checkUser")
                                                val intent = Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                                )
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Log.e(
                                                    TAG,
                                                    "Failed to upload profile image",
                                                    task.exception
                                                )
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Login failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to add user to Firestore", e)
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            FirebaseFirestore.getInstance().collection("User").document(user.uid)
                                .get().addOnSuccessListener { documentSnapshot ->
                                val tempGGUser = documentSnapshot.toObject(UserModel::class.java)
                                if (tempGGUser != null && tempGGUser.signIn) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "This account already signed in!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        recreate()
                                    }
                                } else {
                                    val userId = tempGGUser?.id ?: ""
                                    if (userId.isNotBlank()) {
                                        FirebaseFirestore.getInstance().collection("User")
                                            .document(userId).update("signIn", true)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val intent = Intent(
                                                        this@LoginActivity,
                                                        MainActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    Log.e(
                                                        TAG,
                                                        "Failed to update user sign-in status",
                                                        task.exception
                                                    )
                                                    Toast.makeText(
                                                        this@LoginActivity,
                                                        "Login failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Log.e(TAG, "User ID is null or empty")
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Login failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "GoogleActivity"
    }
}
