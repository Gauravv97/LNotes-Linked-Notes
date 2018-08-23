package utilities;

import android.graphics.PointF;

import org.opencv.core.Point;

import java.util.Map;

/**
 * Created by Gaurav on 11/18/2017.
 */

public class PointMapFunctions {
    public static Point[] SetPointToRatio(Point[] point, double ratio){

        point[0].x*=ratio;
        point[0].y*=ratio;
        point[1].x*=ratio;
        point[1].y*=ratio;
        point[2].x*=ratio;
        point[2].y*=ratio;
        point[3].x*=ratio;
        point[3].y*=ratio;
        //printpoint(point);
        return point;
    }
    public static Point[] MapToPoint(Map<Integer,PointF> pointMap){
        Point[] p={new Point(pointMap.get(0).x,pointMap.get(0).y),new Point(pointMap.get(1).x,pointMap.get(1).y),
                new Point(pointMap.get(2).x,pointMap.get(2).y),new Point(pointMap.get(3).x,pointMap.get(3).y)
        };

        return p;

    }
}
