package com.mytech.lab.musicplayer.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Created by lnx on 8/3/18.
 */

class AccelerometerManager {


    private val now: Long = 0
    private val timeDiff: Long = 0
    private val lastUpdate: Long = 0
    private val lastShake: Long = 0

    private val x = 0f
    private val y = 0f
    private val z = 0f
    private val lastX = 0f
    private val lastY = 0f
    private val lastZ = 0f
    private val force = 0f


    interface OnShakeListener {
        fun onShake()
    }

    companion object {

        private var context: Context? = null

        private var threshold = 30.0f
        private var interval = 1000

        private var sensor: Sensor? = null
        private var sensorManager: SensorManager? = null

        private var supported: Boolean? = null

        var isListening = false
            private set

        fun stopListening() {

            isListening = false
            try {
                if (sensorManager != null && sensorEventListener != null) {
                    sensorManager!!.unregisterListener(sensorEventListener)
                }
            } catch (e: Exception) {
            }

        }


        private var mListener: OnShakeListener? = null

        private val mShakeTimestamp: Long = 0

        fun isSupported(cntxt: Context): Boolean {
            context = cntxt
            if (supported == null) {
                if (context != null) {

                    sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager

                    // Get all sensors in device
                    val sensors = sensorManager!!.getSensorList(
                            Sensor.TYPE_ACCELEROMETER)

                    supported = sensors.size > 0


                } else {
                    supported = java.lang.Boolean.FALSE
                }
            }
            return supported!!
        }


        fun configure(threshold: Int, interval: Int) {
            Companion.threshold = threshold.toFloat()
            Companion.interval = interval
        }

        fun startListening(listener: OnShakeListener?) {

            sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            val sensors = sensorManager!!.getSensorList(
                    Sensor.TYPE_ACCELEROMETER)

            if (sensors.size > 0) {

                sensor = sensors[0]

                isListening = sensorManager!!.registerListener(
                        sensorEventListener, sensor,
                        SensorManager.SENSOR_DELAY_GAME)
            }

            mListener = listener
        }

        fun startListening(threshold: Int, interval: Int) {
            configure(threshold, interval)
            startListening(mListener)
        }

        private val sensorEventListener = object : SensorEventListener {

            private var now: Long = 0
            private var timeDiff: Long = 0
            private var lastUpdate: Long = 0
            private var lastShake: Long = 0

            private var x = 0f
            private var y = 0f
            private var z = 0f
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private var force = 0f

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {

                now = event.timestamp

                x = event.values[0]
                y = event.values[1]
                z = event.values[2]

                if (lastUpdate == 0L) {
                    lastUpdate = now
                    lastShake = now
                    lastX = x
                    lastY = y
                    lastZ = z


                } else {
                    timeDiff = now - lastUpdate

                    if (timeDiff > 0) {

                        force = Math.abs(x + y + z - lastX - lastY - lastZ)

                        if (java.lang.Float.compare(force, threshold) > 0) {

                            if (now - lastShake >= interval) {

                                mListener!!.onShake()
                            } else {

                            }
                            lastShake = now
                        }
                        lastX = x
                        lastY = y
                        lastZ = z
                        lastUpdate = now
                    } else {

                    }
                }

            }
        }
    }


}
