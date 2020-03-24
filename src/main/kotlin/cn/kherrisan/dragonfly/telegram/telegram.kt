package cn.kherrisan.dragonfly.telegram

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