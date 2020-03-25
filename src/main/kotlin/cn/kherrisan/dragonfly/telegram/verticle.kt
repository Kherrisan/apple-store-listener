package cn.kherrisan.dragonfly.telegram

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.ext.web.client.webClientOptionsOf
import okhttp3.*
import okhttp3.Response
import org.jsoup.Jsoup
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

const val EVNETBUS_DOWN = "1"
const val EVENTBUS_EMAIL = "2"
const val EVENTBUS_TELEGRAM = "3"
const val TELEGRAM_ADDRESS = "TELEGRAM_ADDRESS"

val CODE_PATTERN = Pattern.compile("/shop/product/(.+)$")
var DEBUG = false

fun main() {
    println(getCode("翻新 Apple Watch Series 4 (GPS + 蜂窝网络)，44 毫米银色铝金属表壳搭配白色运动型表带，链接为：https://www.apple.com.cn/cn-k12/shop/product/FTVR2CH/A"))
}

fun getCode(full: String): String {
    val m = CODE_PATTERN.matcher(full)
    m.find()
    return m.group(1)
}

class EmailVerticle : AbstractVerticle() {

    val mailer = MailerBuilder
        .withSMTPServer("smtp-mail.outlook.com", 587, "zdkscope@outlook.com", "zou970514")
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .withSessionTimeout(10 * 1000)
        .buildMailer()

    override fun start() {
        vertx.eventBus().consumer<String>(EVENTBUS_EMAIL) { it ->
            val msg = Gson().fromJson<MessageJob>(it.body(), MessageJob::class.java)
            val content = msg.products.map { "${it.name}，链接为 ${it.link}" }.joinToString("\n")
            logger.trace("正在发送邮件给 ${msg.receiver}：$content")
            try {
                mailer.sendMail(buildMail(msg.receiver, content))
            } catch (e: Exception) {
                logger.error(e)
                e.printStackTrace()
            }
        }
    }

    private fun buildMail(addr: String, content: String): Email {
        return EmailBuilder.startingBlank()
            .from("zdkscope@outlook.com")
            .to("Subscriber", addr)
            .withSubject("Apple Store翻新区上新了。")
            .withPlainText(content)
            .buildEmail()
    }
}

class DispatcherVerticle : AbstractVerticle() {

    private val mongoTemplate: MongoTemplate = SpringContainer[MongoTemplate::class.java]
    private val config = SpringContainer[Config::class]

    override fun start() {
        for (line in ProductLine.values()) {
            vertx.eventBus().consumer<String>(line.name) { it ->
                val type = object : TypeToken<List<Product>>() {}.type
                val list = Gson().fromJson<List<Product>>(it.body(), type)
                distinctAndSave(line, list.toMutableList())
            }
            vertx.setPeriodic(10000) {
                dispatchNewEmail(line)
                if (config.tg!!) {
                    dispatchNewTelegram(line)
                }
            }
        }
    }

    private fun distinctAndSave(line: ProductLine, upProducts: MutableList<Product>) {
        val upIpadPros = upProducts.filter { it.name.contains("iPad Pro") }.toMutableList()
        val q = Query()
            .addCriteria(
                Criteria.where("line").`is`(line.name)
                    .and("downTime").`is`(null)
            )
        val upProductCodes = upIpadPros.map { it.code }
        val dbUpProducts = mongoTemplate.find(q, Product::class.java)
        dbUpProducts.filter { it.code !in upProductCodes }.forEach {
            //数据库中的产品不在网页上，下架了
            logger.debug("产品下架 $it")
            it.downTime = MyDate()
            mongoTemplate.save(it)
        }
        val dbUpProductCodes = dbUpProducts.map { it.code }
        //在数据库中已经存在的，不是新上架的商品
        upIpadPros.removeIf { it.code in dbUpProductCodes }
        upIpadPros.forEach { p ->
            logger.debug("发现新的产品上架: $p")
            mongoTemplate.save(p)
        }
    }

