package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_settings)  // ваш layout с fragmentContainer + BottomNavigationView

        // при первом старте – показываем DashboardFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
        }

        findViewById<BottomNavigationView>(R.id.bottomNavigation)
            .setOnItemSelectedListener { item ->
                val frag = when (item.itemId) {
                    R.id.nav_wallet   -> DashboardFragment()
                    R.id.nav_settings -> SettingsFragment()
                    else              -> null
                }
                frag?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, it as Fragment)
                        .commit()
                    true
                } ?: false
            }
    }
}
