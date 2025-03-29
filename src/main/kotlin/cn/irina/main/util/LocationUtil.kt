package cn.irina.main.util

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.entity.Player

class LocationUtil {
    companion object {
        private val worldGuard: WorldGuardPlugin = WorldGuardPlugin.inst()

        fun isPlayerInRegion(player: Player, regionName: String): Boolean {
            val regionManager = worldGuard.regionContainer.get(player.world) ?: return false

            val bukkitLocation = player.location

            val applicableRegions = regionManager.getApplicableRegions(bukkitLocation)

            return applicableRegions.regions.any { region ->
                region.id.equals(regionName, ignoreCase = true)
            }
        }
    }
}