package com.justai.jaicf.template.scenario

import com.justai.jaicf.template.scenario.mainScenario

fun generateNumber(): String {
    var botNumber = arrayOf<Int>()
    var botNumberString = ""
    do {
        val randomInt = (0..9).random()
        if ((randomInt != 0 || botNumber.isNotEmpty()) && (randomInt !in botNumber)) {
            botNumber += randomInt
            botNumberString += randomInt
        }
    } while (botNumber.size < 4)
    return botNumberString
}

fun countBullsAndCows(botNumber: String, playerNumber: String): Pair<Int,Int> {
    var bulls = 0
    var cows = 0
    for (i in 0..3) {
        if (playerNumber[i] == botNumber[i]) {
            bulls ++
        } else if (playerNumber[i] in botNumber) {
            cows ++
        }
    }
    return Pair(bulls, cows)
}
