package ie.app.freelanchaincode.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import ie.app.freelanchaincode.MainActivity
import ie.app.freelanchaincode.PostProjectActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private var sweetAlertDialog: SweetAlertDialog? = null
    var profilePicture: Uri? = null
    private val REQUEST_POST_PROJECT = 1001
    private var userName: String? = null
    private var userId: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profilePicture = it
                if (!isDestroyed) {
                    Glide.with(this@ProfileActivity).load(profilePicture).into(binding.profileImg)
                }
                uploadImageToStorage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID")

        Log.d("ProfileActivity", "onCreate: $userId")

        supportActionBar?.title = "Profile"

        val viewPager: ViewPager2 = findViewById(R.id.pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        val pagerAdapter = if (userId != null && userId != auth.currentUser?.uid) {
            ProfilePagerAdapterSingleFragment(this)
        } else {
            ProfilePagerAdapter(this)
        }
        viewPager.adapter = pagerAdapter

        val bundle = Bundle()
        bundle.putString("USER_ID", userId)
        for (i in 0 until pagerAdapter.itemCount) {
            val fragment = pagerAdapter.createFragment(i)
            fragment.arguments = bundle
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (pagerAdapter is ProfilePagerAdapter) {
                tab.text = pagerAdapter.getTabTitle(position)
            } else {
                binding.tabLayout.visibility = View.GONE
            }
        }.attach()

        binding.rlEditProfileImg.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.addPostBtn.setOnClickListener {
            val intent = Intent(this, PostProjectActivity::class.java)
            startActivityForResult(intent, REQUEST_POST_PROJECT)
        }

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog?.show()

        if (userId != null && userId != auth.currentUser?.uid) {
            loadProfilePictureByUserId(userId!!)
        } else {
            loadProfilePictureByCurrentUser()
        }
    }
    private fun loadProfilePictureByUserId(userId: String) {
        val docRef = FirebaseFirestore.getInstance().collection("User").document(userId)

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                userName = document.getString("name")
                if (document.contains("profilePictureUrl")) {
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    setupUI(profilePictureUrl)
                } else {
                    setupUI(null)
                }
                sweetAlertDialog?.dismissWithAnimation()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this, "Failed to load profile picture: ${exception.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProfilePictureByCurrentUser() {
        val user = auth.currentUser ?: return
        val docRef = FirebaseFirestore.getInstance().collection("User").document(user.uid)

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                userName = document.getString("name")
                if (document.contains("profilePictureUrl")) {
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    setupUI(profilePictureUrl)
                } else {
                    setupUI(null)
                }
                sweetAlertDialog?.dismissWithAnimation()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this, "Failed to load profile picture: ${exception.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_POST_PROJECT) {
            if (resultCode == RESULT_OK) {
                if (userId != null && userId != auth.currentUser?.uid) {
                    loadProfilePictureByUserId(userId!!)
                } else {
                    loadProfilePictureByCurrentUser()
                }
                val viewPager: ViewPager2 = findViewById(R.id.pager)
                val pagerAdapter = ProfilePagerAdapter(this)
                viewPager.adapter = pagerAdapter

                val tabLayout: TabLayout = findViewById(R.id.tab_layout)
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = pagerAdapter.getTabTitle(position)
                }.attach()
            }
        }
    }

    private fun setupUI(profilePictureUrl: String?) {
        if (!isDestroyed) {
            if (!profilePictureUrl.isNullOrEmpty()) {
                Glide.with(this).load(profilePictureUrl).into(binding.profileImg)
            } else {
                Glide.with(this).load(R.drawable.default_profile_picture).into(binding.profileImg)
            }
            binding.username.text = userName ?: "Unknown User"
            binding.aboutInfoText.text = "See $userName's About Info"
            if (userId != null && userId != auth.currentUser?.uid) {
                binding.personalInfo.visibility = View.GONE
                binding.guestInfo.visibility = View.VISIBLE
            } else {
                binding.personalInfo.visibility = View.VISIBLE
                binding.guestInfo.visibility = View.GONE
            }
        }
    }

    private fun uploadImageToStorage(uri: Uri) {
        val user = auth.currentUser ?: return
        val reference = storage.reference.child("images/${user.uid}")

        reference.putFile(uri).addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfilePictureUrl(downloadUrl.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfilePictureUrl(url: String) {
        val user = auth.currentUser ?: return
        val userUpdate = hashMapOf("profilePictureUrl" to url)

        FirebaseFirestore.getInstance().collection("User").document(user.uid)
            .set(userUpdate, SetOptions.merge()).addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT)
                    .show()
                if (!isDestroyed) {
                    Glide.with(this).load(url).into(binding.profileImg)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to update profile picture: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
