package io.github.yeobara.android.app

interface RxView<M> {

    fun showLoading(show: Boolean)

    fun showResult(result: M)

    fun showError(error: Throwable)

    fun showComplete()
}