    private fun dispatchNewTelegram(line: ProductLine) {
        var q = Query()
            .addCriteria(
                Criteria.where("line").`is`(line.name)
                    .and("downTime").`is`(null)
            )
        val upProducts = mongoTemplate.find(q, Product::class.java)
        if (upProducts.isEmpty()) {
            return
        }
        q = Query().addCriteria(
            Criteria.where("_id.line").`is`(line.name)
                .and("_id.email").`is`(TELEGRAM_ADDRESS)
        )
        var tg = mongoTemplate.findOne(q, Subscription::class.java)
        if (tg == null) {
            tg = Subscription(TELEGRAM_ADDRESS, line)
            mongoTemplate.save(tg)
        }
        val newProducts = upProducts.filter { tg.pid < it.pid }
        if (newProducts.isEmpty()) {
            return
        }
        vertx.eventBus().send(EVENTBUS_TELEGRAM, Gson().toJson(newProducts))
        tg.pid = newProducts.maxBy { it.pid }!!.pid
        mongoTemplate.save(tg)
    }

    private fun dispatchNewEmail(line: ProductLine) {
        var q = Query()
            .addCriteria(
                Criteria.where("line").`is`(line.name)
                    .and("downTime").`is`(null)
            )
        val upProducts = mongoTemplate.find(q, Product::class.java)
        if (upProducts.isEmpty()) {
            return
        }
        q = Query().addCriteria(
            Criteria.where("_id.line").`is`(line.name)
                .and("_id.email").ne(TELEGRAM_ADDRESS)
        )
        val subUsers = mongoTemplate.find(q, Subscription::class.java)
        subUsers.forEach { sub ->
            val unReceivedProducts = upProducts.filter { it.pid > sub.pid }
            if (unReceivedProducts.isEmpty()) {
                return
            }
            val job = MessageJob(unReceivedProducts, sub.sid.email)
            vertx.eventBus().send(EVENTBUS_EMAIL, Gson().toJson(job))
            sub.pid = unReceivedProducts.maxBy { it.pid }!!.pid
            mongoTemplate.save(sub)
        }
    }
}

val httpClientOptions =
    webClientOptionsOf(userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15")

class CrawlerVerticle : AbstractVerticle() {

    private val config: Config = SpringContainer[Config::class.java]
    private val client = OkHttpClient.Builder()
        .addInterceptor(DefaultInterceptor())
        .cache(null)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .cookieJar(DefaultCookieJar())
        .build()
    private val DEFAULT_PERIOD = 10000L

    override fun start() {
        for (line in ProductLine.values()) {
            vertx.setPeriodic(config.interval ?: DEFAULT_PERIOD) {
                crawl(line)
            }
            vertx.eventBus().consumer<String>(EVNETBUS_DOWN) {
                val product = Gson().fromJson<Product>(it.body(), Product::class.java)
                crawlProductBuyable(product.code, it)
            }
        }
    }

    private fun crawlProductBuyable(code: String, msg: Message<String>) {
        val req =
            Request.Builder().url("https://www.apple.com.cn/cn/shop/buyability-message?parts.0=$code").get().build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val obj = JsonParser.parseString(response.body!!.string()).asJsonObject
                val b =
                    obj["body"].asJsonObject["content"].asJsonObject["buyabilityMessage"].asJsonObject["sth"].asJsonObject[code].asJsonObject["isBuyable"].asBoolean
                msg.reply(b)
            }
        })
    }

    private fun crawl(line: ProductLine) {
        lastCheckTime = MyDate()
        logger.debug("正在爬取苹果官网/$line")
        val req = Request.Builder().url("https://www.apple.com.cn/cn/shop/refurbished/${line.name.toLowerCase()}").get()
            .build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                var html: String = "success"
                try {
                    html = response.body!!.string()
                    val soup = Jsoup.parse(html)
                    val json = soup.select("#page > div:nth-child(13) > script")[0].html()
                        .removePrefix("window.REFURB_GRID_BOOTSTRAP = ").removeSuffix(";")
                    val obj = JsonParser.parseString(json).asJsonObject
                    val products = obj["tiles"].asJsonArray.map { it.asJsonObject }
                        .filter { it["omnitureModel"].asJsonObject["customerCommitString"].asString == "有现货" }
                        .map {
                            Product(
                                0L,
                                it["title"].asString,
                                line,
                                it["partNumber"].asString
                            )
                        }

                    if (DEBUG && line == ProductLine.IPAD) {
                        writeFile("${Date().time.toString()}-${products.size}", html)
                    }
                    vertx.eventBus().send(line.name, Gson().toJson(products))
                } catch (e: Exception) {
                    logger.error(e)
                    e.printStackTrace()
                    writeFile("${Date().time.toString()}-${e.message}", html)
                }
            }
        })
    }
}