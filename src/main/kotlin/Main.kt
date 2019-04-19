import engine.Engine
import engine.EngineParams
import gui.MainController
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import transmission.Transmission
import transmission.TransmissionParams


class Main : Application() {
    val bus = CANBus().apply { start() }

    fun createEngine(): Pair<EngineParams, Engine> {
        val engineParams = EngineParams("Jiguli", 6000.0, 5500.0, 500.0, 7000.0, starterRPM = 400.0)
        val engine = Engine(engineParams, bus = bus)

        engine.start()

        return engineParams to engine
    }

    fun createTransmission(engine: Engine, wheel: Wheel): Transmission {
        val transmissionParams = TransmissionParams(3.46, listOf(4.17, 2.34, 1.52, 1.14, 0.87, 0.69))
        return Transmission(transmissionParams, engine, wheel, bus).apply { start() }
    }

    fun createWheel(): Wheel {
        return Wheel(14 * 2.54, bus).apply { start() }
    }

    override fun start(primaryStage: Stage) {
        bus.addListener("engine", "status") {
            println(it)
        }

        val loader = FXMLLoader()
        loader.location = this::class.java.classLoader.getResource("gui.fxml")
        val layout = loader.load<Any>()

        val controller = loader.getController<MainController>()
        controller.canBus = bus

        val wheel = createWheel()
        val (engineParams, engine) = createEngine()
        val trans = createTransmission(engine, wheel)

        primaryStage.scene = Scene(layout as Parent?)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}