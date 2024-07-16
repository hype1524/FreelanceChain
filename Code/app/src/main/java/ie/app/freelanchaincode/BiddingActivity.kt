package ie.app.freelanchaincode

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.databinding.ActivityBiddingBinding
import ie.app.freelanchaincode.models.BiddingModel
import com.google.firebase.Timestamp
import ie.app.freelanchaincode.auth.ProfileActivity
import ie.app.freelanchaincode.models.UserModel
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BiddingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiddingBinding
    private var existingBidId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiddingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.edtBidAmount.addTextChangedListener(
            object : TextWatcher {
                private var current = ""

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != current) {
                        binding.edtBidAmount.removeTextChangedListener(this)
                        val cleanString = s.toString().replace("""[,.]""".toRegex(), "")
                        val parsed = cleanString.toDoubleOrNull()
                        val formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed ?: 0)
                        current = formatted
                        binding.edtBidAmount.setText(formatted)
                        binding.edtBidAmount.setSelection(formatted.length)
                        binding.edtBidAmount.addTextChangedListener(this)
                    }
                }
            },
        )

        binding.btnBid.setOnClickListener {
            val bidAmount = binding.edtBidAmount.text.toString().replace(",", "")
            val bidDescription = binding.description.text.toString()
            val bidTitle = binding.edtBidTitle.text.toString()
            if (bidAmount.isEmpty() || bidDescription.isEmpty() || bidTitle.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else if (bidDescription.length < 20) {
                Toast.makeText(this, "Mô tả quá ngắn, đề xuất của bạn phải tối thiểu 20 ký tự", Toast.LENGTH_SHORT).show()
            } else {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    FirebaseFirestore.getInstance().collection("User")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            val user = document.toObject(UserModel::class.java)
                            if (user?.skills.isNullOrEmpty()) {
                                AlertDialog.Builder(this)
                                    .setTitle("Thông báo")
                                    .setMessage("Bạn chưa thêm kỹ năng nào. Kỹ năng có thể giúp customer đánh giá cao về bạn. \nBạn có muốn tiếp tục bid?")
                                    .setPositiveButton("Continue") { dialog, which ->
                                        placeBid(bidAmount, bidTitle, bidDescription)
                                    }
                                    .setNegativeButton("Edit Profile") { dialog, which ->
                                        val intent = Intent(this, ProfileActivity::class.java)
                                        startActivity(intent)
                                    }.show()
                            } else {
                                placeBid(bidAmount, bidTitle, bidDescription)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to get user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        }

        getData()

    }

    private fun placeBid(bidAmount: String, bidTitle: String, bidDescription: String) {
        val biddingModel = BiddingModel().apply {
            this.bidTitle = bidTitle
            this.projectId = intent.getStringExtra("PROJECT_ID")
            this.userId = FirebaseAuth.getInstance().currentUser?.uid
            this.ownerId = intent.getStringExtra("USER_ID")
            this.bidAmount = bidAmount
            this.bidDescription = bidDescription
            this.bidTime = Timestamp.now()
            this.bidStatus = null
        }

        val db = FirebaseFirestore.getInstance()
        if (existingBidId != null) {
            db.collection("Bidding").document(existingBidId!!).set(biddingModel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bid Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update bid", Toast.LENGTH_SHORT).show()
                }
        } else {
            biddingModel.id = UUID.randomUUID().toString() + "-" + Date().time
            db.collection("Bidding").document(biddingModel.id!!).set(biddingModel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bid Placed Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to place bid", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val projectId = intent.getStringExtra("PROJECT_ID")

        if (userId != null && projectId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Bidding")
                .whereEqualTo("userId", userId)
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val bidding = documents.documents[0].toObject(BiddingModel::class.java)
                        existingBidId = documents.documents[0].id // Lưu ID của document
                        bidding?.let {
                            binding.edtBidTitle.setText(it.bidTitle)
                            binding.edtBidAmount.setText(it.bidAmount)
                            binding.description.setText(it.bidDescription)
                        }
                    }
                }
        }
    }
}
