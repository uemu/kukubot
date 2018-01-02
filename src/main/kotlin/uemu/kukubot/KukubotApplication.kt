package uemu.kukubot

import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
@LineMessageHandler
class KukubotApplication {

  private val userContextMap = ConcurrentHashMap<String, UserContext>()

  @Value("\${line.bot.channelToken}")
  private val channelToken = ""

  @EventMapping
  fun handleTextMessageEvent(messageEvent: MessageEvent<TextMessageContent>): List<TextMessage> {
    val context = userContextMap.getOrPut(messageEvent.source.userId) { UserContext() }
    val response = ArrayList<TextMessage>()
    val text = messageEvent.message.text

    if (text == "か") {
      context.mode = text
      context.fixedNumber = 0
      response.add(TextMessage("かけ算になったよ"))
    } else if (text == "わ") {
      context.mode = text
      context.fixedNumber = 0
      response.add(TextMessage("わり算になったよ"))
    } else if (text.matches(Regex("だ[1-9]"))) {
      context.fixedNumber = text.substring(1).toInt()
      response.add(TextMessage("${context.fixedNumber}のだんになったよ"))
    } else if (context.answer != 0 && text.matches(Regex("[0-9]+"))) {
      if (text.toInt() == context.answer) {
        context.combo++
        response.add(TextMessage("正かい！" + if (context.combo == 1) "" else " ${context.combo}れんぞく"))
      } else {
        context.combo = 0
        response.add(TextMessage("ざんねん ... 答えは${context.answer}"))
      }
    } else {
      response.add(TextMessage("[つかい方]\nか -> かけ算になるよ\nわ -> わり算になるよ\nだ数字 -> だんをえらべるよ\n数字 -> 答えられるよ"))
    }

    if (context.mode == "か") {
      context.firstNumber = if (context.fixedNumber == 0) (Math.random() * 9).toInt() + 1 else context.fixedNumber
      context.secondNumber = (Math.random() * 9).toInt() + 1
      context.answer = context.firstNumber * context.secondNumber
      response.add(TextMessage("${context.firstNumber} × ${context.secondNumber} = "))
    } else {
      context.answer = (Math.random() * 9).toInt() + 1
      context.secondNumber = if (context.fixedNumber == 0) (Math.random() * 9).toInt() + 1 else context.fixedNumber
      context.firstNumber = context.secondNumber * context.answer
      response.add(TextMessage("${context.firstNumber} ÷ ${context.secondNumber} = "))
    }

    return response
  }

  @EventMapping
  fun handleDefaultMessageEvent(event: Event) {
  }

}

class UserContext(var mode: String = "か",
                  var fixedNumber: Int = 0,
                  var firstNumber: Int = 0,
                  var secondNumber: Int = 0,
                  var answer: Int = 0,
                  var combo: Int = 0)

fun main(args: Array<String>) {
  SpringApplication.run(KukubotApplication::class.java, *args)
}
