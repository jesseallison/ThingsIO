package com.example.milltwo.take2;

import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;

import android.media.MediaPlayer;
import android.content.Context;
import android.view.KeyEvent;
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
    private static final String BUTTON_PIN_NAME1 = "BCM21"; // Button A
    private static final String BUTTON_PIN_NAME2 = "BCM20"; // Button B
    private static final String BUTTON_PIN_NAME3 = "BCM16"; // Button C
    private static final String LED_PIN_NAME1 = "BCM6";
    private static final String LED_PIN_NAME2 = "BCM19";
    private static final String LED_PIN_NAME3 = "BCM26";

    // GPIO connection to button input
    private Gpio mButtonGpio2;
    private Gpio mButtonGpio3;
    private Gpio mLedGpio1;
    private Gpio mLedGpio2;
    private Gpio mLedGpio3;
    private ButtonInputDriver mButtonInputDriver1;

    Context context = this;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        mp = MediaPlayer.create(context, R.raw.cherokee);

        try { // Button A

            mButtonInputDriver1 = new ButtonInputDriver(BUTTON_PIN_NAME1,
                    Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE);
            mButtonInputDriver1.register();
            mLedGpio1 = pioManager.openGpio(LED_PIN_NAME1);
            // Configure as an output.
            mLedGpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }

        try { // Button B
            mButtonGpio2 = pioManager.openGpio(BUTTON_PIN_NAME2); // Create GPIO connection.

            // Configure as an input, trigger events on every change.
            mButtonGpio2.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio2.setEdgeTriggerType(Gpio.EDGE_BOTH);

            mButtonGpio2.setActiveType(Gpio.ACTIVE_LOW); // Value is true when the pin is LOW
            mButtonGpio2.registerGpioCallback(mCallback);
            mLedGpio2 = pioManager.openGpio(LED_PIN_NAME2);
            mLedGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // Configure as an output.
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }
        try { // Button C
            mButtonGpio3 = pioManager.openGpio(BUTTON_PIN_NAME3); // Create GPIO connection.

            // Configure as an input, trigger events on every change.
            mButtonGpio3.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio3.setEdgeTriggerType(Gpio.EDGE_BOTH);

            // Value is true when the pin is LOW
            mButtonGpio3.setActiveType(Gpio.ACTIVE_LOW);
            mButtonGpio3.registerGpioCallback(mCallback);
            mLedGpio3 = pioManager.openGpio(LED_PIN_NAME3);
            mLedGpio3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // Configure as an output.
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getName() == "BCM21") {
                    boolean buttonValue1 = gpio.getValue();
                    mLedGpio1.setValue(buttonValue1);
                } else if (gpio.getName() == "BCM20"){
                    boolean buttonValue2 = gpio.getValue();
                    mLedGpio2.setValue(buttonValue2);
                } else {
                    boolean buttonValue3 = gpio.getValue();
                    mLedGpio3.setValue(buttonValue3);

                    /*Speaker buzzer = RainbowHat.openPiezo(); // Play a note on the buzzer.
                    buzzer.play(440);

                    if (buttonValue3) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                     // Stop the buzzer.
                    buzzer.stop();
                    buzzer.close(); // Close the device when done.
                    mLedGpio3.setValue(false);*/
                }
            } catch (IOException e) {
                Log.w(TAG, "Error reading GPIO");
            }

            // Return true to keep callback active.
            return true;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            try {
                mLedGpio1.setValue(true); // Turn on the LED
                mp.start();
                return true;
            } catch (IOException e) {
                Log.w(TAG, "Error!", e);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            try {
                mLedGpio1.setValue(false); // Turn off the LED
                mp.pause();
                return true;
            } catch (IOException e) {
                Log.w(TAG, "Error!", e);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the button
        if (mButtonGpio3 != null) {
            mButtonGpio3.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio3.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing GPIO", e);
            }
        }
        // Unregister the driver and close
        if (mButtonInputDriver1 != null) {
            mButtonInputDriver1.unregister();
            try {
                mButtonInputDriver1.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            }
        }
    }
}
