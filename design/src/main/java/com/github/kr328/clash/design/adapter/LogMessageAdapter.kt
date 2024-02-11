package com.github.kr328.clash.design.adapter

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.AdapterLogMessageBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.resolveThemedColor

class LogMessageAdapter(
    private val context: Context,
    private val copy: (LogMessage) -> Unit,
) :
    RecyclerView.Adapter<LogMessageAdapter.Holder>() {
    class Holder(val binding: AdapterLogMessageBinding) : RecyclerView.ViewHolder(binding.root)

    var messages: List<LogMessage> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            AdapterLogMessageBinding
                .inflate(context.layoutInflater, parent, false)
        )
    }

    private val colorPrimary = context.resolveThemedColor(R.attr.colorPrimary)

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = messages[position]

        holder.binding.message = current
        when (current.level) {
            LogMessage.Level.Debug ->
                holder.binding.logLevel.setTextColor(Color.DKGRAY)
            LogMessage.Level.Silent ->
                holder.binding.logLevel.setTextColor(Color.LTGRAY)
            LogMessage.Level.Warning ->
                holder.binding.logLevel.setTextColor(Color.rgb(232, 150, 15 ))
            LogMessage.Level.Error ->
                holder.binding.logLevel.setTextColor(Color.RED)
            else -> {
                holder.binding.logLevel.setTextColor(Color.rgb(
                    Color.red(colorPrimary),
                    Color.green(colorPrimary),
                    Color.blue(colorPrimary)
                ))
            }
        }

        holder.binding.root.setOnLongClickListener {
            copy(current)

            true
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}