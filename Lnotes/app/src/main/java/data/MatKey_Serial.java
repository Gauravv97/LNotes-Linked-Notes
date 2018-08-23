package data;

import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;

import java.io.Serializable;

/**
 * Created by Gaurav on 12/15/2017.
 */

public class MatKey_Serial implements Serializable {
    private float x[];
    private float y[];
    private float size[];
    private float angle[];
    private int octave[];
    private int length;
    private int class_id[];
    private float response[];

    public MatKey_Serial(KeyPoint[] keyPoints) {
        this.length=keyPoints.length;
        x=new float[length];
        y=new float[length];
        size=new float[length];
        angle=new float[length];
        response=new float[length];
        octave=new int[length];
        class_id=new int[length];
        int i=0;
        for (KeyPoint keyPoint : keyPoints) {
            x[i]=(float)keyPoint.pt.x;
            y[i]=(float)keyPoint.pt.y;
            size[i]=keyPoint.size;
            angle[i]=keyPoint.angle;
            octave[i]=keyPoint.octave;
            class_id[i]=keyPoint.class_id;
            response[i]=keyPoint.response;
            i++;
        }

    }
    public KeyPoint[] deserialize() {
        KeyPoint keyPoints[]=new KeyPoint[length];
        for(int i=0;i<length;i++)
        {
            keyPoints[i]=new KeyPoint(x[i],y[i],size[i],angle[i],response[i],octave[i],class_id[i]);
        }
        return (keyPoints);

    }


}
