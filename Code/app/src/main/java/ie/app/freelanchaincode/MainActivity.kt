package ie.app.freelanchaincode

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
//import androidx.navigation.NavController
//import androidx.navigation.findNavController
import ie.app.freelanchaincode.databinding.ActivityMainBinding
import ie.app.freelanchaincode.main.RoomChatFragment
import ie.app.freelanchaincode.main.HomeFragment
import ie.app.freelanchaincode.main.NotiFragment
import ie.app.freelanchaincode.main.ProjectFragment
import ie.app.freelanchaincode.main.SettingFragment


class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())
        binding.bottomNavigationView.background = null

//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
//        navController = navHostFragment.navController
//
//        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.chat -> replaceFragment(RoomChatFragment())
                R.id.notification -> replaceFragment(NotiFragment())
                R.id.setting -> replaceFragment(SettingFragment())
            }
            true
        }

        binding.project.setOnClickListener {
            replaceFragment(ProjectFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}