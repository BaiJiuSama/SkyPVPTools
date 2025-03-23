package cn.irina.main.util

import java.util.Random

class RandomUtil {
    companion object {
        private val random: Random = Random()

        fun hasSuccessfullyByChance(chance: Double): Boolean {
            if (chance >= 1) {
                return true
            }
            if (chance <= 0) {
                return false
            }
            val i = (random.nextInt(1000000000) / 1000000000.0)

            return i <= chance
        }

        fun helpMeToChooseOne(vararg entry: Any): Any {
            return entry[random.nextInt(entry.size)]
        }
    }

}