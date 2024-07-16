package ie.app.freelanchaincode.adapter

import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import ie.app.freelanchaincode.BiddingListActivity
import ie.app.freelanchaincode.R
import ie.app.freelanchaincode.databinding.ItemBiddingBinding
import ie.app.freelanchaincode.models.BiddingModel
import ie.app.freelanchaincode.models.UserModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BiddingAdapter(
    private val context: BiddingListActivity,
    private val biddingList: ArrayList<BiddingModel>,
) : RecyclerView.Adapter<BiddingAdapter.BiddingViewHolder>() {
    val skillList = ArrayList<String>()
    private var projectId: String? = null

    fun setProjectId(id: String) {
        projectId = id
    }

    class BiddingViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemBiddingBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BiddingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bidding, parent, false)
        return BiddingViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BiddingViewHolder,
        position: Int,
    ) {
        val bid = biddingList[position]
        holder.binding.bidTitle.text = bid.bidTitle
        holder.binding.bidDesc.text = bid.bidDescription

        if (bid.bidStatus != null) {
            holder.binding.btnAccept.isEnabled = false
            if (bid.bidStatus == false) {
                holder.binding.btnAccept.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                    text = "Declined"
                }
            } else {
                holder.binding.btnAccept.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                    setTextColor(ContextCompat.getColor(context, R.color.main_green))
                    text = "Accepted"
                }
            }
        }
        val bidAmountFormatted =
            NumberFormat.getNumberInstance(Locale.US).format(bid.bidAmount?.toLong())
        holder.binding.bidAmount.text = bidAmountFormatted

        val timestamp = bid.bidTime?.toDate()
        val now = Date()
        val diff =
            if (timestamp != null) {
                now.time - timestamp.time
            } else {
                0 // Default value
            }
        val bidTimeText =
            when {
                diff < DateUtils.HOUR_IN_MILLIS -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "$minutes mins ago"
                }

                diff < DateUtils.DAY_IN_MILLIS -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "$hours hours ago"
                }

                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(timestamp)
                }
            }

        holder.binding.bidTime.text = bidTimeText

        holder.binding.bidDesc.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    holder.binding.bidDesc.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                    if (holder.binding.bidDesc.lineCount > 3) {
                        holder.binding.readMore.visibility = View.VISIBLE
                        holder.binding.bidDesc.maxLines = 3
                        holder.binding.bidDesc.ellipsize = TextUtils.TruncateAt.END
                    } else {
                        holder.binding.readMore.visibility = View.GONE
                    }
                }
            },
        )

        holder.binding.readMore.setOnClickListener {
            if (holder.binding.readMore.text == "Xem thêm") {
                holder.binding.bidDesc.maxLines = Int.MAX_VALUE
                holder.binding.bidDesc.ellipsize = null
                holder.binding.readMore.text = "Thu gọn"
            } else {
                holder.binding.bidDesc.maxLines = 3
                holder.binding.bidDesc.ellipsize = TextUtils.TruncateAt.END
                holder.binding.readMore.text = "Xem thêm"
            }
        }

        holder.binding.btnAccept.setOnClickListener {
            val bidId = bid.id
            if (projectId != null && bidId != null) {
                AlertDialog
                    .Builder(holder.itemView.context)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn chấp nhận chào giá này không? Tất cả các chào giá khác cho dự án này sẽ bị hủy.")
                    .setPositiveButton("Accept") { dialog, which ->
                        acceptBid(projectId!!, bidId)
                        context.finish()
                    }.setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast
                    .makeText(holder.itemView.context, "Failed to accept bid", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        FirebaseFirestore
            .getInstance()
            .collection("User")
            .document(bid.userId!!)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserModel::class.java)
                if (user != null) {
                    if (user.skills != null) {
                        skillList.clear()
                        skillList.addAll(user.skills!!)
                        holder.binding.bidChipGroup.removeAllViews()
                        for (skill in skillList) {
                            val chip = Chip(holder.itemView.context)
                            chip.text = skill
                            holder.binding.bidChipGroup.addView(chip)
                        }
                    }
                    holder.binding.userName.text = user.name
                    val profilePictureUrl = user.profilePictureUrl
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide
                            .with(holder.itemView.context)
                            .load(profilePictureUrl)
                            .into(holder.binding.userImage)
                    } else {
                        holder.binding.userImage.setImageResource(R.drawable.default_profile_picture)
                    }
                }
            }.addOnFailureListener { exception ->
            }
    }

    private fun acceptBid(
        projectId: String,
        bidId: String,
    ) {
        val db = FirebaseFirestore.getInstance()

        // Cập nhật field isBidded của project thành true
        db
            .collection("Project")
            .document(projectId)
            .update("isBidded", true)
            .addOnSuccessListener {
                // Cập nhật trạng thái bidStatus của tất cả các bidding có cùng projectId
                db
                    .collection("Bidding")
                    .whereEqualTo("projectId", projectId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val status = document.id == bidId
                            db
                                .collection("Bidding")
                                .document(document.id)
                                .update("bidStatus", status)
                        }
                        Toast
                            .makeText(
                                context,
                                "Bid accepted and others cancelled successfully",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }.addOnFailureListener { exception ->
                        Toast
                            .makeText(
                                context,
                                "Failed to update bids: ${exception.message}",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
            }.addOnFailureListener { exception ->
                Toast
                    .makeText(
                        context,
                        "Failed to update project: ${exception.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
    }

    override fun getItemCount(): Int = biddingList.size
}
