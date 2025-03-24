package cn.irina.main.event

import cn.irina.main.SkyPVPTools
import cn.irina.main.util.ActionBar
import cn.irina.main.util.Chat
import cn.irina.main.util.ChatComponentBuilder
import cn.irina.main.util.PApi
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.PermissionNode
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.text.append

class PlayerListener : Listener {
    private val plugin: Plugin = SkyPVPTools.instance
    val rawMessage = plugin.config.getString("OtherMessage.Chat") ?: "%s: %s"
    val luckPerms = LuckPermsProvider.get()

    @EventHandler (priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val user = luckPerms.userManager.getUser(player.uniqueId)
        val node = PermissionNode.builder("irina.test").build()
        user!!.data().add(node)
        luckPerms.userManager.saveUser(user)

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

        val parsedMessage = PApi.parsePlaceholders(player, rawMessage.replace("!msg", event.message))

        event.format = Chat.normalTranslate(String.format("%s", parsedMessage.replace("%", "%%")))
    }

    @EventHandler
    fun onShow(event: AsyncPlayerChatEvent) {
        val player = event.getPlayer()
        if (event.message.lowercase() != "%i") return

        val handItem = player.inventory.itemInHand
        val handItemNBT = getItemNBT(handItem)

        val item = arrayOf<BaseComponent>(TextComponent(handItemNBT))

        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, item)
        val messageShow = if (handItem == null || handItem.type.equals(Material.AIR)) {
            "我是一个空气, 你看你妈呢"
            return
        }
        else handItem.itemMeta.displayName ?: handItem.type.name.uppercase()

        val itemHover = ChatComponentBuilder(messageShow).setCurrentHoverEvent(hoverEvent).create()
        val parsedFormat = Chat.normalTranslate(String.format("%s", PApi.parsePlaceholders(player, rawMessage.replace("!msg", "")).replace("%", "%%")))

        val msg = ChatComponentBuilder(parsedFormat).append(itemHover).create()

        for (p in Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(*msg)
        }

        event.isCancelled = true
    }

    private fun getItemNBT(item: ItemStack?): String {
        if (item == null || item.type == Material.AIR) return Material.AIR.toString()
        val nmsItem = CraftItemStack.asNMSCopy(item)
        val tag = NBTTagCompound()
        nmsItem.save(tag)
        return tag.toString()
    }
}