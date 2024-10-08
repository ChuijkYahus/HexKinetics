package net.sonunte.hexkinetics.common.casting.actions.great_spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpAcceleration : SpellAction {

	override val argc = 3
	private val entityFastTicks = HashMap<Entity, Int>()
	private val entityWaitTicks = HashMap<Entity, Int>()

	private var supertime = 0
	override val isGreat = true

	private var speed = Vec3(0.0, 0.0, 0.0)

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val target = args.getEntity(0, argc)
		val time = Mth.clamp(args.getDouble(1, argc), 0.0, 200.0)
		val force = args.getVec3(2, argc)

		val cost = if(force.length() >= 1)
		{
			(force.lengthSqr() * time * MediaConstants.DUST_UNIT).toInt()
		}else if (force.length() > 0)
		{
			MediaConstants.DUST_UNIT * time.toInt()
		}else
		{
			0
		}

		ctx.assertEntityInRange(target)


		return Triple(
			Spell(target, time, force),
			cost,
			listOf(ParticleSpray.burst(target.position().add(0.0, target.eyeHeight / 2.0, 0.0),1.0)),
		)
	}

	private data class Spell(val target: Entity, val time: Double, val force: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			if (entityFastTicks.containsKey(target))
				return   // don't change the already set propulsion
			supertime = time.toInt() * 5 + 5
			entityFastTicks[target] = supertime
			entityWaitTicks[target] = 0
			speed = force
		}
	}

	@JvmStatic
	fun tickAcceleratedEntities() {
		val it: MutableIterator<MutableMap.MutableEntry<Entity, Int>> = entityFastTicks.iterator()
		while (it.hasNext()) {
			val next = it.next()
			val entity = next.key
			val ticks = next.value

			if (!entity.isRemoved && ticks > 0) {
				val wait = entityWaitTicks[entity] ?: 0

				if (wait >= 0) {
					if (wait == 5)
					{
						entity.push(speed.x, speed.y, speed.z)
						entity.hurtMarked = true
					}
					entityWaitTicks[entity] = wait + 1
				}
				entityFastTicks[entity] = ticks - 1

				if (wait < 0 || wait > 5) {
					entityWaitTicks[entity] = 0
				}
			} else {
				it.remove()
				entityFastTicks.remove(entity)
				entityWaitTicks.remove(entity)
			}
		}
	}
}