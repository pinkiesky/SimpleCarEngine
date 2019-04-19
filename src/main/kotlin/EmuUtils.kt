object EmuUtils {
    val defaultSleepTime: Long = 16

    fun sleep(name: String): Long {
        Thread.sleep(defaultSleepTime)
        return defaultSleepTime
    }

    fun tick(name: String) = sleep(name)

    fun namedTick(name: String): () -> Long {
        return { tick(name) }
    }
}