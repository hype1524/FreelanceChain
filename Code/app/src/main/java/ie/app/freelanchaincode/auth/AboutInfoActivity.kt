package ie.app.freelanchaincode.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.databinding.ActivityAboutInfoBinding
import ie.app.freelanchaincode.models.UserModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AboutInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutInfoBinding
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID")
        getUserData()
    }

    fun formatBirthday(dateString: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d 'tháng' M, yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun getUserData() {
        if (userId != null && userId != user?.uid) {
            binding.saveBioBtn.visibility = View.GONE
            binding.bio.isEnabled = false
            binding.chipText.visibility = View.GONE
            binding.chipGroup.isEnabled = false
            binding.addSkillBtn.visibility = View.GONE
            binding.editBasicInfo.visibility = View.GONE

            FirebaseFirestore
                .getInstance()
                .collection("User")
                .document(userId!!)
                .get()
                .addOnSuccessListener { document ->
                    val userData = document.toObject(UserModel::class.java)
                    userData?.let {
                        if (it.bio != null) {
                            binding.bio.setText(it.bio)
                        } else {
                            binding.bio.setText("No bio available")
                        }

                        if (it.skills != null) {
                            for (skill in it.skills!!) {
                                val chip = Chip(this)
                                chip.text = skill
                                chip.isCloseIconVisible = false
                                binding.chipGroup.addView(chip)
                            }
                        } else {
                            binding.bio.setText("No skills available")
                        }

                        if (it.gender != null && it.gender != "Chọn giới tính") {
                            binding.genderTxt.text = it.gender
                        } else {
                            binding.genderTxt.text = "Unknown"
                        }

                        if (it.birthday != null && it.birthday != "") {
                            binding.birthdayTxt.text = formatBirthday(it.birthday!!)
                        } else {
                            binding.birthdayTxt.text = "Unknown"
                        }
                    }
                }
        } else {
            binding.saveBioBtn.visibility = View.VISIBLE
            binding.bio.isEnabled = true
            binding.chipText.visibility = View.VISIBLE
            binding.addSkillBtn.visibility = View.VISIBLE
            binding.chipGroup.isEnabled = true
            binding.saveSkillBtn.visibility = View.VISIBLE
            binding.editBasicInfo.visibility = View.VISIBLE

            user?.let { currentUser ->
                val db = FirebaseFirestore.getInstance()
                val userDocRef = db.collection("User").document(currentUser.uid)

                userDocRef.get().addOnSuccessListener { document ->
                    val userData = document.toObject(UserModel::class.java)
                    userData?.let { user ->
                        if (user.bio != null) {
                            binding.bio.setText(user.bio)
                        } else {
                            binding.bio.setText("No bio available")
                        }

                        if (user.skills != null) {
                            for (skill in user.skills!!) {
                                val chip = Chip(this)
                                chip.text = skill
                                chip.isCloseIconVisible = true
                                chip.setOnCloseIconClickListener { binding.chipGroup.removeView(chip) }
                                binding.chipGroup.addView(chip)
                            }
                        } else {
                            binding.bio.setText("No skills available")
                        }

                        if (user.gender != null && user.gender != "Chọn giới tính") {
                            binding.genderTxt.text = user.gender
                        } else {
                            binding.genderTxt.text = "Unknown"
                        }

                        if (user.birthday != null && user.birthday != "") {
                            Log.d("AboutInfoActivity", "Birthday: ${user.birthday}")
                            binding.birthdayTxt.text = formatBirthday(user.birthday!!)
                        } else {
                            binding.birthdayTxt.text = "Unknown"
                        }

                        binding.editBasicInfo.setOnClickListener {
                            showEditBasicInfoPopup()
                        }

                        binding.saveBioBtn.setOnClickListener {
                            val bio = binding.bio.text.toString()
                            val updatedUser = user.copy(bio = bio)

                            userDocRef.set(updatedUser, SetOptions.merge()).addOnSuccessListener {
                                finish()
                                Toast
                                    .makeText(
                                        this,
                                        "Thông tin đã được cập nhật",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }

                        binding.saveSkillBtn.setOnClickListener {
                            val skillList = getSkillListFromChips()
                            val updatedUser = user.copy(skills = skillList)
                            userDocRef.set(updatedUser, SetOptions.merge()).addOnSuccessListener {
                                finish()
                                Toast
                                    .makeText(
                                        this,
                                        "Thông tin kỹ năng đã được cập nhật",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                        binding.addSkillBtn.setOnClickListener {
                            if (binding.chipText.text
                                    .toString()
                                    .isNotBlank()
                            ) {
                                addChipsFromText(binding.chipText.text.toString())
                            }
                            binding.chipText.setText("")
                        }
                    }
                }
            }
        }
    }

    private fun addChipsFromText(toString: String) {
        val chip = Chip(this)
        chip.text = binding.chipText.text.toString()
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener { binding.chipGroup.removeView(chip) }
        binding.chipGroup.addView(chip)
    }

    private fun getSkillListFromChips(): List<String> {
        val chipGroup = binding.chipGroup
        val skillList = mutableListOf<String>()

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            skillList.add(chip.text.toString())
        }
        return skillList
    }

    private fun showEditBasicInfoPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_basic_info, null)

        val alertDialog =
            AlertDialog
                .Builder(this)
                .setView(popupView)
                .setPositiveButton("Save") { dialog, which ->
                    val spinner = popupView.findViewById<Spinner>(R.id.spinner)
                    val edtBirthday = popupView.findViewById<EditText>(R.id.edtBirthday)

                    val selectedGender = spinner.selectedItem.toString()
                    val birthday = edtBirthday.text.toString()

                    if (userId == null || userId == user?.uid) {
                        userId = user?.uid
                        val db = FirebaseFirestore.getInstance()
                        val userDocRef = user?.let { db.collection("User").document(userId!!) }

                        val updates =
                            hashMapOf<String, Any>(
                                "gender" to selectedGender,
                                "birthday" to birthday,
                            )

                        if (userDocRef != null) {
                            userDocRef
                                .set(updates, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast
                                        .makeText(
                                            this,
                                            "Thông tin đã được cập nhật",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }.addOnFailureListener { e ->
                                    Toast
                                        .makeText(
                                            this,
                                            "Cập nhật thất bại: ${e.message}",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                        }
                    }

                    finish()
                }.setNegativeButton("Cancel", null)
                .create()

        alertDialog.show()

        val window = alertDialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.gravity = Gravity.CENTER
            window.attributes = layoutParams
        }

        val spinner = popupView.findViewById<Spinner>(R.id.spinner)
        val edtBirthday = popupView.findViewById<EditText>(R.id.edtBirthday)

        val options = listOf("Chọn giới tính", "Nam", "Nữ", "Khác")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter

        if (user?.uid != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("User").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                val userData = document.toObject(UserModel::class.java)
                userData?.let { user ->
                    val genderIndex = options.indexOf(user.gender)
                    if (genderIndex >= 0) {
                        spinner.setSelection(genderIndex)
                    }

                    user.birthday?.let {
                        edtBirthday.setText(it)
                    }
                }
            }
        }

        edtBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                        edtBirthday.setText(date)
                    },
                    year,
                    month,
                    day,
                )
            datePickerDialog.show()
        }
    }
}
