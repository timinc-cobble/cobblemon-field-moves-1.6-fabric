package us.timinc.mc.cobblemon.fieldmoves.droppers

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import us.timinc.mc.cobblemon.droploottables.lootconditions.LootConditions
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.debug
import us.timinc.mc.cobblemon.fieldmoves.droppers.contexts.AbilityDropContext

object CombatAbilityDropper : AbstractAbilityDropper("ability/victory") {
    override fun load() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val level =
                event.winners.flatMap { it.pokemonList }.firstNotNullOfOrNull { it.entity?.level() } ?: return@subscribe
            if (level !is ServerLevel) return@subscribe
            for (winner in event.winners) {
                for (battlePokemon in winner.pokemonList) {
                    val pokemon = battlePokemon.effectedPokemon
                    val player = pokemon.getOwnerPlayer() ?: continue
                    val wasInBattle = battlePokemon.facedOpponents.isNotEmpty()
                    processDrop(level, player, pokemon, wasInBattle)
                }
            }
        }
    }

    private fun processDrop(level: ServerLevel, player: ServerPlayer, pokemon: Pokemon, wasInBattle: Boolean) {
        if (!pokemon.heldItem().isEmpty) return

        debug("Rolling on ${pokemon.species.name}'s ${pokemon.ability.name}")

        val lootParams = LootParams(
            level,
            mapOf(
                LootContextParams.ORIGIN to player.position(),
                LootContextParams.THIS_ENTITY to player,
                LootConditions.PARAMS.POKEMON_DETAILS to pokemon,
                LootConditions.PARAMS.WAS_IN_BATTLE to wasInBattle
            ),
            mapOf(),
            player.luck
        )
        val drop = getDrops(
            lootParams,
            AbilityDropContext(pokemon.ability)
        ).randomOrNull() ?: return

        debug("Got $drop for ${pokemon.species.name}'s ${pokemon.ability.name}")

        if (CobblemonFieldMoves.config.addDropsToBundles) {
            for (itemStack in player.inventory.items) {
                if (!itemStack.`is`(Items.BUNDLE)) continue
                val bundleContents =
                    BundleContents.Mutable(itemStack.get(DataComponents.BUNDLE_CONTENTS) as BundleContents)
                bundleContents.tryInsert(drop)
                itemStack.set(DataComponents.BUNDLE_CONTENTS, bundleContents.toImmutable())
                if (drop.isEmpty) break
            }
        }
        if (!drop.isEmpty) pokemon.swapHeldItem(drop)
    }
}