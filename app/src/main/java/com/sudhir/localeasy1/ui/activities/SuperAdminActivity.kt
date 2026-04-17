package com.sudhir.localeasy1.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.sudhir.localeasy1.databinding.ActivitySuperAdminBinding
import com.sudhir.localeasy1.ui.fragments.SuperAdminAnalyticsFragment
import com.sudhir.localeasy1.ui.fragments.SuperAdminDashboardFragment
import com.sudhir.localeasy1.ui.fragments.SuperAdminManageBusinessFragment
import com.sudhir.localeasy1.ui.fragments.SuperAdminProfileFragment

class SuperAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivitySuperAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        if (savedInstanceState == null) {
            loadDashboard()
        }
    }

    private fun setupNavigation() {
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                com.sudhir.localeasy1.R.id.nav_dashboard -> loadDashboard()
                com.sudhir.localeasy1.R.id.nav_manage -> loadManage()
                com.sudhir.localeasy1.R.id.nav_analytics -> loadAnalytics()
                com.sudhir.localeasy1.R.id.nav_profile -> loadProfile()
            }
            true
        }
        binding.bottomNav.selectedItemId = com.sudhir.localeasy1.R.id.nav_dashboard
    }

    private fun loadDashboard() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.sudhir.localeasy1.R.anim.slide_in_right,
                com.sudhir.localeasy1.R.anim.fade_out,
                com.sudhir.localeasy1.R.anim.fade_in,
                com.sudhir.localeasy1.R.anim.slide_out_right
            )
            .replace(binding.frameLayout.id, SuperAdminDashboardFragment())
            .commit()
    }

    private fun loadManage() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.sudhir.localeasy1.R.anim.slide_in_right,
                com.sudhir.localeasy1.R.anim.fade_out,
                com.sudhir.localeasy1.R.anim.fade_in,
                com.sudhir.localeasy1.R.anim.slide_out_right
            )
            .replace(binding.frameLayout.id, SuperAdminManageBusinessFragment())
            .commit()
    }

    private fun loadAnalytics() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.sudhir.localeasy1.R.anim.slide_in_right,
                com.sudhir.localeasy1.R.anim.fade_out,
                com.sudhir.localeasy1.R.anim.fade_in,
                com.sudhir.localeasy1.R.anim.slide_out_right
            )
            .replace(binding.frameLayout.id, SuperAdminAnalyticsFragment())
            .commit()
    }

    private fun loadProfile() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.sudhir.localeasy1.R.anim.slide_in_right,
                com.sudhir.localeasy1.R.anim.fade_out,
                com.sudhir.localeasy1.R.anim.fade_in,
                com.sudhir.localeasy1.R.anim.slide_out_right
            )
            .replace(binding.frameLayout.id, SuperAdminProfileFragment())
            .commit()
    }
}
