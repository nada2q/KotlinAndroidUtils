package humazed.github.com.kotlinandroidutils

import android.view.View
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.call(progressBar: View?, onResult: (response: T) -> Unit) {
    val context = progressBar?.context
    if (context?.isConnected() == true || progressBar == null) {
        progressBar?.show()
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                progressBar?.hide()
                if (response.isSuccessful)
                    response.body()?.let { onResult(it) } ?: e { "Response Null" }
                else e { "${response.errorBody()}" }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                progressBar?.hide()
                er { t }
            }
        })
    } else {
        context?.toast(
                context.getString(R.string.no_internet_connection) ?: "لا يوجد اتصال بالانترت")
    }

}

fun <T> Call<T>.onSuccess(onResult: (response: T) -> Unit) = call(null) { onResult(it) }