package org.aksw.defacto.util;

import java.util.concurrent.TimeUnit;


public class TimeUtil {

    public static String formatTime(long millis) {

        return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
