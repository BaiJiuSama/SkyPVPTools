package cn.irina.main.util

import java.util.Random

class RandomUtil {
    companion object {
        private val random: Random = Random()

        fun hasSuccessfullyByChance(chance: Double): Boolean {
            if (chance <= 0) return false
            if (chance >= 1) return true

            return random.nextDouble() < chance
        }

        fun helpMeToChooseOne(vararg entry: Any): Any {
            return entry[random.nextInt(entry.size)]
        }
    }
}