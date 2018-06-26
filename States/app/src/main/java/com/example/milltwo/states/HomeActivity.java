package com.example.milltwo.states;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final String PIR_PIN_NAME = "BCM27";
    private static final String BUTTON_PIN_NAME = "BCM17";
    private Gpio mButtonGpio; // GPIO connection to button input

    Timer timer;
    Context context = this;
    MediaPlayer mp_main;
    MediaPlayer mp_background;
    AudioManager audioManager;
    int newVolume;
    int state = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Main thread interrupted!");
        }

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        mp_main = MediaPlayer.create(context, R.raw.yazz); // find a way to flip through different audio files
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        newVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        try {
            mButtonGpio = pioManager.openGpio(BUTTON_PIN_NAME); // Create GPIO connection.

            // Configure as an input, trigger events on every 'down' press
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW); // Value is true when the pin is LOW
            mButtonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }

        try {
            mButtonGpio = pioManager.openGpio(PIR_PIN_NAME); // Create GPIO connection.

            // Configure as an input, trigger events on every 'down' press
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH); // Value is true when the pin is LOW
            mButtonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            if (gpio.getName() == "BCM27") {
                Log.i(TAG, "Button 27 has been pressed! (Trigger State 4 - Play Interview)");
                if (state == 2) {
                    state = 4; // Bump to 'Playing Interview' State
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_SHOW_UI);
                    mp_main.start();
                } else if (state == 4) {
                    if (timer != null) {
                        Log.i(TAG, "Timer cancelled early!");
                        timer.cancel();
                    }
                } else if (state == 3) {
                    state = 4; // Bump back to 'Playing Interview' State
                    mp_main.start();
                    // Fade out background music and fade interview back in
                }
                Reminder(15); // start timer for however long; if movement before however long, restart timer
            } else if (gpio.getName() == "BCM17") {
                Log.i(TAG, "Button 17 has been pressed! (Trigger State 2 - Attracting)");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Main thread interrupted!");
                }
                if (state == 1) {
                    // Play background music and lights
                    Log.i(TAG, "Cue background music and lights!");
                    state = 2;
                } // else do nothing
            }
            return true;
        }

        public void Reminder (int seconds) {
            timer = new Timer();
            timer.schedule(new RemindTask(), seconds*1000);
        }

        class RemindTask extends TimerTask {
            public void run() {
                timer.cancel(); // Terminate the timer thread
                Log.i(TAG, "No movement, timer cancelled! Back to Active State!");
                state = 3; // bump back to Active State 3
                mp_main.pause();
                // Fade out interview and fade in background music for Active State 3
            }
        }
    };
}
