package ie.app.freelanchaincode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var button:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.editButton)
        button.setOnClickListener {
            Intent(
                this@MainActivity,
                EditProfileActivity::class.java
            ).also { startActivity(it) }
        }
    }
}