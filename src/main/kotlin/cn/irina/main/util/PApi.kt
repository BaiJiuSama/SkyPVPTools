package cn.irina.main.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PApi {
    @JvmStatic
    fun parsePlaceholders(player: Player, text: String): String {
        return if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPI.setPlaceholders(player, text)
        } else {
            text
        }
    }
}