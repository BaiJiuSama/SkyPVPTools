package cn.irina.main.util

import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.PacketPlayOutChat
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object ActionBar {
    fun sendBar(player: Player, message: String) {
        val components = ChatComponentText(Chat.normalTranslate(message))
        val packet = PacketPlayOutChat(components, 2.toByte())
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }
}