package com.kevincianfarini.keddit.commons.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.kevincianfarini.keddit.R
import com.kevincianfarini.keddit.commons.RedditNewsItem
import com.kevincianfarini.keddit.commons.inflate
import com.kevincianfarini.keddit.commons.interfaces.ViewType
import com.kevincianfarini.keddit.commons.interfaces.ViewTypeDelegateAdapter
import kotlinx.android.synthetic.main.text_item.view.*


class NewsTextDelegateAdapter : ViewTypeDelegateAdapter {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = TurnsViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as TurnsViewHolder).bind(item as RedditNewsItem)
    }

    class TurnsViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.text_item)) {
        fun bind(item: RedditNewsItem) = with(itemView) {
            description.text = item.title
            author.text = item.author
            comments.text = "${item.numComments} comments"
            time.text = "${item.created} ms"
        }
    }
}
