package com.nov0cx.uhc.scenarios

import com.nov0cx.uhc.listener.listen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

class ScenarioManger {
    companion object STATIC {
        private var instance: ScenarioManger? = null

        fun get(): ScenarioManger {
            if (instance != null)
                return instance!!
            else {
                instance = ScenarioManger()
                return instance!!
            }
        }
    }

    val scenarios: ArrayList<Scenario> = ArrayList()

    init {
        scenarios.add(CutClean())
        scenarios.add(Diamondless())
        scenarios.add(Timebomb())
    }

    fun getScenario(s: String): Scenario {
        return scenarios.parallelStream().filter { sen -> sen.name == s }.findFirst().get()
    }
}

interface Scenario {
    var enabled: Boolean
    val name: String
}

class CutClean(override var enabled: Boolean = false, override val name: String = "CutClean") : Scenario {

    init {
        listen<BlockBreakEvent>(EventPriority.HIGHEST) {
            when (it.block.type) {
                Material.IRON_ORE ->
                    changeBlockDrop(it.block, Material.IRON_INGOT)
                Material.GOLD_ORE ->
                    changeBlockDrop(it.block, Material.GOLD_INGOT)
                else -> {

                }
            }
        }

        listen<EntityDeathEvent> {
            when (it.entityType) {
                EntityType.COW -> {
                    it.drops.clear()
                    val stack: ItemStack = ItemStack(Material.COOKED_BEEF)
                    stack.amount = ThreadLocalRandom.current().nextInt(1, 4)
                    it.drops.add(stack)
                }
                EntityType.CHICKEN -> {
                    it.drops.clear()
                    val stack: ItemStack = ItemStack(Material.COOKED_CHICKEN)
                    stack.amount = ThreadLocalRandom.current().nextInt(1, 3)
                    it.drops.add(stack)
                }
                EntityType.SHEEP -> {
                    it.drops.clear()
                    val stack: ItemStack = ItemStack(Material.COOKED_MUTTON)
                    stack.amount = ThreadLocalRandom.current().nextInt(1, 3)
                    it.drops.add(stack)
                }
                EntityType.RABBIT -> {
                    it.drops.clear()
                    val stack: ItemStack = ItemStack(Material.COOKED_RABBIT)
                    stack.amount = ThreadLocalRandom.current().nextInt(1, 2)
                    it.drops.add(stack)
                }
                else -> {

                }
            }
        }
    }

    private fun changeBlockDrop(block: Block, newDrop: Material) {
        block.drops.clear()
        block.drops.add(ItemStack(newDrop))
    }
}

class Diamondless(override var enabled: Boolean = false, override val name: String = "Diamondless") : Scenario {
    init {
        listen<BlockBreakEvent> {
            if (it.block.type == Material.DIAMOND_ORE)
                it.isCancelled = true
        }

        listen<PlayerDeathEvent> {
            val loc = it.entity.location
            if (!ScenarioManger.get().getScenario("Timebomb").enabled)
                it.entity.world.dropItem(loc, ItemStack(Material.DIAMOND))
        }

    }
}

class Timebomb(override var enabled: Boolean = false, override val name: String = "Timebomb") : Scenario {

    init {
        listen<PlayerDeathEvent> {
            val loc = it.entity.location
            loc.clone().add(0.0, 1.0, 0.0).block.type = Material.CHEST
            val inv = ((loc.clone().add(0.0, 1.0, 0.0).block.state) as Chest).blockInventory
            inv.clear()

            for (i in 0..inv.size) {
                inv.setItem(i, it.entity.inventory.getItem(i))
            }

            if (!ScenarioManger.get().getScenario("Diamondless").enabled)
                return@listen

            if (isInventoryFull(inv))
                it.entity.world.dropItem(loc, ItemStack(Material.DIAMOND))
            else
                inv.addItem(ItemStack(Material.DIAMOND))
        }
    }

    fun isInventoryFull(inv: Inventory): Boolean {
        return inv.firstEmpty() == -1;
    }



}