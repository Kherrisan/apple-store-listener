package cn.kherrisan.dragonfly.telegram

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

var hasSentAlert = false

fun main() {
    runApplication<SpringStarter>()
    ApiContextInitializer.init()
    val api = TelegramBotsApi()
    try {
        api.registerBot(SpringContainer[TelegramBot::class])
    } catch (e: Exception) {
        e.printStackTrace()
    }
    SpringContainer[TelegramBot::class].sendMessage("系统启动")
    val client = OkHttpClient()
    val schedule = Executors.newSingleThreadScheduledExecutor()
    schedule.scheduleAtFixedRate(object : Runnable {
        override fun run() {
            println("${Date()}正在检测 Apple 官网翻新产品列表")
            if (hasSentAlert) {
                return
            }
            try {
                val req = Request.Builder().url("https://www.apple.com.cn/cn-k12/shop/refurbished/ipad")
                    .get().build()
                val resp = client.newCall(req).execute()
                val html = resp.body!!.string()
                val soup = Jsoup.parse(html)
                //class="as-gridpage-producttiles as-producttiles pinwheel"
                val ul = soup.select("div.refurbished-category-grid-no-js")[0].select("ul")[0]
                if (ul.select("li").isEmpty()) {
                    hasSentAlert = false
                }
                for (ui in ul.select("li")) {
                    val a = ui.select("a")[0]
                    hasSentAlert = true
                    SpringContainer[TelegramBot::class].sendMessage("${a.text()}，链接：https://www.apple.com.cn/${a.attr("href")}")
                }
            } catch (e: java.lang.Exception) {
                println(e)
            }
        }
    }, 0, SpringContainer[Config::class].interval!!.toLong(), TimeUnit.SECONDS)
}

@RestController
class UselessController {

    @GetMapping("/hello")
    fun hello(): String {
        return "world"
    }

}

@Component
@Lazy(false)
class SpringContainer : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        println("setApplicationContext")
        context = applicationContext
    }

    companion object {
        private lateinit var context: ApplicationContext

        operator fun <T> get(cls: Class<T>): T = context.getBean(cls)

        operator fun <T : Any> get(cls: KClass<T>): T = context.getBean(cls.java)
    }
}

@Component
class TelegramBot(
    @Autowired
    private val config: Config
) : AbilityBot(config.token, config.name) {

    override fun creatorId(): Int = config.creatorId!!

    fun sendMessage(text: String) {
        println("Send telegram message: $text")
        silent.send(text, config.chatId!!)
    }

    fun sayHello(): Ability = Ability.builder()
        .name("hello")
        .info("say hello world~")
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action {
            silent.send("Hello World!", it.chatId())
        }
        .build()
}

@ConfigurationProperties(prefix = "dragonfly")
@Configuration
open class Config(
    var keyword: String? = null,
    var interval: Int? = null,
    var token: String? = System.getenv("token"),
    var name: String? = null,
    var creatorId: Int? = null,
    var chatId: Long? = null
)

@SpringBootApplication
open class SpringStarter