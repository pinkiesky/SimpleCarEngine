import engine.RPM
import java.lang.Math.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

data class Wheel(val radiusSm: Double, val bus: CANBus) : Thread("wheel ${System.currentTimeMillis()}") {
    private val send2bus = bus.named("wheel")
    private val tick = EmuUtils.namedTick("wheel_${hashCode()}")

    private val rpm: AtomicLong = AtomicLong(0)

    val kmh: Double
        get() = rpm.get() * radiusSm * 3.14 * 2 / 100 / 1000 * 60

    fun applyRPM(desire: Double): RPM {
        val delta = abs(desire - rpm.get())
        val deltaWithSlow = min(sqrt(delta * 6), delta)
        rpm.getAndUpdate {
            if (rpm.get() > desire) {
                (it - deltaWithSlow).toLong()
            } else {
                (it + deltaWithSlow).toLong()
            }
        }

        return rpm.get().toDouble()
    }

    override fun run() {
        while (!isInterrupted) {
            send2bus("speed", kmh)
            rpm.getAndUpdate {
                (it * 0.98).toLong()
            }

            tick()
        }
    }

}