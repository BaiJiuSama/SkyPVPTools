package cn.irina.main.event

import cn.irina.main.SkyPVPTools
import cn.irina.main.util.ActionBar
import cn.irina.main.util.Chat
import cn.irina.main.util.PApi
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PlayerListener : Listener {
    private val plugin: Plugin = SkyPVPTools.instance

    @EventHandler (priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage = null
        Bukkit.broadcastMessage(Chat.normalTranslate(PApi.parsePlaceholders(player, plugin.config.getString("OtherMessage.Join"))))
    }

    @EventHandler (priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        event.quitMessage = null
        Bukkit.broadcastMessage(Chat.normalTranslate(PApi.parsePlaceholders(player, plugin.config.getString("OtherMessage.Quit"))))
    }

    @EventHandler (priority = EventPriority.MONITOR)
    fun onAttack(event: EntityDamageByEntityEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            if (event.damager !is Player || event.entity !is Player) return@Runnable
            val attacker = event.damager as Player

            if (!attacker.hasPermission("irina.healthCheck")) return@Runnable
            val victim = event.entity as Player

            val health = victim.health / 2
            val formattedHealth = Chat.normalTranslate(String.format("%.1f", health))

            ActionBar.sendBar(attacker, Chat.normalTranslate("&e目标 &f${victim.displayName} &e剩余血量: &c$formattedHealth❤"))
        })
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val rawMessage = plugin.config.getString("OtherMessage.Chat") ?: "%s: %s"

        val parsedMessage = PApi.parsePlaceholders(player, rawMessage.replace("!msg", event.message))

        event.format = Chat.normalTranslate(String.format("%s", parsedMessage.replace("%", "%%")))
    }
}