package com.codinlog.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.codinlog.app.databinding.MainActivityBinding
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.InetAddress
import java.net.ServerSocket

class MainActivity : AppCompatActivity() {
    private val PERMISSION_CODE_INTERNET: Int = 1
    private var mPort: Int = 5353
    private var mServiceName = "Test"
    private var mServiceType = "_http._tcp"
    private val that: Context by lazy {
        this@MainActivity
    }
    private lateinit var binding: MainActivityBinding
    private lateinit var mNdsService: NsdServiceInfo
    private lateinit var mNdsManager: NsdManager
    private val mAdapter = DeviceAdapter()
    private val mHandler = Handler(Looper.getMainLooper(), Handler.Callback {
        binding.msg.text = when (it.what) {
            100 -> "mDNS Discovery Started"
            101 -> "mDNS Discovery Stopped"
            else -> "Unknown"
        }
        return@Callback true
    })
    private val mModel: DeviceViewModel by viewModels()
    private val mRegistrationListener = object : NsdManager.RegistrationListener {
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
    private val mDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Toast.makeText(that, "onStartDiscoveryFailed $serviceType", Toast.LENGTH_SHORT).show()
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Toast.makeText(that, "onStopDiscoveryFailed $serviceType", Toast.LENGTH_SHORT).show()
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            mHandler.sendEmptyMessage(100)
            Toast.makeText(that, "onDiscoveryStarted $serviceType", Toast.LENGTH_SHORT).show()
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            mHandler.sendEmptyMessage(101)
            Toast.makeText(that, "onDiscoveryStopped $serviceType", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            if (serviceInfo == null) {
                return
            }
            mNdsManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    Toast.makeText(that, "onResolveFailed", Toast.LENGTH_SHORT).show()
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                    if (serviceInfo == null) {
                        return
                    }
                    lifecycleScope.launch {
                        with(serviceInfo) {
                            if (serviceName.isNullOrEmpty()) {
                                serviceName = "unknown"
                            }
                            if (serviceType.isNullOrEmpty()) {
                                serviceType = "unknown"
                            }
                            val device = Device(
                                serviceName,
                                serviceType,
                                if (host == null) "unknown" else host.toString()
                            )
                            mModel.devices.value?.let {
                                if (!it.contains(device)) {
                                    it.add(device)
                                    mModel.devices.value = it
                                }
                            }
                        }
                        Toast.makeText(that, "onServiceFound $serviceInfo", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Toast.makeText(that, "onServiceLost $serviceInfo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        binding.rv.adapter = mAdapter
        mModel.devices.observe(this) {
            if(it.isNotEmpty()){
                mAdapter.submitList(it)
            }
        }
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
        mNdsManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(mNdsService, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
        }
        mHandler.postDelayed({
            mNdsManager.discoverServices(
                mServiceType,
                NsdManager.PROTOCOL_DNS_SD,
                mDiscoveryListener
            )
        }, 5 * 1000)

    }

    override fun onDestroy() {
        super.onDestroy()
        mNdsManager.unregisterService(mRegistrationListener)
    }
}