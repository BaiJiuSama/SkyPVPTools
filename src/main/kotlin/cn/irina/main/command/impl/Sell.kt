package cn.irina.main.command.impl

import cn.irina.main.SkyPVPTools
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class Sell : Listener , CommandExecutor {
    private val plugin: JavaPlugin = SkyPVPTools.plugin!!
    private val config = plugin.config

    private val configMessages get() = config.getConfigurationSection("Message")!!
    private val sellGUIName by lazy { config.getString("SellGUIName").colorize() }
    private val sellGUISlot by lazy { config.getInt("SellGUISlot") }
    private val loreDetermine by lazy { config.getBoolean("LoreDetermine") }
    private val sellLore by lazy { config.getString("SellLore").colorize() }

    private val saleSuccess get() = configMessages.getString("SaleSuccess").colorize()
    private val notForSale get() = configMessages.getString("NotforSale").colorize()
    private val noItems get() = configMessages.getString("Noitems").colorize()
    private val noHandItems get() = configMessages.getString("NoHandItems").colorize()
    private val errorSellPrice get() = configMessages.getString("ErrorSellPrice").colorize()
    private val addSellItem get() = configMessages.getString("AddSellItem").colorize()
    private val noPermission get() = configMessages.getString("NoPermission").colorize()

    fun String.colorize() = replace('&', '§')

    private val blackGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()).apply {
        itemMeta = itemMeta.apply { displayName = "§7分割线" }
    }

    private val yellowGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, 4.toShort()).apply {
        itemMeta = itemMeta.apply {
            displayName = "§e点击出售"
            lore = listOf("§7§o请放入你要出售的物品")
        }
    }

    private var economy: Economy? = SkyPVPTools.instance.economy

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.view.title != sellGUIName) return
        val item = e.currentItem ?: return
        val meta = item.itemMeta ?: return

        when (meta.displayName) {
            "§e点击出售" -> handleSellClick(e)
            "§7分割线" -> e.isCancelled = true
        }
    }

    private fun handleSellClick(e: InventoryClickEvent) {
        e.isCancelled = true
        val inventory = e.inventory
        val player = e.whoClicked as? Player ?: return

        val (slotCount, totalPrice) = calculateTotalPrice(inventory)

        when {
            slotCount == 0 -> player.sendMessage(noItems)
            totalPrice > 0 -> {
                economy?.depositPlayer(player, totalPrice)
                player.sendMessage(saleSuccess.replace("%price%", "%.2f".format(totalPrice)))
                clearInventory(inventory)
            }
            else -> player.sendMessage(notForSale)
        }
    }

    private fun calculateTotalPrice(inv: Inventory): Pair<Int, Double> {
        var slotCount = 0
        var total = 0.0

        (0 until sellGUISlot - 9).forEach { i ->
            inv.getItem(i)?.takeIf { it.type != Material.AIR }?.let { item ->
                slotCount++
                total += if (loreDetermine) {
                    getPriceFromLore(item) * item.amount
                } else {
                    config.getDouble("SellItem.${item.type}") * item.amount
                }
            }
        }
        return Pair(slotCount, total)
    }

    private fun getPriceFromLore(item: ItemStack): Double {
        return item.itemMeta?.lore?.filter { it.contains(sellLore) }
            ?.sumOf { lore ->
                lore.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
            } ?: 0.0
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (e.view.title == sellGUIName) {
            (0 until sellGUISlot - 9).forEach { i ->
                e.inventory.getItem(i)?.takeIf { it.type != Material.AIR }?.let {
                    (e.player as? Player)?.inventory?.addItem(it) ?: e.player.world.dropItemNaturally(e.player.location, it)
                    e.inventory.setItem(i, null)
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        when {
            args.isEmpty() -> handleSellCommand(sender)
            args[0].equals("reload", true) -> handleReload(sender)
            args[0].equals("id", true) -> handleIdCommand(sender)
            args[0].equals("add", true) && args.size >= 2 -> handleAddCommand(sender, args[1])
        }
        return true
    }

    private fun handleSellCommand(sender: CommandSender) {
        if (sender !is Player) return sender.sendMessage("§f[§e出售§f] 当前指令只允许玩家执行")
        if (!sender.hasPermission("irina.sell.use")) return sender.sendMessage(noPermission)

        val inv = Bukkit.createInventory(null, sellGUISlot, sellGUIName)
        (sellGUISlot - 9 until sellGUISlot).forEach { i ->
            inv.setItem(i, if (i == sellGUISlot - 5) yellowGlass else blackGlass)
        }
        sender.openInventory(inv)
    }

    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("irina.sell.reload")) return sender.sendMessage(noPermission)
        plugin.reloadConfig()
        sender.sendMessage("§f[§e出售§f] 配置文件已重新加载")
    }

    private fun handleIdCommand(sender: CommandSender) {
        if (sender !is Player) return sender.sendMessage("§f[§e出售§f] 当前指令只允许玩家执行")
        if (!sender.hasPermission("irina.sell.id")) return sender.sendMessage(noPermission)

        val item = sender.inventory.itemInHand
        sender.sendMessage("§f[§e出售§f] 当前手持物品名为 §e${item.type}")
    }

    private fun handleAddCommand(sender: CommandSender, priceArg: String) {
        if (sender !is Player) return sender.sendMessage("§f[§e出售§f] 当前指令只允许玩家执行")
        if (!sender.hasPermission("irina.sell.add")) return sender.sendMessage(noPermission)

        val price = priceArg.toDoubleOrNull() ?: return sender.sendMessage(errorSellPrice)
        val item = sender.inventory.itemInHand.takeIf { it.type != Material.AIR }
            ?: return sender.sendMessage(noHandItems)

        config.set("SellItem.${item.type}", price)
        plugin.saveConfig()
        sender.sendMessage(addSellItem.replace("%ItemType%", item.type.name).replace("%price%", "%.2f".format(price)))
    }

    private fun clearInventory(inv: Inventory) {
        (0 until sellGUISlot - 9).forEach { inv.setItem(it, null) }
    }
}