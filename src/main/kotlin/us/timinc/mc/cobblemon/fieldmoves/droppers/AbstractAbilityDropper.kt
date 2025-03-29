package us.timinc.mc.cobblemon.fieldmoves.droppers

import com.cobblemon.mod.common.api.abilities.Ability
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootParams
import us.timinc.mc.cobblemon.droploottables.api.droppers.AbstractDropper
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.modIdentifier
import us.timinc.mc.cobblemon.fieldmoves.droppers.contexts.AbilityDropContext

abstract class AbstractAbilityDropper(dropType: String, modId: String = CobblemonFieldMoves.MOD_ID) :
    AbstractDropper<AbilityDropContext>(
        dropType,
        modId
    ) {
    open fun getAbilityDropId(ability: Ability) =
        modIdentifier("$dropType/${ability.name}")

    override fun getDrops(params: LootParams, context: AbilityDropContext): List<ItemStack> {
        val results: MutableList<ItemStack> = mutableListOf()

        results.addAll(getDropsFromTable(params.level, getAbilityDropId(context.ability), params))

        return results
    }
}