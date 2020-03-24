package cn.kherrisan.dragonfly.telegram

import com.google.gson.Gson
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.*
import java.util.regex.Pattern


@RestController
class MainController {

    private val logger = LogManager.getLogger()

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    val regEx1 =
        "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"

    val pattern = Pattern.compile(regEx1)

    private fun isEmail(email: String): Boolean = pattern.matcher(email).matches()

    @PostMapping("/registration/{email}/{line}")
    fun registerEmail(@PathVariable email: String, @PathVariable line: String): String {
        invokeCounter++
        val q = Query(Criteria.where("_id.email").`is`(email).and("_id.line").`is`(line.toUpperCase()))
        val res = mongoTemplate.findOne(q, Subscription::class.java)
        if (res != null) {
            return Gson().toJson(Response("该邮箱已经订阅过了", false))
        }
        val r = Subscription(email, ProductLine.valueOf(line.toUpperCase()))
        mongoTemplate.save(r)
        return Gson().toJson(Response("订阅成功"))
    }

    @DeleteMapping("/registration/{email}/{line}")
    fun unregisterEmail(@PathVariable email: String, @PathVariable line: String): String {
        invokeCounter++
        val res =
            mongoTemplate.findAndRemove(
                Query.query(
                    Criteria.where("_id.email").`is`(email).and("_id.line").`is`(line.toUpperCase())
                ), Subscription::class.java
            )
        return if (res == null) {
            Gson().toJson(Response("该邮箱还没有订阅", false))
        } else {
            Gson().toJson(Response("取消订阅成功"))
        }
    }

    @GetMapping("/registration/{email}/{line}")
    fun checkRegisteration(@PathVariable email: String, @PathVariable line: String): String {
        invokeCounter++
        if (!isEmail(email)) {
            return Gson().toJson(Response("邮箱格式错误", false))
        }
        val q = Query(Criteria.where("_id.email").`is`(email).and("_id.line").`is`(line.toUpperCase()))
        val res = mongoTemplate.findOne(q, Subscription::class.java)
        return if (res != null) {
            Gson().toJson(Response("取消订阅", true))
        } else {
            Gson().toJson(Response("订阅", true))
        }
    }

    @GetMapping("/system")
    fun getSystemInfo(): String {
        invokeCounter++
        var info = mutableMapOf<String, Any>()
        info["startTime"] = systemStartTime.toString()
        info["invoke"] = invokeCounter.toString()
        info["lastCheck"] = lastCheckTime.toString()
        return Gson().toJson(Response(info))
    }
}

data class Response(
    val msg: Any,
    val success: Boolean = true
)
