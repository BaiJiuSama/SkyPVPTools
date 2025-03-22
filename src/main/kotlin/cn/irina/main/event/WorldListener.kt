package cn.irina.main.event

import cn.irina.main.SkyPVPTools
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.weather.WeatherChangeEvent

class WorldListener: Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onWeatherChange(event: WeatherChangeEvent) {
        event.isCancelled = SkyPVPTools.plugin!!.config.getBoolean("DenyWeatherChange")
    }
}