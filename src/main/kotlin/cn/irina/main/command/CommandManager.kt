package cn.irina.main.command

import cn.irina.main.SkyPVPTools
import cn.irina.main.command.impl.FastGameMode
import cn.irina.main.command.impl.LoreControl
import cn.irina.main.command.impl.MainCommand
import cn.irina.main.command.impl.Sell

object CommandManager {
    private val plugin = SkyPVPTools.Companion.plugin!!

    fun registerCommand() {
        plugin.apply {
            getCommand("irina").apply { executor = MainCommand() }
            getCommand("gm").apply { executor = FastGameMode() }
            getCommand("sell").apply { executor = Sell() }
            getCommand("lore").apply { executor = LoreControl() }
        }
    }
}