package cn.irina.main.event

import cn.irina.main.SkyPVPTools.Companion.plugin
import cn.irina.main.util.Chat.Companion.normalTranslate
import cn.irina.main.util.Chat.Companion.translate
import cn.irina.main.util.RandomUtil
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.DyeColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import kotlin.getValue

class WorldListener: Listener {
    val denyBlocks: List<Material> = listOf(
        Material.LADDER
    )

    @EventHandler(priority = EventPriority.LOWEST)
    fun onWeatherChange(event: WeatherChangeEvent) {
        event.isCancelled = plugin!!.config.getBoolean("DenyWeatherChange")
    }

    @EventHandler (priority = EventPriority.LOWEST)
    fun denyBreak(event: BlockBreakEvent) {
        if (!denyBlocks.contains(event.block.type) || event.player.hasPermission("irina.admin")) return
        event.isCancelled = true
        event.player.sendMessage(translate("&c此方块不被允许破坏"))
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.hasPermission("irina.admin")) return

        val block = event.block
        val blockType = block.type

        if (blockType !in validBlockTypes) return
        if (!slotsIsEmpty(player)) {
            handleFullInventory(event, player)
            return
        }

        when (blockType) {
            Material.OBSIDIAN -> handleObsidian(event, player)
            Material.STAINED_CLAY -> handleStainedClay(event, player, block)
            else -> return
        }
    }

    private val validBlockTypes = setOf(Material.STAINED_CLAY, Material.OBSIDIAN)

    private val crystalNames by lazy {
        mapOf(
            WHITE to "&f&n白晶石",
            BLACK to "&0&n黑晶石",
            BLUE to "&9&n蓝晶石",
            RED to "&c&n红晶石",
            ORANGE to "&6&n橙晶石",
            MAGENTA to "&d&n品红晶石",
            LIGHT_BLUE to "&b&n淡蓝晶石",
            YELLOW to "&e&n黄晶石",
            LIME to "&a&n亮绿晶石",
            PINK to "&d&n粉晶石",
            GRAY to "&8&n灰晶石",
            SILVER to "&7&n浅灰晶石",
            CYAN to "&3&n青晶石",
            PURPLE to "&5&n紫晶石",
            BROWN to "&4&n棕晶石",
            GREEN to "&a&n绿晶石"
        ).withDefault { "&7&n未知晶石" }
    }

    private fun crystalLore(): List<String> {
        return translate(listOf(
            "&7一种可被开采的晶石",
            "&7稍微有那么一些价值",
            "",
            "&7品质: &f普通",
            "&7回收价: &e${RandomUtil.helpMeToChooseOne(50, 60, 70)}$"
        ))
    }

    private fun handleFullInventory(event: BlockBreakEvent, player: Player) {
        event.isCancelled = true
        player.sendMessage(translate("&c背包已满, 无法继续挖掘!"))
    }

    private fun handleObsidian(event: BlockBreakEvent, player: Player) {
        event.block.type = Material.AIR
        player.inventory.addItem(obsidian())
    }

    private fun handleStainedClay(event: BlockBreakEvent, player: Player, block: Block) {
        event.expToDrop = 20

        if (RandomUtil.hasSuccessfullyByChance(0.01) || player.hasPermission("irina.crystal")) {
            handleLegendaryCrystal(player)
            event.block.type = Material.AIR
            return
        }

        generateNormalCrystal(event, player, block)
    }

    private fun handleLegendaryCrystal(player: Player) {
        Bukkit.getScheduler().runTask(plugin) {
            player.sendMessage(translate("&6&n恭喜!&7 你发掘到了传奇品质的晶石!"))
            player.playSound(player.location, Sound.LEVEL_UP, 1f, 1f)
        }
        player.inventory.addItem(legendaryCrystal())
    }

    private fun generateNormalCrystal(event: BlockBreakEvent, player: Player, block: Block) {
        val colorData = block.data
        val dyeColor = DyeColor.getByData(colorData.toByte())

        val itemStack = ItemStack(Material.STAINED_GLASS, 1).apply {
            itemMeta = (itemMeta ?: return@apply).also { meta ->
                meta.displayName = normalTranslate(crystalNames[dyeColor]!!)
                meta.lore = crystalLore()
            }
        }

        event.block.type = Material.AIR
        player.inventory.addItem(itemStack)
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        if (player.hasPermission("irina.admin")) return

        val item = player.itemInHand
        if (item.type != Material.STAINED_GLASS) return

        val itemMeta = item.itemMeta ?: return
        val displayName = itemMeta.displayName ?: return
        if (!displayName.contains("晶石")) return

        event.isCancelled = true
        player.sendMessage(translate("&c该物品无法被放置!"))
    }

    fun legendaryCrystal(): ItemStack {
        val item = ItemStack(Material.NETHER_STAR, 1)
        val meta = item.itemMeta
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.addAll(listOf(
            "&6传奇品质的晶石",
            "&6赚飞了兄弟!",
            "",
            "&7品质: &6传奇",
            "&7回收价: &e1000$"
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
            "&c坚硬的金刚石",
            "&c但也很值钱",
            "",
            "&7品质: &d稀有",
            "&7回收价: &e600$"
        ))

        meta.displayName = normalTranslate("&c&n金刚晶石")
        meta.lore = translate(lore)
        item.itemMeta = meta
        return item
    }

    fun slotsIsEmpty(player: Player): Boolean {
        return player.inventory.firstEmpty() != -1
    }
}