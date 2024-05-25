package us.timinc.mc.cobblemon.fieldmoves.influences

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.pokemon.Nature
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves

class SynchronizedNature(val player: ServerPlayerEntity) : SpawningInfluence {
    override fun affectAction(action: SpawnAction<*>) {
        if (action !is PokemonSpawnAction) return
        if (action.props.nature != null) return

        val nature = getSynchronizedNature(player)?.name?.namespace ?: return

        action.props.nature = nature
    }

    private fun getSynchronizedNature(player: PlayerEntity): Nature? {
        val playerPartyStore = Cobblemon.storage.getParty(player.uuid)
        if (CobblemonFieldMoves.config.mustBeFirst) {
            val firstPartyMember = playerPartyStore.firstOrNull()
            if (firstPartyMember?.ability?.name != "synchronize") {
                return null
            }
            return firstPartyMember.nature
        }

        return playerPartyStore.find { it.ability.name == "synchronize" }?.nature
    }
}