package io.github.yeobara.android.app

import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

abstract class RxPresenter<M : Any, V : RxView<M>>() {

    protected var views = arrayListOf<V>()
    protected var subscription: Subscription? = null
    protected var request: Observable<M>? = null
    protected var completeWithRefresh: Boolean = false

    @Synchronized
    public fun isLoading(): Boolean = request != null

    @Synchronized
    public fun attach(view: V) {
        if (!views.contains(view)) {
            views.add(view)
        }
        setSubscription()
    }

    @Synchronized
    public fun detach(view: V) {
        views.remove(view)
        subscription?.unsubscribe()
        subscription = null
    }

    @Synchronized
    public fun start(o: Observable<M>, force: Boolean = false) {
        onLoading(true)
        if (request == null || force) {
            subscription?.unsubscribe()
            subscription = null
            request = o
        }
        setSubscription()
    }

    open fun setSubscription() {
        request?.run {
            subscription = subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        onNext(result)
                    }, { error ->
                        onError(error)
                    }, {
                        onComplete()
                    })
        }
    }

    @Synchronized
    fun onLoading(show: Boolean) {
        views.forEach { it.showLoading(show) }
    }

    @Synchronized
    fun onNext(result: M) {
        views.forEach { it.showResult(result) }
    }

    @Synchronized
    fun onError(error: Throwable) {
        views.forEach { it.showError(error) }
        request = null
        onLoading(false)
    }

    @Synchronized
    fun onComplete() {
        if (completeWithRefresh) {
            views.forEach { it.showComplete() }
        }

        request = null
        onLoading(false)
    }

    @Synchronized
    fun cancel() {
        subscription?.unsubscribe()
        subscription = null
        onComplete()
    }
}