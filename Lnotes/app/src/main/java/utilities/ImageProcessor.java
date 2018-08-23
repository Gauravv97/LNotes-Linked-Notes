package utilities;

import com.links.gaurav.lnotes.Modify;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

/**
 * Created by Gaurav on 2/28/2018.
 */

public class ImageProcessor {
    public static void ProcessImage(Mat src, Modify.ScanMode mode, double contrast,int c,float multiplier) {
        OpenCVLoader.initDebug();
        double beta=-90;
        int blockSize=35;


        Scalar mod=new Scalar(-100+(contrast-1.5)*200);
        switch (mode){
            case ORIGINAL:
                src.convertTo(src,-1,contrast,beta);
                break;
            case COLOR_SCAN:
                Imgproc.resize( src, src, new Size(src.width()*multiplier,src.height()*multiplier));
                Mat mask = new Mat(src.size(), CvType.CV_8UC1);
                Imgproc.cvtColor(src,mask,Imgproc.COLOR_RGBA2GRAY);
                Mat copy = new Mat(src.size(), CvType.CV_8UC3);
                src.copyTo(copy);
                Imgproc.adaptiveThreshold(mask,mask,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,blockSize,c);
                src.setTo(new Scalar(255,255,255));
                copy.copyTo(src,mask);
                copy.release();
                mask.release();
                // special color threshold algorithm
                src.convertTo(src,-1, 1.5, beta);
                colorThresh(src,0);
                double correction=(9-(contrast-1.5)*10);
                Scalar s=new Scalar(50-correction*20,50-correction*20,50-correction*20,40+correction*20);
                //Scalar s=new Scalar(0,0,0,100+(contrast-1.5)*200);
                Core.add(src,s,src);
                Imgproc.resize( src, src, new Size(src.width()/multiplier,src.height()/multiplier) );
                break;
            case GRAYSCALE:
                Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY);
                src.convertTo(src,-1,contrast,beta);
                break;
            case BLACK_AND_WHITE1:
                Imgproc.resize( src, src, new Size(src.width()*multiplier,src.height()*multiplier));
                Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, c);
                //Imgproc.cvtColor(src,src,Imgproc.COLOR_GRAY2RGBA);
                Core.add(src,mod,src);
                //src.convertTo(src,-1, contrast, beta);
                Imgproc.resize( src, src, new Size(src.width()/multiplier,src.height()/multiplier) );
                break;
            case BLACK_AND_WHITE2:
                Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.threshold(src,src,150-(c)*5,255,Imgproc.THRESH_BINARY);
                Core.add(src,mod,src);
                break;
        }

        return ;
    }
    private static void colorThresh(Mat src, int threshold) {

        int channels=src.channels();
        int size = (int) (src.total())*channels;
        byte[] d = new byte[size];
        src.get(0,0,d);

        for (int i=0; i < size-3; i+=channels) {

            // the "& 0xff" operations are needed to convert the signed byte to double

            // avoid unneeded work
            if ( (double) (d[i] & 0xff) == 255 ) {
                continue;
            }

            double max = Math.max(Math.max((double) (d[i] & 0xff), (double) (d[i + 1] & 0xff)),
                    (double) (d[i + 2] & 0xff));
            double mean = ((double) (d[i] & 0xff) + (double) (d[i + 1] & 0xff)
                    + (double) (d[i + 2] & 0xff)) / 3;

            if (max > threshold && mean < max * 0.9) {
                d[i] = (byte) ((double) (d[i] & 0xff) * 255 / max);
                d[i + 1] = (byte) ((double) (d[i + 1] & 0xff) * 255 / max);
                d[i + 2] = (byte) ((double) (d[i + 2] & 0xff) * 255 / max);
            } else {
                d[i] = d[i + 1] = d[i + 2] = 0;
            }
        }
        src.put(0,0,d);
    }
}
