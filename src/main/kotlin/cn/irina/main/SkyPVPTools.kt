package cn.irina.main

import cn.irina.main.command.MainCommand
import org.bukkit.plugin.java.JavaPlugin
import cn.irina.main.util.Chat
import cn.irina.main.util.ClassUtil
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.lang.reflect.InvocationTargetException

class SkyPVPTools : JavaPlugin() {
    companion object {
        var plugin: JavaPlugin? = null

        @JvmStatic
        lateinit var instance: SkyPVPTools
            private set
    }

    init {
        plugin = this
        instance = this
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, {
            val xConomy = Bukkit.getPluginManager().getPlugin("XConomy")
            val pApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")

            if (xConomy == null || !xConomy.isEnabled) {
                Chat.log("&cXConomy 未启动")
                Bukkit.getPluginManager().disablePlugin(this)
                return@runTaskLaterAsynchronously
            }

            if (pApi == null || !pApi.isEnabled) {
                Chat.log("&cPlaceholderAPI 未启动")
                Bukkit.getPluginManager().disablePlugin(this)
                return@runTaskLaterAsynchronously
            }

            loadListener()
            registerCommand()

            Chat.log("&aSkyPVPTools 已启动")
        }, 21L)
    }

    override fun onDisable() {
        Chat.log("&cSkyPVPTools 已关闭")
    }

    fun registerCommand() {
        getCommand("irina").apply { executor = MainCommand() }
    }

    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    fun loadListener() {
        val classes = ClassUtil.getClassesInPackage(this, "cn.irina")

        if (classes == null) return

        for (clazz in classes) {
            if (!Listener::class.java.isAssignableFrom(clazz!!)) continue
            Chat.log("&a注册事件: &e${clazz.simpleName}")
            Bukkit.getPluginManager().registerEvents(clazz.getDeclaredConstructor().newInstance() as Listener, instance)
        }
    }
}
