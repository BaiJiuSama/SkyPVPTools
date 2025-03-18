package cn.irina.main.util

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object Chat {
    const val PREFIX = "&8[&bI&fRINA&8] &f| "

    @JvmStatic
    fun normalTranslate(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    @JvmStatic
    fun translate(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', PREFIX + message)
    }

    @JvmStatic
    fun translate(messages: List<String>): List<String> {
        val toReturn = ArrayList<String>()

        for (line in messages) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line))
        }

        return toReturn
    }

    @JvmStatic
    fun sendToPlayer(player: Player, message: String) {
        player.sendMessage(translate(message))
    }

    @JvmStatic
    fun log(message: String) {
        Bukkit.getConsoleSender().sendMessage(translate(message))
    }
}