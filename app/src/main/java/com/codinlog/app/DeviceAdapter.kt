package com.codinlog.app

import android.net.nsd.NsdServiceInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinlog.app.databinding.DeviceItemBinding
import java.net.InetAddress

class DeviceAdapter : ListAdapter<Device, DeviceAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: DeviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Device) {
            item.apply {
                binding.device = item
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DeviceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position));
    }

    private class DiffCallback : DiffUtil.ItemCallback<Device>() {

        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.host == newItem.host
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.host == newItem.host
                    && oldItem.serviceName == newItem.serviceName
                    && oldItem.serviceType == newItem.serviceType
        }
    }
}