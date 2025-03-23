package cn.irina.main.event

import cn.irina.main.SkyPVPTools.Companion.plugin
import cn.irina.main.util.Chat.Companion.normalTranslate
import cn.irina.main.util.Chat.Companion.translate
import cn.irina.main.util.RandomUtil
import org.bukkit.DyeColor
import org.bukkit.DyeColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack

class WorldListener: Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onWeatherChange(event: WeatherChangeEvent) {
        event.isCancelled = plugin!!.config.getBoolean("DenyWeatherChange")
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.hasPermission("irina.admin")) return

        val block = event.block
        if (block.type.equals(Material.OBSIDIAN)) {
            event.block.drops.clear()
            block.world.dropItemNaturally(block.location, obsidian())
        }

        if (block.type != Material.STAINED_CLAY) return
        event.block.drops.clear()

        if (RandomUtil.hasSuccessfullyByChance(0.01) || player.hasPermission("irina.crystal")) {
            player.sendMessage(translate("&6&n恭喜!&7 你发掘到了传奇品质的晶石!"))
            player.playSound(player.location, Sound.LEVEL_UP, 1f, 1f)
            block.world.dropItemNaturally(block.location, legendaryCrystal())
            return
        }

        val colorData = block.data.toByte()
        val dyeColor = DyeColor.getByData(colorData)

        val itemStack = ItemStack(Material.STAINED_GLASS, 1)
        val itemMeta = itemStack.itemMeta ?: return

        var displayName: String = when (dyeColor) {
            WHITE -> "&f&n白晶石"
            BLACK -> "&0&n黑晶石"
            BLUE -> "&9&l蓝晶石"
            RED -> "&c&l红晶石"
            ORANGE -> "&6&l橙晶石"
            MAGENTA -> "&d&l洋红晶石"
            LIGHT_BLUE -> "&b&l淡蓝晶石"
            YELLOW -> "&e&l黄晶石"
            LIME -> "&a&l亮绿晶石"
            PINK -> "&d&l粉晶石"
            GRAY -> "&8&l灰晶石"
            SILVER -> "&7&l浅灰晶石"
            CYAN -> "&3&l青晶石"
            PURPLE -> "&5&l紫晶石"
            BROWN -> "&4&l棕晶石"
            GREEN -> "&a&l绿晶石"
            else -> "&7&n未知的晶石"
        }

        itemMeta.displayName = normalTranslate(displayName)
        val lore = listOf(
            normalTranslate("&f- &7一种可被挖掘的晶石"),
            normalTranslate("&f- &7可能是一种有价值的矿物")
        )
        itemMeta.lore = lore

        itemStack.itemMeta = itemMeta

        block.world.dropItemNaturally(block.location, itemStack)
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        if (player.hasPermission("irina.admin")) return

        val item = event.block.state as ItemStack
        if (!item.type.equals(Material.STAINED_GLASS) || !item.itemMeta.displayName.contains("晶石")) return
        event.isCancelled = true
        player.sendMessage(translate("&c该物品无法被放置!"))
    }

    fun legendaryCrystal(): ItemStack {
        val item = ItemStack(Material.NETHER_STAR, 1)
        val meta = item.itemMeta
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.addAll(listOf(
            "&f- &6传奇品质的晶石",
            "&f- &6赚飞了兄弟!"
        ))

        meta.displayName = normalTranslate("&f&k!!&r &6&n神话晶石&r &f&k!!")
        meta.lore = translate(lore)
        item.itemMeta = meta
        return item
    }

    fun obsidian(): ItemStack {
        val item = ItemStack(Material.OBSIDIAN, 1)
        val meta = item.itemMeta
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.addAll(listOf(
            "&f- &c坚硬的金刚石",
            "&f- &c但也很值钱"
        ))

        meta.displayName = normalTranslate("&c&n金刚晶石")
        meta.lore = translate(lore)
        item.itemMeta = meta
        return item
    }
}