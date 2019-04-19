package gui

import CANBus
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.Slider
import javafx.scene.control.ToggleButton

/**
 * Created by naik on 06.02.16.
 */
class MainController {

    var canBus: CANBus? = null
        set(value) {
            field = value
            field?.addListener("engine", "rpm") {
                Platform.runLater {
                    rpmBar.progress = it.details / 8000
                    rpmLabel.text = it.details.toString()
                }
            }

            field?.addListener("wheel", "speed") {
                Platform.runLater {
                    speedBar.progress = it.details / 200
                    speedLabel.text = it.details.toString()
                }
            }
        }

    @FXML
    lateinit var throttleSlider: Slider

    @FXML
    lateinit var gearSlider: Slider

    @FXML
    lateinit var rpmBar: ProgressBar

    @FXML
    lateinit var speedBar: ProgressBar

    @FXML
    lateinit var rpmLabel: Label

    @FXML
    lateinit var speedLabel: Label

    @FXML
    lateinit var engineToggleButton: ToggleButton

    fun initialize() {
        engineToggleButton.selectedProperty().addListener { _, old, new ->
            canBus!!.send("engine", "ignite", if (new) 1.0 else 0.0)
        }

        throttleSlider.valueProperty().addListener { _, old, new ->
            canBus!!.send("engine", "throttle", new.toDouble() / 100.0)
        }

        gearSlider.valueProperty().addListener { _, old, new ->
            if (new.toDouble() - new.toLong() <= 0.000001) {
                println("SET, ${new.toDouble() - new.toLong()}")
                canBus!!.send("trans", "gear", new.toLong().toDouble())
            }
        }
    }
}
