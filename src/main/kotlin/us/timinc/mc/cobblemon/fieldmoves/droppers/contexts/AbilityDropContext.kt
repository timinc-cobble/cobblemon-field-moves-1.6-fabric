package us.timinc.mc.cobblemon.fieldmoves.droppers.contexts

import com.cobblemon.mod.common.api.abilities.Ability
import us.timinc.mc.cobblemon.droploottables.api.droppers.DropContext

data class AbilityDropContext(
    val ability: Ability,
) : DropContext