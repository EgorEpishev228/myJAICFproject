package com.justai.jaicf.template.test

import com.justai.jaicf.template.scenario.mainScenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test


class BullsAndCowsScenarioTest: ScenarioTest(mainScenario) {

    @Test
    fun `Say Hello`() {
        intent("Hello") endsWithState "/start"
    }

    @Test
    fun `Agree To Play`() {
        withCurrentState("/start")
        intent("Yes") goesToState("/yes") endsWithState ("/game")
    }

    @Test
    fun `Disagree To Play`() {
        withCurrentState("/start")
        intent("No") endsWithState ("/no")
    }

    @Test
    fun `Play Game` () {
        withCurrentState("/start")
        query("правила") endsWithState("/rules")
        intent("Yes") goesToState("/yes") endsWithState ("/game")
        withBotContext {
            session["botNumber"] = "1234"
        }
        query("1234") endsWithState("/game/play")
    }

    @Test
    fun `Stop Game` () {
        withCurrentState("/game")
        intent("Stop") goesToState("/stop") endsWithState("/bye")
    }

    @Test
    fun `Imitate Game` () {
        query("/start") goesToState("/start")
        query("правила") goesToState("/rules")
        intent("Yes") goesToState ("/yes") endsWithState("/game")
        withBotContext {
            session["botNumber"] = "5163"
        }
        query("5569") goesToState ("/game/play") endsWithState("/game")
        query("51645") goesToState ("/game/play") endsWithState("/game")
        query("5163") endsWithState("/game/play")
        intent("No") goesToState ("/game/localNo") endsWithState("/bye")
    }
}