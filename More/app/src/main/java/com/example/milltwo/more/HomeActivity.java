package com.example.milltwo.more;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;

import android.media.MediaPlayer;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.io.*;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";
    private static final String BUTTON_PIN_NAME = "BCM17";

    // GPIO connection to button input
    private Gpio mButtonGpio;
    private ButtonInputDriver mButtonInputDriver;

    Context context = this;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        mp = MediaPlayer.create(context, R.raw.ascending);

        try {
            mButtonGpio = pioManager.openGpio(BUTTON_PIN_NAME); // Create GPIO connection.

            // Configure as an input, trigger events on every change.
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH); // Value is true when the pin is LOW
            mButtonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            if (gpio.getName() == "BCM17") {
                Log.i(TAG, "Button has been pressed!");
                if (mp.isPlaying()) {
                    mp.pause();
                } else {
                    mp.start();
                }
            }
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the driver and close
        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            }
        }
    }
}