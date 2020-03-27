package cn.kherrisan.dragonfly.telegram

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.text.SimpleDateFormat
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AutoIncrement

@Document("sequence")
data class Sequence(
    val seq: Long,
    val name: String
)

enum class ProductLine {
    IPAD,
//    MAC,
//    WATCH
}

data class SubscriptionKey(
    val email: String,
    val line: ProductLine
)

@Document("subscription")
data class Subscription(
    @Id val sid: SubscriptionKey,
    var pid: Long = 0L,
    val subTime: Date = MyDate(),
    var unsubTime: Date? = null
) {
    constructor(email: String, line: ProductLine) : this(SubscriptionKey(email, line))
}

@Document("product")
data class Product(
    @Id @AutoIncrement val pid: Long,
    @Indexed val name: String,
    @Indexed val line: ProductLine,
    val code: String,
    val upTime: Date = MyDate(),
    var downTime: Date? = null
) {
    val link: String
        get() = "https://www.apple.com.cn/shop/product/$code"
}

data class MessageJob(
    val products: List<Product>,
    val receiver: String
)

@ConfigurationProperties(prefix = "dragonfly")
@Configuration
open class Config(
    var tg: Boolean? = null,
    var interval: Long? = null,
    var token: String? = System.getenv("token"),
    var name: String? = null,
    var creatorId: Int? = null,
    var chatId: Long? = null,
    var aliyunAccessKey: String? = null
)

class MyDate(ts: Long) : Date(ts) {

    companion object {
        val BEIJING_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        init {
            BEIJING_TIME_FORMAT.timeZone = TimeZone.getTimeZone("GMT+8:00")
        }
    }

    constructor() : this(System.currentTimeMillis())

    override fun toString(): String {
        return BEIJING_TIME_FORMAT.format(this)
    }
}