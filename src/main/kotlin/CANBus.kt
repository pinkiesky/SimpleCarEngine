import java.util.concurrent.LinkedBlockingQueue

typealias CANListener = (CANMessage) -> Unit
data class CANMessage(
    val name: String,
    val area: String,
    val details: Double = 0.0,
    val destinationHashCode: Int = (name to area).hashCode()
)

class CANBus : Thread("CANBus") {
    private val listenerMap = hashMapOf<Int, MutableList<CANListener>>()
    private val queue = LinkedBlockingQueue<CANMessage>()

    fun send(name: String, area: String, details: Double) {
        queue.offer(CANMessage(name, area, details))
    }

    fun named(name: String) = { area: String, details: Double -> send(name, area, details) }

    override fun run() {
        while (!isInterrupted) {
            val message = queue.take()
            listenerMap[message.destinationHashCode]?.forEach {
                it(message)
            }

            listenerMap[0]?.forEach {
                it(message)
            }
        }
    }

    fun addListener(name: String, area: String, listener: CANListener) {
        val key = CANMessage(name, area).destinationHashCode
        println(key)
        if (!listenerMap.contains(key)) {
            listenerMap[key] = arrayListOf()
        }

        val listeners = listenerMap[key]!!
        listeners.add(listener)
    }

    fun removeListener(listener: CANListener) {
        listenerMap.values.forEach {
            it.removeAll { it == listener }
        }
    }
}