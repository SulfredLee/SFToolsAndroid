package com.example.mobliescanner;

import android.graphics.Path;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by SulfredLee on 8/31/2016.
 */
public class PhotoScanner {
    public ArrayList<String> m_Photos;
    public String m_outputPath;
    public int m_shiftValue;

    private double m_ratio;
    private double m_invRatio;

    public PhotoScanner(){}

    public void StartScanning()
    {
        for(String photoPath : m_Photos)
        {
            String photoName = photoPath.substring(photoPath.lastIndexOf("/") + 1);
            Mat rawPhoto = Imgcodecs.imread(photoPath);

            Mat convertedPhoto = new Mat();
            ScanningAlgo(rawPhoto, convertedPhoto, photoName);

            String outputFileName = m_outputPath + "/clean_" + photoName;
            Imgcodecs.imwrite(outputFileName, convertedPhoto);
        }
    }

    private void ScanningAlgo(Mat rawPhoto, Mat outPhoto, String photoName)
    {
        GetRaio(rawPhoto);
        Mat interMediatePhoto = new Mat();
        org.opencv.core.Size sz = new Size(rawPhoto.size().width * m_invRatio, rawPhoto.size().height * m_invRatio);
        Imgproc.resize(rawPhoto, interMediatePhoto, sz);

        Imgproc.cvtColor(interMediatePhoto, interMediatePhoto, Imgproc.COLOR_BGR2GRAY);
        org.opencv.core.Size gaussianKernalSize = new Size(5,5);
        Imgproc.GaussianBlur(interMediatePhoto, interMediatePhoto, gaussianKernalSize, 0);



        int temptemp = 20;
        Mat edged = new Mat();
        Imgproc.Canny(interMediatePhoto, edged, temptemp, temptemp * 2);

        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edged, cnts, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Collections.sort(cnts, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint first, MatOfPoint second)
            {
                double firstArea, secondArea;
                firstArea = Imgproc.contourArea(first);
                secondArea = Imgproc.contourArea(second);

                if (firstArea < secondArea)
                    return 1;           // Neither val is NaN, thisVal is smaller
                if (firstArea > secondArea)
                    return -1;            // Neither val is NaN, thisVal is larger

                long thisBits = Double.doubleToLongBits(firstArea);
                long anotherBits = Double.doubleToLongBits(secondArea);

                return (thisBits == anotherBits ?  0 : // Values are equal
                       (thisBits < anotherBits ? 1 : // (-0.0, 0.0) or (!NaN, NaN)
                       -1));                          // (0.0, -0.0) or (NaN, !NaN)
//                if(Imgproc.contourArea(first) > Imgproc.contourArea(second))
//                    return 1;
//                else
//                    return -1;
            }
        });

        MatOfPoint2f biggestSquare = new MatOfPoint2f();
        for(MatOfPoint point : cnts)
        {
            MatOfPoint2f  newPoint = new MatOfPoint2f( point.toArray() );
            double peri = Imgproc.arcLength(newPoint, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(newPoint, approx, 0.02 * peri, true);

            if(approx.toList().size() == 4)
            {
                biggestSquare = approx;
                break;
            }
        }

        Mat warped = new Mat();
        four_point_transform(rawPhoto, warped, biggestSquare, m_ratio);

//        Imgcodecs.imwrite(m_outputPath + "/inter_" + photoName, warped);

        Imgproc.cvtColor(warped, warped, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(warped, warped, 250, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, m_shiftValue);
        warped.copyTo(outPhoto);
    }

    private void GetRaio(Mat inPhoto)
    {
        double newHeight = 500.0;
        m_ratio = inPhoto.size().height / newHeight;
        m_invRatio = 1 / m_ratio;
    }

    private void four_point_transform(Mat inImage, Mat outImage, MatOfPoint2f pts, double ratio)
    {
        org.opencv.core.Point TL, TR, BR, BL;
        TL = new Point();
        TR = new Point();
        BR = new Point();
        BL = new Point();

        order_points(pts, TL, TR, BR, BL);
        TL.x *= ratio; TL.y *= ratio;
        TR.x *= ratio; TR.y *= ratio;
        BR.x *= ratio; BR.y *= ratio;
        BL.x *= ratio; BL.y *= ratio;

        double widthA = Math.sqrt((BR.x - BL.x)*(BR.x - BL.x) + (BR.y - BL.y)*(BR.y - BL.y));
        double widthB = Math.sqrt((TR.x - TL.x)*(TR.x - TL.x) + (TR.y - TL.y)*(TR.y - TL.y));
        int maxWidth = Math.max((int)widthA, (int)widthB);

        double heightA = Math.sqrt((TR.x - BR.x)*(TR.x - BR.x) + (TR.y - BR.y)*(TR.y - BR.y));
        double heightB = Math.sqrt((TL.x - BL.x)*(TL.x - BL.x) + (TL.y - BL.y)*(TL.y - BL.y));
        int maxHeight = Math.max((int)(heightA), (int)(heightB));

        org.opencv.core.Point dstTL, dstTR, dstBR, dstBL;
        dstTL = new Point();
        dstTR = new Point();
        dstBR = new Point();
        dstBL = new Point();
        dstTL.x = 0; dstTL.y = 0;
        dstTR.x = maxWidth - 1; dstTR.y = 0;
        dstBR.x = maxWidth - 1; dstBR.y = maxHeight - 1;
        dstBL.x = 0; dstBL.y = maxHeight - 1;

        List<Point> orgAry = new ArrayList<Point>();
        List<Point> dstAry = new ArrayList<Point>();


        orgAry.add(TL); orgAry.add(TR); orgAry.add(BR); orgAry.add(BL);
        dstAry.add(dstTL); dstAry.add(dstTR); dstAry.add(dstBR); dstAry.add(dstBL);
//        Mat orgMat = Converters.vector_Point_to_Mat(orgAry);
//        Mat dstMat = Converters.vector_Point_to_Mat(dstAry);
        Mat orgMat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dstMat = new Mat(4, 1, CvType.CV_32FC2);
        orgMat.put(0, 0, TL.x, TL.y, TR.x, TR.y, BR.x, BR.y, BL.x, BL.y);
        dstMat.put(0, 0, dstTL.x, dstTL.y, dstTR.x, dstTR.y, dstBR.x, dstBR.y, dstBL.x, dstBL.y);

        Mat M = Imgproc.getPerspectiveTransform(orgMat, dstMat);
        org.opencv.core.Size maxSize = new Size();
        maxSize.width = maxWidth; maxSize.height = maxHeight;
        Imgproc.warpPerspective(inImage, outImage, M, maxSize);
    }

    private void order_points(MatOfPoint2f pts, Point TL, Point TR, Point BR, Point BL)
    {
        FindTopLeft(pts);
        FindBottomRight(pts);
        FindTopRight(pts);

        Point[] tempArray = pts.toArray();
        TL.x = tempArray[0].x; TL.y = tempArray[0].y;
        TR.x = tempArray[1].x; TR.y = tempArray[1].y;
        BR.x = tempArray[2].x; BR.y = tempArray[2].y;
        BL.x = tempArray[3].x; BL.y = tempArray[3].y;
    }

    private void FindTopLeft(MatOfPoint2f pts)
    {
        Point[] tempArray = pts.toArray();
        double sum = tempArray[0].x + tempArray[0].y;
        int minIndex = 0;
        for(int i = 1; i < 4; i++)
        {
            if (sum > tempArray[i].x + tempArray[i].y)
            {
                sum = tempArray[i].x + tempArray[i].y;
                minIndex = i;
            }
        }
        Point tempPoint = tempArray[minIndex];
        tempArray[minIndex] = tempArray[0];
        tempArray[0] = tempPoint;

        pts.empty();
        pts.fromArray(tempArray);
    }

    private void FindBottomRight(MatOfPoint2f pts)
    {
        Point[] tempArray = pts.toArray();
        double sum = tempArray[0].x + tempArray[0].y;
        int maxIndex = 0;
        for(int i = 1; i < 4; i++)
        {
            if (sum < tempArray[i].x + tempArray[i].y)
            {
                sum = tempArray[i].x + tempArray[i].y;
                maxIndex = i;
            }
        }
        Point tempPoint = tempArray[maxIndex];
        tempArray[maxIndex] = tempArray[2];
        tempArray[2] = tempPoint;

        pts.empty();
        pts.fromArray(tempArray);
    }
    private void FindTopRight(MatOfPoint2f pts)
    {
        Point[] tempArray = pts.toArray();
        if(tempArray[1].y > tempArray[3].y)
        {
            Point tempPoint = tempArray[1];
            tempArray[1] = tempArray[3];
            tempArray[3] = tempPoint;
        }
        pts.empty();
        pts.fromArray(tempArray);
    }
}
