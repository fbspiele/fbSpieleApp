package com.fbspiele.fbspieleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MyMapView extends MyImageView {
    final String tag = "MyImageView";

    //refpunkt 1 island oben links der eine pixel bei vestfirdir der nicht wasser ist (66.450192,-22.84793)
    double refPunkt1X; //= 606;
    double refPunkt1Y; // = 1025;
    double refPunkt1Phi; // = -22.84793;
    double refPunkt1Theta; // = 66.450192;
    //refpunkt 2 neuseeland der eine bei puponga/kaihoka (-40.536766,172.673168)
    double refPunkt2X; // = 2831;
    double refPunkt2Y; // = 2553;
    double refPunkt2Phi; // = 172.673168;
    double refPunkt2Theta; // = -40.536766;

    double refPicIntrinsicDimensionsX; // = 4953.818115234375;
    double refPicIntrinsicDimensionsY; // = 3945.818115234375;

    final double BASE_MARKER_STROKE_WIDTH = 1.72;
    final double BASE_MARKER_RADIUS = 10;

    double deltaX=0;
    double deltaY=0;
    double deltaPhi;
    //äquator berechnen
    // theta in radian
    double refPunkt1ThetaRad;
    double refPunkt2ThetaRad;
    // mercator projection
    double nonScaledRef1Y;
    double nonSCaledRef2Y;
    // skalierung der map
    double mapYscale;
    // äquator = refpunkt + nonScaled*scale (eigentlich - aber da y nach unten geht und theta nach oben gleicht sich das aus)
    double aquatorY;


    int myColor;


    void updateReferenceValues(double intrinsicPictureScaleX, double intrinsicPictureScaleY){
        refPunkt1X *= intrinsicPictureScaleX;
        refPunkt2X *= intrinsicPictureScaleX;
        refPunkt1Y *= intrinsicPictureScaleY;
        refPunkt2Y *= intrinsicPictureScaleY;
        deltaX = refPunkt2X-refPunkt1X;
        deltaY = refPunkt2Y-refPunkt1Y;
        deltaPhi = refPunkt2Phi-refPunkt1Phi;
        //äquator berechnen
        // theta in radian
        refPunkt1ThetaRad = Math.toRadians(refPunkt1Theta);
        refPunkt2ThetaRad = Math.toRadians(refPunkt2Theta);
        // mercator projection
        nonScaledRef1Y = Math.log(Math.tan((Math.PI/4)+(refPunkt1ThetaRad/2)));
        nonSCaledRef2Y = Math.log(Math.tan((Math.PI/4)+(refPunkt2ThetaRad/2)));
        // skalierung der map
        mapYscale = deltaY/(Math.abs(nonSCaledRef2Y-nonScaledRef1Y));
        // äquator = refpunkt + nonScaled*scale (eigentlich - aber da y nach unten geht und theta nach oben gleicht sich das aus)
        aquatorY = refPunkt1Y + nonScaledRef1Y*mapYscale;
    }


    public void updateRefPunkt1(double refPunkt1X, double refPunkt1Y, double refPunkt1Phi, double refPunkt1Theta){
        this.refPunkt1X = refPunkt1X;
        this.refPunkt1Y = refPunkt1Y;
        this.refPunkt1Phi = refPunkt1Phi;
        this.refPunkt1Theta = refPunkt1Theta;
    }

    public void updateRefPunkt2(double refPunkt2X, double refPunkt2Y, double refPunkt2Phi, double refPunkt2Theta){
        this.refPunkt2X = refPunkt2X;
        this.refPunkt2Y = refPunkt2Y;
        this.refPunkt2Phi = refPunkt2Phi;
        this.refPunkt2Theta = refPunkt2Theta;
    }

    public void updateIntrinsicDimensions(double refPicIntrinsicDimensionsX, double refPicIntrinsicDimensionsY){
        this.refPicIntrinsicDimensionsX = refPicIntrinsicDimensionsX;
        this.refPicIntrinsicDimensionsY = refPicIntrinsicDimensionsY;
    }


    public MyMapView(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }


    GestureDetector myMapGestureDetector;
    public MyMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        myMapGestureDetector = new GestureDetector(getContext(),new MyMapGestureListener());
        initializeMarkerList();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        myColor = sharedPref.getInt(context.getString(R.string.settings_key_color),0);
        if(myColor==0){
            Toast.makeText(context, "you havent choosen your color yet, go into settings and change it", Toast.LENGTH_LONG).show();
        }
    }

    double[] screenToKugelCoordinates(double[] screenCoords){
        return mapToKugelCoordinates(screenToPictureCoordinates(screenCoords));
    }

    double[] kugelToScreenCoordinates(double[] kugelCoords){
        return pictureToScreenCoordinates(kugelCoordsToMapCoordinates(kugelCoords));
    }

    void checkAndUpdateReferenceValues(){
        double[] intrinsicPictureDimensions = super.getIntrinsicDimensions();
        if(intrinsicPictureDimensions!=null && deltaX==0){
            double intrinsicPictureScaleX = intrinsicPictureDimensions[0]/refPicIntrinsicDimensionsX;
            double intrinsicPictureScaleY = intrinsicPictureDimensions[1]/refPicIntrinsicDimensionsY;
            Log.v(tag, "intrinsicPictureScaleX\t"+intrinsicPictureScaleX+"\t\tintrinsicPictureScaleY\t"+intrinsicPictureScaleY);
            if(intrinsicPictureScaleX/intrinsicPictureScaleY<0.99 || intrinsicPictureScaleX/intrinsicPictureScaleY>1.01){
                Log.w(tag, "intrinsicPictureScaleX != intrinsicPictureScaleY, seems that the picture was strechted in one direction");
                Toast.makeText(getContext(),"debugging warning:\nintrinsicPictureScaleX != intrinsicPictureScaleY\nseems that the picture was strechted in one direction",Toast.LENGTH_LONG).show();
            }
            updateReferenceValues(intrinsicPictureScaleX, intrinsicPictureScaleY);
        }
    }

    double[] kugelCoordsToMapCoordinates(double[] kugelCoords){
        double[] mapCoords = new double[2];
        checkAndUpdateReferenceValues();

        // x/phi is einfach linear also easy
        mapCoords[0] = (kugelCoords[0] - refPunkt1Phi)/(deltaPhi/deltaX) + refPunkt1X;

        // y/theta is a bisla schwieriger

        // erst ma checken ob theta zwischen +-90° liegt
        if(Math.abs(kugelCoords[1])>90){
            return null;
        }

        // erst ma theta -> radian -> mercator projection transformation -> map skalierung -> um null (äquator) verschieben (eigentlich + aber da y nach unten geht und theta nach oben gleicht sich das aus)

        mapCoords[1] = aquatorY - Math.log(Math.tan((Math.PI/4)+(Math.toRadians(kugelCoords[1])/2)))*mapYscale;
        //Log.v("kugel to map coords"," \nkugel x "+kugelCoords[0]+"\ty "+kugelCoords[1]+"\nmap x "+mapCoords[0]+"\ty "+mapCoords[1]);
        return mapCoords;
    }

    double[] mapToKugelCoordinates(double[] mapCoords){
        checkAndUpdateReferenceValues();
        double[] kugelCoords = new double[2];

        // x/phi is einfach linear also easy
        kugelCoords[0] = (mapCoords[0]-refPunkt1X)*(deltaPhi/deltaX) + refPunkt1Phi;

        // y/theta is a bisla schwieriger
        // verschiebung zur 0 (äquator) -> auf einheitsskalierungs (mercator skalierung) skalieren -> invertieren da y nach unten und theta nach oben zunimmt -> mercator projection transformation -> rad in deg
        kugelCoords[1] = Math.toDegrees(2*Math.atan(Math.exp(- ((mapCoords[1]-aquatorY)/mapYscale)))-(Math.PI/2));
        Log.v("map to kugel coords"," \nmap x "+mapCoords[0]+"\ty "+mapCoords[1]+"\nkugel x "+kugelCoords[0]+"\ty "+kugelCoords[1]);
        return kugelCoords;
    }

    double calcDegAngleBetweenCoords(double[] kugelCoordinates1, double[] kugelCoordinates2){
        return Math.toDegrees(calcRadAngleBetweenCoords(kugelCoordinates1, kugelCoordinates2));
    }

    double calcRadAngleBetweenCoords(double[] kugelCoordinates1, double[] kugelCoordinates2){
        //https://en.wikipedia.org/wiki/Great-circle_distance#:~:text=The%20great-circle%20distance,%20orthodromic,line%20through%20the%20sphere's%20interior).


        double[] kugelCoords1 = new double[2];
        double[] kugelCoords2 = new double[2];
        //Log.v(tag,"coords" + "\t" + kugelCoordinates1[0] + "\t" + kugelCoordinates1[1] + "\t" + kugelCoordinates2[0] + "\t" + kugelCoordinates2[1]);
        kugelCoords1[0] = Math.toRadians(kugelCoordinates1[0]);
        kugelCoords1[1] = Math.toRadians(kugelCoordinates1[1]);
        kugelCoords2[0] = Math.toRadians(kugelCoordinates2[0]);
        kugelCoords2[1] = Math.toRadians(kugelCoordinates2[1]);

        double term1 = Math.pow( Math.cos(kugelCoords2[1])*Math.sin(kugelCoords1[0]-kugelCoords2[0]), 2);
        double term2 = Math.pow( Math.cos(kugelCoords1[1])*Math.sin(kugelCoords2[1]) - Math.sin(kugelCoords1[1])*Math.cos(kugelCoords2[1])*Math.cos(kugelCoords1[0]-kugelCoords2[0]) , 2);
        double term3 = Math.sin(kugelCoords1[1])*Math.sin(kugelCoords2[1]) + Math.cos(kugelCoords1[1])*Math.cos(kugelCoords2[1])*Math.cos(kugelCoords1[0]-kugelCoords2[0]);

        double angle = Math.atan2(Math.sqrt(term1+term2), term3);

        //Log.v(tag, "angle " + Math.toDegrees(angle));

        return Math.abs(angle);
    }

    double calcDistanceBetweenCoords(double[] kugelCoordinates1, double[] kugelCoordinates2){
        double earthRadius = 6371.000785; //in km; mittlerer radius (volumengleiche kugel aus https://de.wikipedia.org/wiki/Erdradius)
        return earthRadius * calcRadAngleBetweenCoords(kugelCoordinates1, kugelCoordinates2);
    }


    float markerRadius = (float) BASE_MARKER_RADIUS * getResources().getDisplayMetrics().density;
    float strokeWidth = (float) BASE_MARKER_STROKE_WIDTH * getResources().getDisplayMetrics().density;

    double[] getGlobalCircleCoords(double[] circleMiddleCoordsDeg, double radiusDeg, double circleParameter, int positionX){
        //https://math.stackexchange.com/a/643255
        double alpha = Math.toRadians(radiusDeg);
        double beta = Math.toRadians(90-circleMiddleCoordsDeg[1]);
        //double gamma = Math.toRadians(-circleMiddleCoordsDeg[0]+180);
        double gamma = Math.toRadians(circleMiddleCoordsDeg[0]);
        // cirlceParameter = t;

        double x = (Math.sin(alpha) * Math.cos(beta) * Math.cos(gamma))*Math.cos(circleParameter)
                + (Math.sin(alpha) * Math.sin(gamma) )*Math.sin(circleParameter)
                - (Math.cos(alpha)*Math.sin(beta)*Math.cos(gamma));
        double y = - (Math.sin(alpha) * Math.cos(beta) * Math.sin(gamma))*Math.cos(circleParameter)
                + (Math.sin(alpha) * Math.cos(gamma) )*Math.sin(circleParameter)
                + (Math.cos(alpha)*Math.sin(beta)*Math.sin(gamma));
        double z = (Math.sin(alpha)*Math.sin(beta))*Math.cos(circleParameter)+Math.cos(alpha)*Math.cos(beta);
        double r = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        //Log.v(tag, "circle radius muss eigentlich 1 sein: "+r);
        double[] returnValues = new double[2];
        returnValues[0] = Math.atan2(y,x);
        returnValues[1] = Math.acos(z/r);

        double[] returnValuesDeg = new double[2];
        returnValuesDeg[0] = 180-Math.toDegrees(returnValues[0]);
        returnValuesDeg[1] = -Math.toDegrees(returnValues[1])+90;
        returnValuesDeg[0] += positionX*360;
        //Log.v("tag", "deg values "+"\t"+returnValuesDeg[0]+"\t"+returnValuesDeg[1]);
        return returnValuesDeg;
    }

    double[] getScreenCicleCoords(double[] circleMiddleCoordsDeg, double radiusDeg, double circleParamter, int positionX){
        return kugelToScreenCoordinates(getGlobalCircleCoords(circleMiddleCoordsDeg,radiusDeg,circleParamter, positionX));
    }

    Path getCirclePathAroundKugelCoord(double[] kugelCoordsCenter, double radiusDeg){
        Path path = new Path();
        // vielleicht noch verbessern(eventuell todo): bei allen 0 durchgängen ist ne lücke da der ja immer springt dort vielleicht kann ich das verbessern is aber nicht so schlimm
        // vielleicht noch verbessern(eventuell todo): die linie bei 0 anfängt (wenn die marker ziehmlich genau auf phi = 0 sin) dann ist glaube ich der sprung zum nächsten kreis der nächste und deswegen macht er trotzdem die lange linie aber auch nicht schlimm
        int corners = 512;
        double t;
        double[] tempCirlcCoords = null;
        double[] tempCirlcCoordsAlt = null;
        double distanceAlt = -1;
        double distance = -1;
        //Log.v(tag, "circle mid "+"\t"+kugelCoordsCenter[0]+"\t"+kugelCoordsCenter[1]);
        for(int k = -1; k<2; k++){
            for(int i = 0; i < corners + 1; i++){
                if(tempCirlcCoords!=null){
                    tempCirlcCoordsAlt = tempCirlcCoords;
                }
                if(distance>0){
                    distanceAlt = distance;
                }
                t = (double) i/(double)corners*2*Math.PI;
                tempCirlcCoords = ((tempCirlcCoords = getScreenCicleCoords(kugelCoordsCenter, radiusDeg, t, k)) != null) ? tempCirlcCoords : null;
                if(tempCirlcCoords != null) {
                    if(tempCirlcCoordsAlt==null) {
                        path.moveTo((float) tempCirlcCoords[0],(float) tempCirlcCoords[1]);
                    }
                    else {
                        distance = Math.sqrt(Math.pow(tempCirlcCoords[0] - tempCirlcCoordsAlt[0], 2) + Math.pow(tempCirlcCoords[1] - tempCirlcCoordsAlt[1], 2));
                        if(distanceAlt>0 && distance>0){
                            if(distance/distanceAlt<10) {
                                path.lineTo((float) tempCirlcCoords[0],(float) tempCirlcCoords[1]);
                            }
                            else {
                                path.moveTo((float) tempCirlcCoords[0],(float) tempCirlcCoords[1]);
                            }
                        }
                        else {
                            path.moveTo((float) tempCirlcCoords[0], (float) tempCirlcCoords[1]);
                        }
                    }
                }
            }
        }
        return path;
    }

    CirlcePath circlePath;
    class CirlcePath{
        Path path;
        double[] centerCoords;
        double radiusDegrees;
        CirlcePath(double[] kugelCoordsCenter, double radiusDeg){
            centerCoords = kugelCoordsCenter;
            radiusDegrees = radiusDeg;
        }

        public Path getCirclePath() {
            path = getCirclePathAroundKugelCoord(centerCoords, radiusDegrees);
            return path;
        }
    }

    Path getMarkerPath(float x, float y){
        Path markerPath = new Path();
        markerPath.moveTo(x-markerRadius,y+markerRadius);
        markerPath.lineTo(x+markerRadius,y-markerRadius);
        markerPath.moveTo(x-markerRadius,y-markerRadius);
        markerPath.lineTo(x+markerRadius,y+markerRadius);
        return markerPath;
    }

    Marker myMarker;
    Marker mySendMarker;

    public double[] getMyMarkerKugelCoords(){
        return myMarker.getKugelCoords();
    }

    class Marker{
        double[] kugelCoords;
        Paint paint;
        Path markerPath;
        int markerId = -1; // 0 = my active marker (of this phone), 1 = my send answer marker, 2 = right answer marker, 3 = nähester marker, -1 non specified

        Marker(double[] kugelcoords, int color){
            this.kugelCoords = kugelcoords;
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);
            markerPath = new Path();
        }

        Path getPath(){
            return getMarkerPath((float)kugelToScreenCoordinates(kugelCoords)[0],(float) kugelToScreenCoordinates(kugelCoords)[1]);
        }
        void setColor(int color){
            paint.setColor(color);
        }

        public double[] getKugelCoords() {
            return kugelCoords;
        }
    }

    void initializeMarkerList(){
        markerList = new ArrayList<>();
    }





    List<Marker> markerList;


    public void singleClick(MotionEvent ev){
        double[] down = {ev.getX(),ev.getY()};

        myMarker = new Marker(screenToKugelCoordinates(down), myColor);
        myMarker.markerId = 0;

        for(Marker marker:markerList){
            if(marker.markerId==0){
                markerList.remove(marker);
            }
        }
        markerList.add(myMarker);


        //circlePath = new CirlcePath(markerList.get(1).getKugelCoords(), calcDegAngleBetweenCoords(markerList.get(1).getKugelCoords(),markerList.get(2).getKugelCoords()));

        //TextView testTextView = getRootView().findViewById(R.id.testTextView);
        //try {
            //String text = calcDistanceBetweenCoords(markerList.get(0).getKugelCoords(), markerList.get(1).getKugelCoords()) + "\n" + calcDistanceBetweenCoords(markerList.get(0).getKugelCoords(), markerList.get(2).getKugelCoords()) + "\n" + calcDistanceBetweenCoords(markerList.get(1).getKugelCoords(), markerList.get(2).getKugelCoords());
            //testTextView.setText(text);
        //}
        //catch (IndexOutOfBoundsException e){
        //    e.printStackTrace();
        //}
        /*
        if(myMarker==null){
            myMarker = new Marker(screenToKugelCoordinates(down),Color.GRAY);
            markerList.add(myMarker);
        }
        else{
            myMarker.setKugelCoords(screenToKugelCoordinates(down));
        }
        */
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_UP: {
                performClick();
                break;
            }
        }
        myMapGestureDetector.onTouchEvent(ev);
        return true;
    }
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }


    private class MyMapGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            singleClick(ev);
            performClick();
            return super.onSingleTapConfirmed(ev);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        for(int i=0;i<markerList.size();i++){
            canvas.drawPath(markerList.get(i).getPath(),markerList.get(i).paint);
        }

        if(circlePath!=null){
            canvas.drawPath(circlePath.getCirclePath(), markerList.get(1).paint);
        }

    }

}
