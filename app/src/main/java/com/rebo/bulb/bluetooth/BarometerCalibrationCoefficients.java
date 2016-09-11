package com.rebo.bulb.bluetooth;

import java.util.List;

/**
 * Created by guodunsong on 16/7/14.
 */
public enum BarometerCalibrationCoefficients {
    INSTANCE;
    volatile public List<Integer> barometerCalibrationCoefficients;
    volatile public double heightCalibration;
}