package com.justai.jaicf.template.scenario

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.dto.TextReply
import com.justai.jaicf.channel.jaicp.reactions.jaicp
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.hook.*
import com.justai.jaicf.logging.*
import com.justai.jaicf.reactions.*
import com.justai.jaicf.logging.ReactionRegistrar
import com.justai.jaicf.template.scenario.generateNumber
import com.justai.jaicf.template.scenario.countBullsAndCows

val mainScenario = Scenario {

    /*handle<AfterProcessHook>{
        println("----BOT----")
        val notSayReactions = reactions.executionContext.reactions.filterNot {it is SayReaction}
        val replies = reactions.executionContext.reactions.filterIsInstance<SayReaction>()
        //val check = reactions.executionContext.reactions
        if (replies.isNotEmpty() && replies.size > 1) {
            //aggregatedReply = mutableListOf<Reaction>(SayReaction(replies.joinToString("\n\n")))
            //println((replies.joinToString("\n\n") {it.text}))
            var processedReply = mutableListOf<Reaction>(reactions.say(replies.joinToString("\n\n") {it.text}))
            //Reactions.jaicp.replies =
            if (notSayReactions.isNotEmpty()) {
                val n = notSayReactions.size-1
                for (i in 0..n) {
                    processedReply.add(notSayReactions[i])
                }
            }
            //processedReply.add(notSayReactions)
            //println(reactions.executionContext.reactions)
            //val processedReply = mutableListOf<Reaction>(reactions.say("5"))
            //reactions.say("$processedReply")
            //reactions.executionContext.reactions = mutableListOf(aggregatedReply)
            //reactions.executionContext.reactions = mutableListOf()
            reactions.executionContext = ExecutionContext(
                reactions.executionContext.requestContext,
                reactions.executionContext.activationContext,
                reactions.executionContext.botContext,
                reactions.executionContext.request,
                reactions.executionContext.firstState,
                processedReply,
                reactions.executionContext.input,
                reactions.executionContext.scenarioException,
                reactions.executionContext.isNewUser
            )
        }
    }*/


    state("start") {
        activators {
            regex("/start")
            intent("Hello")
            intent("LetsPlay")
        }
        action {
            if (context.client["welcome"] == null) {
                context.client["welcome"] = true
                reactions.say("Привет! Сыграем в игру \"Быки и коровы\"? Чтобы узнать правила, пиши \"правила\".")
            } else if (context.client["win"] == true) {
                context.client["win"] = null
                reactions.say("Хочешь снова сыграть в \"Быки и коровы\"? Если забыл правила, пиши \"правила\".")
            } else {
                reactions.say("Хочешь сыграть в \"Быки и коровы\"? Если нужно напомнить правила, пиши \"правила\".")
            }
            reactions.buttons("да" toState "/yes", "нет" toState "/no", "правила" toState "/rules")
        }
    }

    state("rules") {
        activators {
            regex("правила")
        }
        action {
            reactions.run {
                say(
                    "Я задумываю <u>4-значное</u> число с <u>неповторяющимися</u> цифрами, твоя задача его угадать.\n" +
                            "            В ответ я скажу сколько быков (сколько цифр ты угадал вплоть до позиции) и коров (без совпадения с позицией).\n" +
                            "            Например: Я загадал число 3219, ты пробуешь отгадать и называешь 2310.\n" +
                            "            В результате это две <b>коровы</b> (две цифры: \"2\" и \"3\" — угаданы на неверных позициях) и один <b>бык</b> (одна цифра \"1\" угадана вплоть до позиции)." +
                            "            Начинаем?"
                )
                buttons("да" toState "/yes", "нет" toState "/no")
            }
        }
    }

   state("yes") {
       activators {
           intent("Yes")
       }
       activators ("/rules") {
           intent("LetsPlay")
       }
       activators ("/game/play") {
           intent("LetsPlay")
       }
       action {
           reactions.run {
               context.session["botNumber"] = generateNumber()
               say("Здорово! Когда надоест, просто скажи \"<b>стоп</b>\" или \"<b>хватит</b>\"!")
               say("Я загадал число, попробуй его угадать. Введи четырехзначное число.")
               go("/game")
           }
       }
   }

   state("game") {

       state("play") {
           activators {
               regex("\\d+")
           }
           action {
               val botNumber = context.session["botNumber"] as String
               val playerNumber = request.input
               val playerNumberUnique = (playerNumber.split("")).distinct()
               if (playerNumber.length != 4) {
                   reactions.say("Число должно быть <b>четырехзначным</b>.")
                   reactions.go("/game")
               //5 [,1,2,3,4]
               } else if (playerNumberUnique.size != 5) {
                   reactions.say("Цифры не должны повторяться.")
                   reactions.go("/game")
               } else {
                   // reactions.say(botNumber)
                   val (bulls, cows) = countBullsAndCows(botNumber, playerNumber)
                   reactions.say("Число быков: $bulls, число коров: $cows")
                   if (bulls == 4) {
                       context.client["win"] = true
                       reactions.say("Поздравляю! Ты угадал число! Хочешь сыграть еще раз?")
                   } else {
                       reactions.say("Ты пока не угадал число! Попытайся еще. Введи четырехзначное число.")
                   }
               }
           }
       }

       state("localNo") {
           activators {
               intent("No")
           }
           action {
               reactions.say("Спасибо за игру!")
               reactions.go("/bye")
           }
       }

       state("localCatchAll") {
           activators {
               catchAll()
           }
           action {
               reactions.say("Неверный формат введеного числа.")
               reactions.go("/game")
           }
       }
   }

   state("no") {
       activators {
           intent("No")
       }
       action {
           reactions.say("Если захочешь поиграть, пиши \"давай поиграем\"!")
       }
   }

   state("stop") {
       activators {
           intent("Stop")
       }
       action {
           reactions.say("Спасибо за игру!")
           reactions.go("/bye")
       }
   }

   state("bye") {
       activators {
           intent("Bye")
       }
       action {
           reactions.say("Пока! Приходи еще!")
           context.cleanSessionData()
       }
   }

   state("reset") {
       activators {
           regex("reset")
       }
       action {
           reactions.say("Сброс переменных")
           context.cleanSessionData()
       }
   }

   fallback {
       reactions.say("Я ничего не понял. Переформулируй свой запрос.")
   }
}