package exiu.iceteaclient.util;

import java.util.concurrent.ThreadLocalRandom;

public class Delay {
    private static final double SIGMA = 0.15;

    public static long getDelay(double mean, long min, long max) {
        double mu = Math.log(mean) - (SIGMA * SIGMA) / 2.0;
        double sample = Math.exp(mu + SIGMA * ThreadLocalRandom.current().nextGaussian());
        return Math.max(min, Math.min(max, Math.round(sample)));
    }
}