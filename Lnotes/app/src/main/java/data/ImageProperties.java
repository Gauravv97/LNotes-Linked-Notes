package data;

import com.links.gaurav.lnotes.Modify;

import org.opencv.core.Point;

import java.io.Serializable;

/**
 * Created by Gaurav on 10/30/2017.
 */

public class ImageProperties implements Serializable{
    private String Original_Image;
    private Double pointx[]=new Double[4]
            ,pointy[]=new Double[4];
    private double contrast=2;
    private int details=9;
    private float rotation=0;
    Modify.ScanMode scanMode;

    public double getContrast() {
        return contrast;
    }

    public void setContrast(double contrast) {
        this.contrast = contrast;
    }

    public int getDetails() {
        return details;
    }

    public void setDetails(int details) {
        this.details = details;
    }

    public Modify.ScanMode getScanMode() {
        return scanMode;
    }

    public void setScanMode(Modify.ScanMode scanMode) {
        this.scanMode = scanMode;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }



    public ImageProperties(String original_Image, Point[] points, Modify.ScanMode scanMode) {
        Original_Image = original_Image;
        this.scanMode=scanMode;
        for(int i=0;i<4;i++)
        {
            pointx[i]=points[i].x;
            pointy[i]=points[i].y;
        }


    }

    public void setOriginal_Image(String original_Image) {
        Original_Image = original_Image;
    }

    public void setPoints(Point[] points) {

        for(int i=0;i<4;i++)
        {
            pointx[i]=points[i].x;
            pointy[i]=points[i].y;
        }
    }



    public String getOriginal_Image() {

        return Original_Image;
    }
    public Point[] getPoints() {
        Point[] points={new Point(pointx[0],pointy[0]),new Point(pointx[1],pointy[1]),new Point(pointx[2],pointy[2]),new Point(pointx[3],pointy[3])};
        return points;

    }
    public void rotatePoints(int height,int width){
        double x=pointx[0],y=pointy[0];
        /*pointx[0]=pointx[2];
        pointy[0]=pointy[2];
        pointx[2]=pointx[3];
        pointy[2]=pointy[3];
        pointx[3]=pointx[1];
        pointy[3]=pointy[1];
        pointx[1]=x;
        pointy[1]=y;
        for (int i=0;i<4;i++){
            x=pointx[i];
            pointx[i]=height-pointy[i];
            pointy[i]=x;
        }*/
        pointx[0]=pointx[1];
        pointy[0]=pointy[1];
        pointx[1]=pointx[3];
        pointy[1]=pointy[3];
        pointx[3]=pointx[2];
        pointy[3]=pointy[2];
        pointx[2]=x;
        pointy[2]=y;
        for (int i=0;i<4;i++){
            y=pointy[i];
            pointy[i]=width-pointx[i];
            pointx[i]=y;
        }

    }
}
