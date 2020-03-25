package cn.kherrisan.dragonfly.telegram

import okhttp3.*
import okhttp3.Response
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun md5(text: String, prefix: Int = 6): String {
    return fullMD5(text).substring(0, prefix)
}

val MD5_DG = MessageDigest.getInstance("MD5")

fun fullMD5(text: String): String {
    return Hex.encodeHexString(MD5_DG.digest(text.toByteArray()))
}

fun writeFile(name: String, text: String) {
    FileUtils.write(File("${name}.html"), text, StandardCharsets.UTF_8)
}

const val USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15"

class DefaultInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Cache-Control", "no-cache")
            .build()
        return chain.proceed(req)
    }
}

class DefaultCookieJar : CookieJar {
    private val store = mutableMapOf<String, List<Cookie>>()
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store[url.host] ?: listOf()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store[url.host] = cookies
    }
}