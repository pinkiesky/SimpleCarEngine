package transmission

import engine.Engine
import CANBus
import Wheel


class Transmission(val params: TransmissionParams, val engine: Engine, val wheel: Wheel, bus: CANBus) : Thread("trans ${System.currentTimeMillis()}") {
    private val send2bus = bus.named("trans")
    private val tick = EmuUtils.namedTick("trans_${hashCode()}")

    init {
        bus.addListener("trans", "gear") {
            gear = it.details.toLong()
        }
    }

    var multiplier: Double = 0.0
    var gear: Long = 0
        set(value) {
            if (field == value) {
                return
            }

            multiplier = if (value == 0L) 0.0 else params.gears[(value - 1).toInt()]
            field = value

            send2bus("gear", value.toDouble())
        }

    override fun run() {
        while (!isInterrupted) {
            if (gear != 0L) {
                val rpmToWheels = engine.rpm / params.mainGear / multiplier
                val realRpm = wheel.applyRPM(rpmToWheels)
                engine.rpm = realRpm * params.mainGear * multiplier
            }

            tick()
        }
    }
}