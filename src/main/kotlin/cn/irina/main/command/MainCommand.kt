package cn.irina.main.command

import cn.irina.main.SkyPVPTools
import cn.irina.main.util.Chat
import me.yic.xconomy.api.XConomyAPI
import me.yic.xconomy.data.syncdata.PlayerData
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

class MainCommand : CommandExecutor {
    private val xApi = XConomyAPI()
    val needAmount: BigDecimal = SkyPVPTools.plugin!!.config.getDouble("AmountRequiredForRepair").toBigDecimal()


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "fix" -> {
                if (notPlayer(sender)) {
                    sender.sendMessage(Chat.translate("&c非玩家不可执行此指令!"))
                    return true
                }
                val player: Player = sender as Player

                val item = player.itemInHand
                if (item == null || item.type == Material.AIR) {
                    Chat.sendToPlayer(player, "&c请手持物品")
                    return true
                }

                repair(player, item)
            }

            "help" -> showHelp(sender)

            else -> { sender.sendMessage(Chat.translate("&c唔诶...我不明白你的操作诶......")) }
        }

        return true
    }

    fun repair(player: Player, item: ItemStack) {
        val pd: PlayerData = xApi.getPlayerData(player.uniqueId)
        val balance: BigDecimal = pd.balance

        if (balance <= needAmount) {
            Chat.sendToPlayer(player, "&c你没有足够的钱去修复")
            return
        }

        if (item.durability != 0.toShort()) {
            item.durability = 0.toShort()
            pd.balance = pd.balance - needAmount
            Chat.sendToPlayer(player, "&a修复成功!")
        } else {
            Chat.sendToPlayer(player, "&c该物品无需修复")
        }
    }

    fun notPlayer(sender: CommandSender): Boolean {
        return sender !is Player
    }

    fun showHelp(sender: CommandSender) {
        val list = Chat.translate(listOf<String>(
            "&b&l| ",
            "&b&l| &f&lSkyPVPTools &7&l- &f&lBy &b&lI&f&lRINA",
            "&b&l| &f/irina",
            "&b&l| &f- fix <修复你的装备>",
            "&b&l| &f- bind <绑定你的大神装备使其不会掉落>",
            "&b&l| &f- unbind <对没错这是解绑你的大神装备的>",
            "&b&l| "
        ))

        for (string in list) {
            sender.sendMessage(string)
        }
    }
}