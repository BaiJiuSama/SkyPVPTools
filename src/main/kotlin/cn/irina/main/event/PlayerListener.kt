package cn.irina.main.event

import cn.irina.main.SkyPVPTools
import cn.irina.main.util.Chat
import cn.irina.main.util.PApi
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PlayerListener : Listener {
    private val plugin: Plugin = SkyPVPTools.instance

    @EventHandler (priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage = null
        Bukkit.broadcastMessage(Chat.normalTranslate(PApi.parsePlaceholders(player, plugin.config.getString("Message.Join"))))
    }

    @EventHandler (priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        event.quitMessage = null
        Bukkit.broadcastMessage(Chat.normalTranslate(PApi.parsePlaceholders(player, plugin.config.getString("Message.Quit"))))
    }
}