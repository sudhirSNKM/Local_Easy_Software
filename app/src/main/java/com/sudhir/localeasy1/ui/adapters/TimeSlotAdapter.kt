package com.sudhir.localeasy1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sudhir.localeasy1.R
import java.util.*

class TimeSlotAdapter(
    private val onTimeSelected: (Long) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var slots: List<Long> = emptyList()
    private var selectedPosition: Int = -1

    fun updateSlots(newSlots: List<Long>) {
        slots = newSlots
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val time = slots[position]
        holder.bind(time, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onTimeSelected(time)
        }
    }

    override fun getItemCount(): Int = slots.size

    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val slotTextView: TextView = itemView.findViewById(R.id.slotTextView)

        fun bind(time: Long, isSelected: Boolean) {
            slotTextView.text = android.text.format.DateFormat.format("MMM dd, HH:mm", Date(time))
            itemView.isSelected = isSelected
        }
    }
}
