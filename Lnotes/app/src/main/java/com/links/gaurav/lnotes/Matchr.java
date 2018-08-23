package com.links.gaurav.lnotes;

import android.content.Context;
import android.util.Log;
import android.support.v4.util.Pair;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

import data.entry;

/**
 * Created by Gaurav on 6/21/2017.
 */

public class Matchr {


    final DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    int mt = -2;
    int mx = 0;
    int selected;
    FeatureDetector featureDetector;
    DescriptorExtractor descriptorExtractor;
    Mat objectImage;
    Mat sceneImage;
    MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
    MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
    MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
    MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();

    Context context;
    static final String ymlParamsModified = "%YAML:1.0\n---\n"
            + "format: 3\n"
            + "indexParams:\n"
            + "   -\n"
            + "      name: algorithm\n"
            + "      type: 23\n"
            + "      value: 6\n"// this line is changed!
            + "   -\n"
            + "      name: trees\n"
            + "      type: 4\n"
            + "      value: 4\n"
            + "searchParams:\n"
            + "   -\n"
            + "      name: checks\n"
            + "      type: 4\n"
            + "      value: 16\n"
            + "   -\n"
            + "      name: eps\n"
            + "      type: 5\n"
            + "      value: 4.\n"// this line is changed!
            + "   -\n"
            + "      name: sorted\n"
            + "      type: 15\n"
            + "      value: 1\n";

    public Matchr() {
    }

