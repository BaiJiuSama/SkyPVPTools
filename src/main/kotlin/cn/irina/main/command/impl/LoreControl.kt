package cn.irina.main.command.impl

import cn.irina.main.util.Chat
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoreControl: CommandExecutor {
    private val successMessage = Chat.translate("&a成功!")
    private val needHandItem = Chat.translate("&c请手持物品!")

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Chat.translate("&c非玩家不可执行此指令!"))
            return true
        }

        val player = sender
        if (!player.hasPermission("irina.admin")) {
            sender.sendMessage(Chat.translate("&c废物没权限用你妈呢"))
            return true
        }

        if (args.isEmpty()) {
            showHelp(player)
            return true
        }

        val item = player.itemInHand
        if (itemIsNull(item)) {
            player.sendMessage(needHandItem)
            return true
        }

        when (args[0].lowercase()) {
            "name" -> {
                if (args.size < 2) {
                    player.sendMessage(Chat.translate("&cName <String>"))
                    return true
                }

                val string = Chat.normalTranslate(args.drop(1).joinToString(" "))

                val meta = item.itemMeta
                meta.displayName = string
                item.itemMeta = meta
            }
            "set" -> {
                if (args.size < 3 || args[1].toIntOrNull() == null) {
                    player.sendMessage(Chat.translate("&cSet <line> <String>"))
                    return true
                }

                val line: Int = args[1].toInt()
                if (line - 1 < 0) {
                    player.sendMessage(Chat.translate("&c拜托...参数 <line> 最低只能为 1 诶"))
                    return true
                }

                val string = Chat.normalTranslate(args.drop(2).joinToString(" "))

                val meta = item.itemMeta
                val lore = meta.lore ?: mutableListOf()
                while (lore.size <= line) lore.add("")

                lore[line] = string
                meta.lore = lore
                item.itemMeta = meta
            }

            "add" -> {
                if (args.size < 2) {
                    player.sendMessage(Chat.translate("&cAdd <String>"))
                    return true
                }

                val string = Chat.normalTranslate(args.drop(1).joinToString(" "))
                val meta = item.itemMeta
                val lore = meta.lore ?: mutableListOf()

                lore.add(string)
                meta.lore = lore
                item.itemMeta = meta
            }

            "del" -> {
                if (args.size < 2 || args[1].toIntOrNull() == null) {
                    player.sendMessage(Chat.translate("&cDelete <line>"))
                    return true
                }

                val line = args[1].toInt()
                if (line - 1 < 0) {
                    player.sendMessage(Chat.translate("&c拜托...参数 <line> 最低只能为 1 诶"))
                    return true
                }

                val meta = item.itemMeta
                val lore = meta.lore ?: mutableListOf()
                if (lore.isEmpty()) {
                    player.sendMessage(Chat.translate("&c唔诶...这个物品没有Lore给你删掉了诶......"))
                    return true
                }

                lore[line - 1] = ""
                meta.lore = lore
                item.itemMeta = meta
            }

            "ldel" -> {
                val meta = item.itemMeta
                val lore = meta.lore ?: mutableListOf()
                if (lore.isEmpty()) {
                    player.sendMessage(Chat.translate("&c唔诶...这个物品没有Lore给你删掉了诶......"))
                    return true
                }

                lore.removeAt(lore.size - 1)
                meta.lore = lore
                item.itemMeta = meta
            }

            else -> { sender.sendMessage(Chat.translate("&c唔诶...我不明白你的操作诶......")); return true}
        }
        player.itemInHand = item
        player.sendMessage(successMessage)
        return true
    }

    fun itemIsNull(item: ItemStack): Boolean {
        return item.type.equals(Material.AIR)
    }

    fun showHelp(sender: CommandSender) {
        val list = Chat.translate(listOf<String>(
            "&b&l| ",
            "&b&l| &f&lSkyPVPTools &cLoreControl &7&l- &f&lBy &b&lI&f&lRINA",
            "&b&l| &f/lore",
            "&b&l| &f- name (设置物品的炫酷吊炸天名称)",
            "&b&l| &f- set <Line> <String> (设置第 Line 行的字符串为 String)",
            "&b&l| &f- add <String> (在原Lore的基础上添加一行 String)",
            "&b&l| &f- del <Line> (删除第 Line 行的 Lore)",
            "&b&l| &f- ldel (删除最后一行的Lore)",
            "&b&l| "
        ))

        list.forEach { sender.sendMessage(Chat.normalTranslate(it)) }
    }
}