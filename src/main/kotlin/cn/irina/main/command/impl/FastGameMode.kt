package cn.irina.main.command.impl

import cn.irina.main.util.Chat
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FastGameMode: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<out String>): Boolean {
        (sender !is Player).apply {
            sender.sendMessage(Chat.translate("&c非玩家不可执行此指令!"))
            return true
        }

        val player = sender as Player
        (player.hasPermission("irina.admin")).apply {
            player.sendMessage(Chat.translate("&c你没有权限!"))
            return true
        }

        when (args[0].toInt()) {
            0 -> player.gameMode = GameMode.SURVIVAL
            1 -> player.gameMode = GameMode.CREATIVE
            2 -> player.gameMode = GameMode.ADVENTURE
            3 -> player.gameMode = GameMode.SPECTATOR
            else -> {
                player.sendMessage(Chat.translate("&c/gm < 0 / 1 / 2 / 3 >"))
                return true
            }
        }

        player.sendMessage(Chat.translate("&7现在你的游戏模式为 ${gameModeName(player.gameMode)}"))
        return true
    }

    fun gameModeName(mode: GameMode): String {
        return when (mode) {
            GameMode.SURVIVAL -> "&e生存"
            GameMode.CREATIVE -> "&e创造"
            GameMode.ADVENTURE -> "&e冒险"
            GameMode.SPECTATOR -> "&e旁观"
        }
    }
}

