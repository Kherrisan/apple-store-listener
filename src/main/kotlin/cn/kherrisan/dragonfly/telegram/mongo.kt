package cn.kherrisan.dragonfly.telegram

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import java.util.*

@Component
class SaveEventListener : AbstractMongoEventListener<Any>() {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun onBeforeSave(event: BeforeSaveEvent<Any>) {
        val source = event.source
        ReflectionUtils.doWithFields(source.javaClass) {
            ReflectionUtils.makeAccessible(it)
            if (it.isAnnotationPresent(AutoIncrement::class.java)
                && it.get(source) is Number && it.getLong(source) == 0L
            ) {
                it.set(source, nextId(source.javaClass))
            }
        }
    }

    override fun onBeforeConvert(event: BeforeConvertEvent<Any>) {
        val source = event.source
        ReflectionUtils.doWithFields(source.javaClass) {
            ReflectionUtils.makeAccessible(it)
            if (it.isAnnotationPresent(AutoIncrement::class.java)
                && it.get(source) is Number && it.getLong(source) == 0L
            ) {
                it.set(source, nextId(source.javaClass))
            }
        }
    }

    private fun nextId(cls: Class<Any>): Long {
        val query = Query(Criteria.where("name").`is`(cls.simpleName))
        val u = Update()
        u.inc("seq", 1)
        val options = FindAndModifyOptions()
        options.upsert(true)
        options.returnNew(true)
        val seq = mongoTemplate.findAndModify(query, u, options, Sequence::class.java)
        return seq.seq
    }
}


@Configuration
open class MongoConfig {

    @Bean
    open fun customConversions(): MongoCustomConversions = MongoCustomConversions(
        listOf(
            MyDateReadingConverter()
        )
    )
}

class MyDateReadingConverter : Converter<Date, MyDate> {
    override fun convert(source: Date): MyDate? = MyDate(source.time)
}