package ie.app.freelanchaincode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import ie.app.freelanchaincode.auth.EditProfileActivity
import ie.app.freelanchaincode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var button:Button
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editButton.setOnClickListener {
            Intent(
                this@MainActivity,
                EditProfileActivity::class.java
            ).also { startActivity(it) }
        }
        binding.postProjectButton.setOnClickListener {
            Intent(
                this@MainActivity,
                PostProjectActivity::class.java
            ).also { startActivity(it) }
        }
    }
}