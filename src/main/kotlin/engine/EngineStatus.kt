package engine

sealed class EngineStatus(val code: Long)
object Unknown : EngineStatus(Long.MAX_VALUE)
object Initialize : EngineStatus(0)
object Starting: EngineStatus(1)
object Started : EngineStatus(2)
object Stopped : EngineStatus(3)
data class Errored(val error: Long) : EngineStatus(-error)
