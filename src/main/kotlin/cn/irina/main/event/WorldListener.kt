package cn.irina.main.event

import cn.irina.main.SkyPVPTools.Companion.plugin
import cn.irina.main.util.Chat.Companion.normalTranslate
import cn.irina.main.util.Chat.Companion.translate
import cn.irina.main.util.RandomUtil
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.DyeColor.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.Yaml
import java.io.File


class WorldListener: Listener {
    val regionsFolder = File("plugins/MineResetLite", "mines")
    val denyBlocks: List<Material> = listOf(
        Material.LADDER
    )

    val allowBreakWorlds: List<String> = listOf(
        "world",
        "plotworld"
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
        if (player.hasPermission("irina.admin") || allowBreakWorlds.contains(player.world.name.lowercase())) return

        if (!checkPlayerInAllRegions(player)) {
            event.isCancelled = true
            player.sendMessage(translate("&c你不可以在此地开采!"))
            return
        }

        val block = event.block
        val blockType = block.type

        if (blockType !in validBlockTypes) return
        if (!slotsIsEmpty(player)) {
            handleFullInventory(event, player)
            return
        }

        val handItem = player.itemInHand
        if (isTool(handItem)) {
            var durabilityLevel = handItem.getEnchantmentLevel(Enchantment.DURABILITY)
            if (durabilityLevel < 0) durabilityLevel = 0

            if (handItem.durability >= handItem.type.maxDurability) {
                player.sendMessage(translate("&cLLLLLL你的工具爆了"))
                player.inventory.itemInHand = ItemStack(Material.AIR)
            } else if (!RandomUtil.hasSuccessfullyByChance((durabilityLevel * 5) * 0.01)) {
                val newDurability = (handItem.durability + 1).toShort()
                handItem.durability = newDurability
                player.inventory.itemInHand = handItem
            }
            player.updateInventory()

            var fortuneLevel = handItem.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
            player.giveExp(20 + (fortuneLevel * 10))
        }

        when (blockType) {
            Material.OBSIDIAN -> handleObsidian(event, player)
            Material.STAINED_CLAY -> handleStainedClay(event, player, block)
            else -> return
        }
    }

    fun checkPlayerInAllRegions(player: Player): Boolean {
        if (!regionsFolder.exists() || !regionsFolder.isDirectory()) {
            regionsFolder.mkdirs()
            return false
        }

        val files: Array<File>? = regionsFolder.listFiles { _, name -> name.lowercase().endsWith(".yml") }
        if (files == null) return false

        val loc: Location = player.location
        val x: Int = loc.blockX
        val y: Int = loc.blockY
        val z: Int = loc.blockZ

        files.forEach { file ->
            val config = Yaml().loadAs(file.inputStream(), Map::class.java)

            val mineMap = config["mine"] as? Map<*, *> ?: return@forEach

            val maxX = (mineMap["maxX"] as? Int) ?: return@forEach
            val minX = (mineMap["minX"] as? Int) ?: return@forEach
            val maxY = (mineMap["maxY"] as? Int)?.plus(3) ?: return@forEach
            val minY = (mineMap["minY"] as? Int) ?: return@forEach
            val maxZ = (mineMap["maxZ"] as? Int) ?: return@forEach
            val minZ = (mineMap["minZ"] as? Int) ?: return@forEach
            val worldName = mineMap["world"] as? String ?: return@forEach

            if (x in minX..maxX && y in minY..maxY && z in minZ..maxZ && player.world.name == worldName) {
                return true
            }
        }

        return false
    }

    private fun isTool(item: ItemStack): Boolean {
        val itemType = item.type.name.lowercase()
        return itemType.endsWith("_pickaxe") || itemType.endsWith("_axe")
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
        ).withDefault { "&7&n原石" }
    }

    private fun crystalLore(): List<String> {
        return translate(listOf(
            "&7一种可被开采的晶石",
            "&7稍微有那么一些价值",
            "",
            "&7品质: &f普通",
            "&7回收价: &e${RandomUtil.helpMeToChooseOne(20, 25, 35)}$"
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
            "&7回收价: &e888$"
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