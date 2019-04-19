package engine

typealias RPM = Double

data class EngineParams(
    val name: String,
    val cutoffThreshold: RPM,
    val cutoffRPM: RPM,
    val minRPM: RPM,
    val maxRPM: RPM,
    val deltaRPM: RPM = maxRPM - minRPM,
    val starterRPM: RPM
)