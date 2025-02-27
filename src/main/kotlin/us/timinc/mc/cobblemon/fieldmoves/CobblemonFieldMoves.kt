package us.timinc.mc.cobblemon.fieldmoves

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import net.fabricmc.api.ModInitializer
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import us.timinc.mc.cobblemon.fieldmoves.config.Config
import us.timinc.mc.cobblemon.fieldmoves.influences.CuteCharm
import us.timinc.mc.cobblemon.fieldmoves.influences.SynchronizedNature
import us.timinc.mc.cobblemon.fieldmoves.config.ConfigBuilder
import us.timinc.mc.cobblemon.fieldmoves.droppers.FieldMovesDroppers

object CobblemonFieldMoves : ModInitializer {
    const val MOD_ID = "field_moves"
    var config: Config = ConfigBuilder.load(Config::class.java, MOD_ID)

    override fun onInitialize() {
        PlayerSpawnerFactory.influenceBuilders.add(::SynchronizedNature)
        PlayerSpawnerFactory.influenceBuilders.add(::CuteCharm)
        FieldMovesDroppers.load()
    }

    fun modIdentifier(name: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name)
    }

    fun debug(msg: String) {
        if (!config.debug) return
        println(msg)
    }
}