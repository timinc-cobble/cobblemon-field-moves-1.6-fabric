package us.timinc.mc.cobblemon.fieldmoves

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import net.fabricmc.api.ModInitializer
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import us.timinc.mc.cobblemon.fieldmoves.config.Config
import us.timinc.mc.cobblemon.fieldmoves.influences.SynchronizedNature
import us.timinc.mc.cobblemon.unimplementeditems.config.ConfigBuilder

class CobblemonFieldMoves : ModInitializer {
    companion object {
        const val MOD_ID = "field_moves"
        var config: Config = ConfigBuilder.load(Config::class.java, MOD_ID)
    }

    override fun onInitialize() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { evt ->
            val world = evt.winners.flatMap { it.pokemonList }.firstNotNullOfOrNull { it.entity?.world }
            if (world?.server == null) return@subscribe
            for (winner in evt.winners) {
                val position = winner.pokemonList.firstNotNullOf { it.entity }.blockPos
                val vecPos = Vec3d(position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
                for (battlePokemon in winner.pokemonList) {
                    val pokemon = battlePokemon.effectedPokemon
                    debug("Rolling for ${pokemon.ability.name} on ${pokemon.getDisplayName().string}")

                    if (!pokemon.heldItem().isEmpty) {
                        debug("${pokemon.getDisplayName().string} is already holding an item")
                        continue
                    }

                    val identifier = Identifier("pickup", "gameplay/${pokemon.ability.name}")
                    val lootManager = world.server!!.lootManager
                    val lootTable = lootManager.getLootTable(identifier)
                    val list = lootTable.generateLoot(
                        LootContextParameterSet(
                            world as ServerWorld,
                            mapOf(
                                LootContextParameters.ORIGIN to vecPos
                            ),
                            mapOf(),
                            0F
                        )
                    )

                    if (list.isEmpty) {
                        debug("Attempted to roll for ability ${pokemon.ability.name} but nothing dropped.")
                        continue
                    }

                    pokemon.swapHeldItem(list.first())
                }
            }
        }
        PlayerSpawnerFactory.influenceBuilders.add { SynchronizedNature(it) }
    }

    fun debug(msg: String) {
        if (!config.debug) return
        println(msg)
    }
}