    public Pair<Pair<Integer, Integer>,Integer>[] compare(entry scene[], int n, String object, Context MyContext) {
        OpenCVLoader.initDebug();
        //tost(MyContext,"Started");
        //For FlannBasedMatcher  if it gets better..
        /*try {
            File outputF = File.createTempFile("FlannfDetectorParams", ".YAML",MyContext.getCacheDir() );
            FileOutputStream outputStream=new FileOutputStream(outputF);

            outputStream.write(ymlParamsModified.getBytes());
            outputStream.flush();
            outputStream.close();
            //matcher.read(outputF.getPath());
            outputF.delete();


        }catch (IOException i){
            Log.e("lnote","error",i);
        }*/

        List<Pair<Pair<Integer, Integer>,Integer>> SortedMatches = new ArrayList<>();
        featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        objectImage = Imgcodecs.imread(object, Imgcodecs.IMREAD_GRAYSCALE);
        featureDetector.detect(objectImage, objectKeyPoints);
        featureDetector.detect(objectImage, objectKeyPoints);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
        for (entry current : scene) {
            for (int i = 0; i < current.get_im().length; i++) {
                if (current.HasMarker()[i]) {
                    if (current.get_KeyPoints()[i] == null) {
                        try {
                            sceneImage = Imgcodecs.imread(current.get_mr()[i], Imgcodecs.IMREAD_GRAYSCALE);
                            featureDetector.detect(sceneImage, sceneKeyPoints);
                            descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
                            sceneDescriptors.convertTo(sceneDescriptors, CvType.CV_32F);
                            Dbhandler dbhandler = new Dbhandler(MyContext, null, null, 1);
                            File file = File.createTempFile((new File(current.get_mr()[i])).getName().replaceFirst("[.][^.]+$", ""), ".KP", MyContext.getExternalFilesDir("Pictures"));
                            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                            float[] data = new float[(int) sceneDescriptors.total() * sceneDescriptors.channels()];
                            sceneDescriptors.get(0, 0, data);
                            ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);
                            for (float d : data) {
                                buffer.putFloat(d);
                            }
                            byte[] byteArray = buffer.array();
                            fos.write(byteArray);
                            dbhandler.setMarker(current.get_im()[i], current.get_mr()[i], file.getAbsolutePath(), sceneDescriptors.rows(), sceneDescriptors.cols());
                            fos.close();
                        } catch (Exception e) {
                            Log.e("lnote", "error", e);

                        }
                    } else {
                        File file = new File(current.get_KeyPoints()[i]);
                        try {
                            long l1 = System.nanoTime();
                            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                            byte[] bytes = new byte[(int) file.length()];
                            fis.read(bytes);
                            ByteBuffer buffer = ByteBuffer.wrap(bytes);
                            FloatBuffer floatBuffer = buffer.asFloatBuffer();
                            float[] floatArray = new float[floatBuffer.limit()];
                            floatBuffer.get(floatArray);
                            sceneDescriptors.create(current.get_KPRows()[i], current.get_KPCols()[i], CvType.CV_32F);
                            sceneDescriptors.put(0, 0, floatArray);

                            //Toast.makeText(MyContext,"file :"+sceneDescriptors.toArray()[0].pt.x+" "+sceneDescriptors.toArray()[0].pt.y,Toast.LENGTH_LONG).show();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    MatOfDMatch matches = new MatOfDMatch();
                    sceneDescriptors.convertTo(sceneDescriptors, CvType.CV_8U);
                    matcher.match(objectDescriptors, sceneDescriptors, matches);
                    List<DMatch> matches_original = matches.toList();
                    List<DMatch> goodMatchesList = new ArrayList<DMatch>();

                    int DIST_LIMIT = 30;
                    // Check all the matches distance and if it passes add to list of filtered matches

                    for (int j = 0; j < matches_original.size(); j++) {
                        DMatch d = matches_original.get(j);
                        if (Math.abs(d.distance) <= DIST_LIMIT) {
                            goodMatchesList.add(d);
                        }
                    }
                    //tost(MyContext,current.get_id()+" "+i);

                    if (goodMatchesList.size() >= 7) {
                        SortedMatches.add(new Pair<Pair<Integer, Integer>,Integer>( new Pair<Integer, Integer>(current.get_id(),i),goodMatchesList.size()));
                    }
                }
            }
        }
        //tost(MyContext,""+SortedMatches.size());
        Collections.sort(SortedMatches, new Comparator<Pair<Pair<Integer, Integer>,Integer>>() {
            @Override
            public int compare(Pair<Pair<Integer, Integer>,Integer> integerIntegerPair,Pair<Pair<Integer, Integer>,Integer> t1) {
                return Integer.compare(t1.second,integerIntegerPair.second);
            }
        });
        Pair<Pair<Integer, Integer>,Integer>[] pairs;
        pairs=SortedMatches.toArray(new Pair[SortedMatches.toArray().length]);

        return pairs;

    }
    void tost(Context x,String s){
        Toast.makeText(x,s,Toast.LENGTH_SHORT).show();
    }
}
       /* for (int j=0;j<n;j++)
        {
            if(scene[j].HasMarker()) {
                if (scene[j].get_KeyPoints() == null) {
                    try {
                        sceneImage = Imgcodecs.imread(scene[j].get_mr(), Imgcodecs.IMREAD_GRAYSCALE);
                        featureDetector.detect(sceneImage, sceneKeyPoints);
                        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
                        sceneDescriptors.convertTo(sceneDescriptors, CvType.CV_32F);
                        Dbhandler dbhandler = new Dbhandler(MyContext, null, null, 1);
                        File file = File.createTempFile((new File(scene[j].get_mr())).getName().replaceFirst("[.][^.]+$", ""), ".dat", MyContext.getExternalFilesDir("Pictures"));
                        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                        float[] data = new float[(int) sceneDescriptors.total() * sceneDescriptors.channels()];
                        sceneDescriptors.get(0, 0, data);
                        ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);
                        for (int i = 0; i < data.length; i++) {
                            buffer.putFloat(data[i]);
                        }
                        byte[] byteArray = buffer.array();
                        fos.write(byteArray);
                        //Toast.makeText(MyContext,"file :"+scene[j].get_KeyPoints(),Toast.LENGTH_LONG).show();
                        dbhandler.setMarker(scene[j].get_im());
                        //dbhandler.set_marker_KeyPoints(scene[j].get_mr(), file.getAbsolutePath(), sceneDescriptors.rows(), sceneDescriptors.cols());
                        //Toast.makeText(MyContext,"file :"+dbhandler.getresult(j).get_KeyPoints(),Toast.LENGTH_LONG).show();
                        fos.close();
                    *//*buffer.clear();
                    l1=System.nanoTime();
                    FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                    byte[] bytes=new byte[(int)file.length()];
                    fis.read(bytes);
                    buffer=ByteBuffer.wrap(bytes);
                    FloatBuffer floatBuffer = buffer.asFloatBuffer();
                    float[] floatArray = new float[floatBuffer.limit()];
                    floatBuffer.get(floatArray);
                    sceneDescriptors.create(sceneDescriptors.rows(),sceneDescriptors.cols(),CvType.CV_32F);
                    sceneDescriptors.put(0,0,data);
                    Toast.makeText(MyContext,"file :"+sceneDescriptors.toArray()[0].pt.x+" "+sceneDescriptors.toArray()[0].pt.y,Toast.LENGTH_LONG).show();


*//*


                    } catch (Exception e) {
                        Log.e("lnote", "error", e);

                    }
            *///if using flann convert to cv_32f
            /*List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();

            matcher.knnMatch(objectDescriptors, sceneDescriptors,matches,2);
            LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

            float nndrRatio = 0.7f;

            for (int i = 0; i < matches.size(); i++) {
                MatOfDMatch matofDMatch = matches.get(i);
                DMatch[] dmatcharray = matofDMatch.toArray();
                DMatch m1 = dmatcharray[0];
                DMatch m2 = dmatcharray[1];


                if (m1.distance <= m2.distance * nndrRatio) {
                    goodMatchesList.addLast(m1);

                }

            }
            if(sceneDescriptors.equals(sceneDescriptors2))
                Toast.makeText(MyContext,sceneDescriptors.total()+" "+sceneDescriptors2.total(),Toast.LENGTH_SHORT).show();
            Toast.makeText(MyContext,"Notequal"+sceneDescriptors.total()+" "+sceneDescriptors.isContinuous()+" "+sceneDescriptors2.total()+" "+sceneDescriptors2.isContinuous(),Toast.LENGTH_SHORT).show();


                } else {
                    File file = new File(scene[j].get_KeyPoints());
                    try {
                        long l1 = System.nanoTime();
                        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                        byte[] bytes = new byte[(int) file.length()];
                        fis.read(bytes);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        FloatBuffer floatBuffer = buffer.asFloatBuffer();
                        float[] floatArray = new float[floatBuffer.limit()];
                        floatBuffer.get(floatArray);
                        sceneDescriptors.create(scene[j].get_KPRows(), scene[j].get_KPCols(), CvType.CV_32F);
                        sceneDescriptors.put(0, 0, floatArray);

                        //Toast.makeText(MyContext,"file :"+sceneDescriptors.toArray()[0].pt.x+" "+sceneDescriptors.toArray()[0].pt.y,Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                MatOfDMatch matches = new MatOfDMatch();
                sceneDescriptors.convertTo(sceneDescriptors, CvType.CV_8U);
                matcher.match(objectDescriptors, sceneDescriptors, matches);
                List<DMatch> matches_original = matches.toList();
                List<DMatch> goodMatchesList = new ArrayList<DMatch>();

                int DIST_LIMIT = 30;
                // Check all the matches distance and if it passes add to list of filtered matches

                for (int i = 0; i < matches_original.size(); i++) {
                    DMatch d = matches_original.get(i);
                    if (Math.abs(d.distance) <= DIST_LIMIT) {
                        goodMatchesList.add(d);
                    }
                }

                if (goodMatchesList.size() >= 7) {
                    SortedMatches.add(new Pair<Integer, Integer>(j,goodMatchesList.size()));
                    if (mx < goodMatchesList.size()) {
                        mx = goodMatchesList.size();
                        mt = j;
                    }
                }
            }
        }
        Collections.sort(SortedMatches, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> integerIntegerPair, Pair<Integer, Integer> t1) {
                return Integer.compare(t1.second,integerIntegerPair.second);
            }
        });
        Pair<Integer,Integer>[] pairs;
        pairs=SortedMatches.toArray(new Pair[SortedMatches.toArray().length]);
        return pairs;
    }
}*/
