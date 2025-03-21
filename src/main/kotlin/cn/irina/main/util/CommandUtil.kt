package cn.irina.main.util

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.SimplePluginManager

object CommandUtil {
    private val commandMap: SimpleCommandMap? by lazy {
        (Bukkit.getPluginManager() as? SimplePluginManager)?.let { pluginManager ->
            pluginManager.javaClass.getDeclaredField("commandMap").apply { isAccessible = true }.get(pluginManager) as? SimpleCommandMap
        }
    }

    fun registerCommand(
        name: String,
        aliases: Array<String> = emptyArray(),
        description: String = "",
        usage: String = "/$name",
        executor: (CommandSender, Array<String>) -> Boolean
    ) {
        val cmd = object : Command(name, description, usage, listOf(*aliases)) {
            override fun execute(sender: CommandSender, label: String, args: Array<String>) = executor(sender, args)
        }
        commandMap?.register(name, cmd)
    }
}