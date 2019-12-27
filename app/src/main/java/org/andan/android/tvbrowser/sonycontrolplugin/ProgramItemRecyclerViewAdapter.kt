package org.andan.android.tvbrowser.sonycontrolplugin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.ProgramItemBinding
import org.andan.av.sony.model.SonyProgram

class ProgramItemRecyclerViewAdapter(val clickListener: ProgramListener, val controlViewModel: ControlViewModel) :
    RecyclerView.Adapter<ProgramItemRecyclerViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return controlViewModel.getFilteredProgramList().value!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program = controlViewModel.getFilteredProgramList().value!![position]
        holder.bind(program, clickListener,controlViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ProgramItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SonyProgram, clickListener: ProgramListener, controlViewModel: ControlViewModel) {
            binding.program = item
            binding.clickListener = clickListener
            binding.controlViewModel = controlViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item)}
            binding.controlViewModel
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProgramItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

class ProgramListener(val clickListener: (program: SonyProgram) -> Unit,
                      val longClickListener: (program: SonyProgram) -> Boolean) {
    fun onClick(program: SonyProgram) = clickListener(program)
}
