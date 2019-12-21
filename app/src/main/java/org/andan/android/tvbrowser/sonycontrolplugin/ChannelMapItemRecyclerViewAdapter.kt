package org.andan.android.tvbrowser.sonycontrolplugin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.MapChannnelItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.ProgramItemBinding
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyProgram

class ChannelMapItemRecyclerViewAdapter(val clickListener: ChannelMapListener, val controlViewModel: ControlViewModel) :
    RecyclerView.Adapter<ChannelMapItemRecyclerViewAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channelName = controlViewModel.getFilteredChannelNameList().value?.get(position)!!
        holder.bind(channelName, clickListener,controlViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return controlViewModel.getFilteredChannelNameList().value!!.size
    }

    class ViewHolder private constructor(val binding: MapChannnelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, clickListener: ChannelMapListener, controlViewModel: ControlViewModel) {
            binding.channelName = item
            binding.channelPosition = adapterPosition+1
            //binding.channelPosition = 1
            //binding.channelPosition = layoutPosition+1
            //binding.channelPosition = position
            /*
            SonyIPControl.ChannelMapEntryValue channelMapEntryValue = mControl.getChannelMap().get(channelName);
            if (channelMapEntryValue != null) {
                if (channelMapEntryValue.getProgramId() >= 0 && channelMapEntryValue.getProgramId() < mProgramTitleList.size()) {
            */

            val programUri: String? = controlViewModel.getSelectedControl()!!.channelProgramUriMap[item]
            if (!programUri.isNullOrEmpty()) {
                val program: SonyProgram? = controlViewModel.getSelectedControl()!!.programUriMap[programUri]
                binding.programTitle = program?.title
                binding.programSourceWithType = program?.sourceWithType
            } else
            {
                binding.programTitle="--unmapped--"
                binding.programSourceWithType = ""
            }

            binding.clickListener = clickListener
            binding.controlViewModel = controlViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item)}
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MapChannnelItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ChannelMapListener(val clickListener: (view: View, channelName: String) -> Unit,
                      val longClickListener: (channelName: String) -> Boolean) {
    fun onClick(view: View, channelName: String) = clickListener(view, channelName)
    fun onLongClick(channelName: String) = longClickListener(channelName)
}