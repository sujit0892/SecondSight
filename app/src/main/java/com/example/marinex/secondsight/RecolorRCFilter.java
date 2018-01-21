package com.example.marinex.secondsight;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by marinex on 1/21/18.
 */

public class RecolorRCFilter implements Filter {
    private final ArrayList<Mat> mChannels = new ArrayList<Mat>(4);
    @Override
    public void apply(Mat src, Mat dst) {
        Core.split(src, mChannels);
        final Mat g = mChannels.get(1);
        final Mat b = mChannels.get(2);
        Core.addWeighted(g,0.5,b,0.5,0.0,g);
        mChannels.set(2,g);
        Core.merge(mChannels, dst);
    }
}
