package com.codinlog.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.codinlog.app.databinding.MainActivityBinding
import androidx.databinding.DataBindingUtil.setContentView
import java.net.ServerSocket

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var mNdsService: NsdServiceInfo
    private lateinit var mNdsManager: NsdManager
    private lateinit var registerLister: NsdManager.RegistrationListener
    private val PERMISSION_CODE_INTERNET: Int = 1
    private var mPort: Int = 5353
    private var mServiceName = "Test"
    private var mServiceType = "_http._tcp"
    private val that: Context by lazy {
        this@MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        checkPermission()
    }

    private fun checkPermission(): Unit {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    registermDNS()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        when {
            ContextCompat.checkSelfPermission(
                that,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED -> {
                registermDNS()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET) -> {
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.INTERNET)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_INTERNET -> {

            }
        }
    }

    private fun registermDNS(): Unit {
        mPort = ServerSocket(0).localPort
        mNdsService = NsdServiceInfo().apply {
            serviceName = mServiceName
            serviceType = mServiceType
            port = mPort
        }
        registerLister = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Toast.makeText(that, "onRegistrationFailed", Toast.LENGTH_SHORT).show()
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Toast.makeText(that, "onUnregistrationFailed", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                Toast.makeText(that, "onServiceRegistered", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                Toast.makeText(that, "onServiceUnregistered", Toast.LENGTH_SHORT).show()
            }

        }
        mNdsManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(mNdsService, NsdManager.PROTOCOL_DNS_SD, registerLister)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mNdsManager.unregisterService(registerLister)
    }
}