package com.example.marinex.secondsight;

import org.opencv.core.Mat;

/**
 * Created by marinex on 1/21/18.
 */

public interface Filter {

    public abstract void apply(final Mat src, final Mat dst);

}
