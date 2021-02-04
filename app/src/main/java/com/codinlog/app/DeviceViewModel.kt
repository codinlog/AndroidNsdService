package com.codinlog.app

import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeviceViewModel:ViewModel() {
    var devices = MutableLiveData<MutableList<Device>>().apply {
        value = mutableListOf()
    }
}