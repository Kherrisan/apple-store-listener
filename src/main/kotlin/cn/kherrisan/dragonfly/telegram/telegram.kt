package cn.kherrisan.dragonfly.telegram

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.vertx.core.AbstractVerticle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy

@Component
class TelegramBot(
    @Autowired
    private val config: Config
) : AbilityBot(config.token, config.name) {

    override fun creatorId(): Int = config.creatorId!!

    fun sendMessage(text: String) {
        logger.trace("Send telegram message: $text")
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

class TelegramVerticle : AbstractVerticle() {

    private val telegramBot = SpringContainer[TelegramBot::class.java]

    override fun start() {
        vertx.eventBus().consumer<String>(EVENTBUS_TELEGRAM) { it ->
            val type = object : TypeToken<List<Product>>() {}.type
            val msg = Gson().fromJson<List<Product>>(it.body(), type)
            val line = msg[0].line
            vertx.executeBlocking<Unit>({ p ->
                telegramBot.sendMessage("Apple Store翻新区上架了 ${msg.size} 台 $line 设备，链接为 https://www.apple.com.cn/cn-k12/shop/refurbished/${line.name.toLowerCase()}")
                p.complete()
            }, { ar ->
                if (ar.failed()) {
                    logger.error(ar.cause())
                    ar.cause().printStackTrace()
                }
            })
        }
    }
}