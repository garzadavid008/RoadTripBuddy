package com.example.roadtripbuddy


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout




class MainActivity : BaseMapUtils() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navButton = findViewById<ImageButton>(R.id.nav_button)
        val navView: com.google.android.material.navigation.NavigationView = findViewById(R.id.nav_view)


        navButton.setOnClickListener {
            drawerLayout.openDrawer(findViewById(R.id.nav_view))
        }

        navView.setNavigationItemSelectedListener{ menuItem ->
            when(menuItem.itemId) {
                R.id.nav_plan_button -> {

                    true
                }
                R.id.nav_settings_button -> {

                    true
                }
                R.id.nav_about_button -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.map_container, aboutFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }


        initMap()

        initRouting()

        //initNavigationTileStore()

        //initNavigation()
    }



}
