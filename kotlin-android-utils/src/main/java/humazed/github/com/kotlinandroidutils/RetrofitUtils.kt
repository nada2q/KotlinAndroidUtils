package humazed.github.com.kotlinandroidutils

import android.view.View
import android.widget.EditText
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

fun <T> Call<T>.call(progressBar: View?, onResult: (responseBody: T, response: Response<T>) -> Unit) {
    val context = progressBar?.context
    if (context?.isConnected() == true || progressBar == null) {
        progressBar?.show()
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                progressBar?.hide()
                response.body()?.let { onResult(it, response) } ?: e { "Response Null" }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                progressBar?.hide()
                er { t }
                context?.toast(context.getString(R.string.error_happened) ?: "حدث خطأ")
            }
        })
    } else {
        context?.toast(
                context.getString(R.string.no_internet_connection) ?: "لا يوجد اتصال بالانترت")
    }
}

fun <T> Call<T>.call(progressBar: View?, onResult: (responseBody: T) -> Unit) {
    val context = progressBar?.context
    call(progressBar) { responseBody, response ->
        if (response.isSuccessful) {
            response.body()?.let { onResult(responseBody) } ?: e { "Response Null" }
        } else {
            e { "${response.errorBody()}" }
            context?.toast(context.getString(R.string.error_happened) ?: "حدث خطأ")
        }

    }
}


fun <T> Call<T>.onSuccess(onResult: (responseBody: T, response: Response<T>) -> Unit) =
        call(null) { responseBody, response -> onResult(responseBody, response) }

fun <T> Call<T>.onSuccess(onResult: (responseBody: T) -> Unit) =
        call(null) { responseBody -> onResult(responseBody) }


// Multipart helpers
fun EditText.textPart() = MultipartBody.create(MultipartBody.FORM, text.toString())

fun String.part() = MultipartBody.create(MultipartBody.FORM, this)
fun Int.part() = MultipartBody.create(MultipartBody.FORM, toString())
fun Double.part() = MultipartBody.create(MultipartBody.FORM, toString())

fun File.part(requestName: String, mimeType: String = "image/*"): MultipartBody.Part {
    // okHttp doesn't accept non ascii chars and crashes the app
    val asciiName = name.replace(Regex("[^A-Za-z0-9 ]"), "")
    val requestFile = RequestBody.create(MediaType.parse(mimeType), this)
    return MultipartBody.Part.createFormData(requestName, asciiName, requestFile)
}