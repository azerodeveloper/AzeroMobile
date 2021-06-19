package com.soundai.azero.azeromobile.ui.activity.question.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.QuestionHelperTemplate

class QuestionHelpAdapter(questionHelperTemplate: QuestionHelperTemplate) :
    RecyclerView.Adapter<QuestionHelpAdapter.HelpViewHolder>() {
    private val dataList = ArrayList<Pair<Int, String>>()
    private val titleIndexList = ArrayList<Int>()

    init {
        questionHelperTemplate.pageDisplay.forEach { helpItem ->
            titleIndexList.add(dataList.size)
            dataList.add(Pair(-1, helpItem.title))
            for (i in helpItem.contents.indices) {
                dataList.add(Pair(i + 1, helpItem.contents[i]))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.grid_question_title -> TitleViewHolder(view)
            R.layout.grid_question_content -> ContentViewHolder(view)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (titleIndexList.contains(position)) {
            R.layout.grid_question_title
        } else {
            R.layout.grid_question_content
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data.first, data.second)
    }

    abstract class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(index: Int, content: String) {
            onBind(index, content)
        }

        protected abstract fun onBind(
            index: Int,
            content: String
        )
    }

    class TitleViewHolder(itemView: View) : HelpViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        override fun onBind(index: Int, content: String) {
            tvTitle.text = content
        }
    }

    class ContentViewHolder(itemView: View) : HelpViewHolder(itemView) {
        private val tvIndex: TextView = itemView.findViewById(R.id.tv_index)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        override fun onBind(index: Int, content: String) {
            tvIndex.text = index.toString()
            tvContent.text = content
        }
    }
}
