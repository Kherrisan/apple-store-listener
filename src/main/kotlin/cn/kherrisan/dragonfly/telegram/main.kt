package cn.kherrisan.dragonfly.telegram

import com.google.gson.Gson
import io.vertx.core.Vertx
import io.vertx.kotlin.core.cli.optionOf
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.reflect.KClass

var invokeCounter = 0
var systemStartTime = MyDate()
var lastCheckTime = MyDate()
val logger = LogManager.getLogger()

fun main() {
    runApplication<SpringStarter>()
    val config = SpringContainer[Config::class]
    val vertx = Vertx.vertx()
    if (config.tg!!) {
        ApiContextInitializer.init()
        val api = TelegramBotsApi()
        try {
            api.registerBot(SpringContainer[TelegramBot::class])
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vertx.deployVerticle(TelegramVerticle())
        SpringContainer[TelegramBot::class].sendMessage("Apple-Store-listener已启动")
    }
    vertx.deployVerticle(EmailVerticle())
    vertx.deployVerticle(DispatcherVerticle())
    vertx.deployVerticle(CrawlerVerticle())
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


@SpringBootApplication
open class SpringStarter