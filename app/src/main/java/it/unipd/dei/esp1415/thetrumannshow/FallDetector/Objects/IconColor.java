package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects;


import android.graphics.Color;

/**
 * @author Enrico Naletto
 */

public class IconColor {
    public static int getRandomColor() {
        int randomRedVal = (int) Math.round(Math.random() * 256);
        int randomGreenVal = (int) Math.round(Math.random() * 256);
        int randomBlueVal = (int) Math.round(Math.random() * 256);

        return Color.rgb(randomRedVal, randomGreenVal, randomBlueVal);
    }
}
