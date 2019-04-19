package engine

import CANBus
import EmuUtils
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong


class Engine(val params: EngineParams, bus: CANBus) : Thread("engine.Engine ${System.currentTimeMillis()}") {
    private val send2bus = bus.named("engine")
    private val tick = EmuUtils.namedTick("engine_${hashCode()}")
    private var throttle: Double = 0.07

    private var status: EngineStatus = Initialize
        set(value) {
            if (value::class == field::class && value.code == field.code) {
                return
            }

            field = value
            send2bus("status", value.code.toDouble())
        }

    private val _cutoff = AtomicBoolean(false)
    private var cutoff: Boolean
        get() = _cutoff.get()
        set(value) {
            _cutoff.set(value)
            send2bus("cutoff", if (value) 1.0 else 0.0)
        }

    private val _rpm = AtomicLong(0)
    var rpm: RPM
        set(value) = _rpm.set(value.toLong())
        get() = _rpm.get().toDouble()

    init {
        bus.addListener("engine", "ignite") {
            if (it.details == 1.0) {
                ignite()
            } else {
                down()
            }
        }

        bus.addListener("engine", "throttle") {
            throttle = Math.max(0.1, it.details)
        }
    }

    fun ignite() {
        status = Starting
    }

    fun down() {
        status = Initialize
    }

    override fun run() {
        while (!isInterrupted) {
            val status = this.status
            if (status is Starting && rpm < params.minRPM) {
                _rpm.getAndUpdate {
                    (it + params.starterRPM).toLong()
                }
            } else if (status is Starting && rpm >= params.minRPM) {
                this.status = Started
            } else if (status is Started && rpm < params.minRPM) {
                this.status = Stopped
            } else if (rpm >= params.cutoffThreshold) {
                cutoff = true
            } else if (status is Started) {
                _rpm.getAndUpdate {
                    (it + throttle * 200).toLong()
                }
            } else if (cutoff && rpm <= params.cutoffRPM) {
                cutoff = false
            } else if (cutoff) {
                // CUTOFF!
            } else if (status is Initialize) {
                // Just wait...
            } else {
                this.status = Errored(1)
            }

            _rpm.getAndUpdate {
                (it * 0.99).toLong()
            }
            send2bus("rpm", rpm)

            tick()
        }
    }
}