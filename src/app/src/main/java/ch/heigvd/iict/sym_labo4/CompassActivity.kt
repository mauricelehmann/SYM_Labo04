package ch.heigvd.iict.sym_labo4

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer

/**
 * Project: Labo4
 * Created by fabien.dutoit on 21.11.2016
 * Updated by fabien.dutoit on 06.11.2020
 * (C) 2016 - HEIG-VD, IICT
 */
class CompassActivity : AppCompatActivity(), SensorEventListener {

    //opengl
    private lateinit var opglr: OpenGLRenderer
    private lateinit var m3DView: GLSurfaceView

    private lateinit var sensorManager: SensorManager;
    private lateinit var accelerometer: Sensor;
    private lateinit var magnetometer: Sensor;

    //Will hold the latest values from the listener
    private val accelerometerData = FloatArray(3)
    private val magnetometerData = FloatArray(3)

    private val rotationMatrix = FloatArray(16) //4 x 4
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // we need fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // we initiate the view
        setContentView(R.layout.activity_compass)

        //we create the renderer
        opglr = OpenGLRenderer(applicationContext)

        // link to GUI
        m3DView = findViewById(R.id.compass_opengl)

        //init opengl surface view
        m3DView.setRenderer(opglr)


        //Setup of the : sensorManager , accelerometer and magnetometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Register the sensors
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)

    }
    /*
        TODO
        your activity need to register to accelerometer and magnetometer sensors' updates
        then you may want to call
        opglr.swapRotMatrix()
        with the 4x4 rotation matrix, every time a new matrix is computed
        more information on rotation matrix can be found online:
        https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],%20float[],%20float[],%20float[])
    */

    //Get the latest values from the sensors
    //from kotlin tuto : https://www.raywenderlich.com/10838302-sensors-tutorial-for-android-getting-started
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //Copy the events captured values
            System.arraycopy(event.values, 0, accelerometerData, 0, accelerometerData.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            //Copy the events captured values
            System.arraycopy(event.values, 0, magnetometerData, 0, magnetometerData.size)
        }
        // update the matrix
        SensorManager.getRotationMatrix(opglr.swapRotMatrix(rotationMatrix),null, accelerometerData, magnetometerData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //No need to implem this for this lab, yet mandatory for the inheritance
    }

}