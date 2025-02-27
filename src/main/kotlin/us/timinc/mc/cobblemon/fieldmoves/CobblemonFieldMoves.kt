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

object CobblemonFieldMoves : ModInitializer {
    const val MOD_ID = "field_moves"
    var config: Config = ConfigBuilder.load(Config::class.java, MOD_ID)

    override fun onInitialize() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { evt ->
            val world = evt.winners.flatMap { it.pokemonList }.firstNotNullOfOrNull { it.entity?.level() }
            if (world?.server == null) return@subscribe
            for (winner in evt.winners) {
                val position = winner.pokemonList.firstNotNullOf { it.entity }.position()
                for (battlePokemon in winner.pokemonList) {
                    val pokemon = battlePokemon.effectedPokemon
                    val ability = pokemon.ability.name
                    val lootManager = world.server!!.reloadableRegistries()
                    val identifier = modIdentifier("gameplay/${ability}")

                    if (!lootManager.getKeys(Registries.LOOT_TABLE).contains(identifier)) {
                        debug("$ability does not have a loot table associated with it.")
                        continue
                    }
                    debug("Rolling for ${pokemon.ability.name} on ${pokemon.getDisplayName().string}")

                    if (!pokemon.heldItem().isEmpty) {
                        debug("${pokemon.getDisplayName().string} is already holding an item")
                        continue
                    }

                    val lootTable = lootManager.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, identifier))
                    val list = lootTable.getRandomItems(
                        LootParams(
                            world as ServerLevel,
                            mapOf(
                                LootContextParams.ORIGIN to position
                            ),
                            mapOf(),
                            0F
                        )
                    ).filter { !it.isEmpty }
                    debug("$list")

                    if (list.isEmpty()) {
                        debug("Attempted to roll for ability ${pokemon.ability.name} but nothing dropped.")
                        continue
                    }

                    pokemon.swapHeldItem(list.first())
                }
            }
        }
        PlayerSpawnerFactory.influenceBuilders.add(::SynchronizedNature)
        PlayerSpawnerFactory.influenceBuilders.add(::CuteCharm)
    }

    fun modIdentifier(name: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name)
    }

    fun debug(msg: String) {
        if (!config.debug) return
        println(msg)
    }
}