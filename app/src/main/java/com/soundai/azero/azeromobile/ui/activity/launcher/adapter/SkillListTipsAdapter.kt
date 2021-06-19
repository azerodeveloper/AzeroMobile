package com.soundai.azero.azeromobile.ui.activity.launcher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R

class SkillListTipsAdapter(val context: Context) :
    RecyclerView.Adapter<SkillListTipsAdapter.ViewHolder>() {
    var data: List<Pair<String,Boolean>> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
        get(){
            return field
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.item_skill_list_tip, parent, false)
        return ViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return if(data.isNullOrEmpty()) 0 else Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = position % (data.size)
        holder.tvContent?.text = data[p].first
        holder.tvContent?.isSelected = true
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var tvContent: TextView? = null

        init {
            tvContent = view.findViewById(R.id.tv_content)
        }
    }
}