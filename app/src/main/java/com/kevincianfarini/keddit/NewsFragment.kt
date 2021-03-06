package com.kevincianfarini.keddit

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kevincianfarini.keddit.commons.RedditNews
import com.kevincianfarini.keddit.commons.RedditNewsItem
import com.kevincianfarini.keddit.commons.adapters.InfiniteScrollListener
import com.kevincianfarini.keddit.commons.adapters.NewsAdapter
import com.kevincianfarini.keddit.commons.inflate
import com.kevincianfarini.keddit.commons.news.NewsManager
import kotlinx.android.synthetic.main.news_fragment.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class NewsFragment : RxBaseFragment() {

    private val newsList: RecyclerView by lazy {
        news_list
    }

    private val refresh: SwipeRefreshLayout by lazy {
        swipe_refresh
    }

    companion object {
        private val KEY_REDDIT_NEWS = "redditNews"
    }

    private var redditNews: RedditNews? = null

    @Inject lateinit var newsManager: NewsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            container?.inflate(R.layout.news_fragment)

    private fun requestNews() {
        val subscription = newsManager.getNews(redditNews?.after ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { retrievedNews ->
                            redditNews = retrievedNews
                            (newsList.adapter as NewsAdapter).addNews(retrievedNews.news)
                            refresh.isRefreshing = false
                        },
                        { e ->
                            Log.e("refresh", "exception", e)
                            Snackbar.make(newsList, e.message ?: "", Snackbar.LENGTH_LONG).show()
                            refresh.isRefreshing = false
                        }
                )
        subscriptions.add(subscription)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        newsList.apply {
            setHasFixedSize(true)
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            clearOnScrollListeners()
            addOnScrollListener(InfiniteScrollListener(this@NewsFragment::requestNews, lm))
        }

        initAdapter()

        refresh.setOnRefreshListener {
            (newsList.adapter as NewsAdapter).clear()
            redditNews = null
            requestNews()
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_REDDIT_NEWS)) {
            redditNews = savedInstanceState.get(KEY_REDDIT_NEWS) as RedditNews
            (newsList.adapter as NewsAdapter).clearAndAddNews(redditNews?.news ?: emptyList<RedditNewsItem>())
        } else {
            requestNews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KedditApp.newsComponent.inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val news = (newsList.adapter as NewsAdapter).getNews()
        if (redditNews != null && news.isNotEmpty()) {
            outState.putParcelable(KEY_REDDIT_NEWS, redditNews?.copy(news = news))
        }
    }

    private fun initAdapter() {
        if (newsList.adapter == null) {
            newsList.adapter = NewsAdapter()
            refresh.isRefreshing = true
        }
    }
}