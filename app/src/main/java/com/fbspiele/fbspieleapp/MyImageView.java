package com.fbspiele.fbspieleapp;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MyImageView extends AppCompatImageView {

    //https://stackoverflow.com/a/12184210

    double scaleSpeed = 1.27;
    double drawableFolderDensity = 3.0;


    private float positionX = 0;
    private float positionY = 0;
    private float initialPositionX = 0;
    private float initialPositionY = 0;
    private float lastTouchPosX = 0;
    private float lastTouchPosY = 0;
    double[] zoomFixPunktLocal = new double[2];


    int onDoubleTapZoomCounter = 0;     //>0 = true else false
    boolean onDoubleTapZoom = false;
    boolean onPinchZoom = false;


    double baseScaleFactor = 1;

    private float gesScaleFactor = 1.f;
    float startScaleFactor = 1.f;

    int parentWidth;
    int parentHeight;

    double[] intrinsicDimensions;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector mScaleDetector;

    public MyImageView(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }

    double[] getIntrinsicDimensions(){
        return intrinsicDimensions;
    }

    MyImageView thisMyImageView;

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetector(getContext(),new MyImageViewGestureListener());

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        thisMyImageView = this;

        thisMyImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            double initialAspectRatio = 1;

            boolean layoutChanged = false;

            @Override
            public void onGlobalLayout() {
                //erst wrap parent damit ich weiß wie groß das bild ist
                //dann match parent und schauen wie groß die view jetzt ist
                //wenn aspect ratio mehr hochkant als das bild zuvor dann ist das bild vertical mittig und horizontal platzfüllend platziert
                //wenn aspect ratio mehr breitbild als das bild zuvor dann ist das bild vertical platzfüllend und horizontal mittig platziert
                if(!layoutChanged){

                    initialTopLeftCornerVector[0] = thisMyImageView.getLeft();
                    initialTopLeftCornerVector[1] = thisMyImageView.getTop();

                    initialSize[0] = thisMyImageView.getWidth();
                    initialSize[1] = thisMyImageView.getHeight();

                    initialAspectRatio = (double) initialSize[0] / (double) initialSize[1];

                    initialBottomRightCornerVector[0] = initialTopLeftCornerVector[0] + initialSize[0];
                    initialBottomRightCornerVector[1] = initialTopLeftCornerVector[1] + initialSize[1];

                    Log.v("init pic","wrap top left x " + initialTopLeftCornerVector[0]+"\twrap top left y "+initialTopLeftCornerVector[1]);
                    Log.v("init pic","wrap bot right x " + initialBottomRightCornerVector[0]+"\twrap bot right y "+initialBottomRightCornerVector[1]);

                    thisMyImageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                    layoutChanged = true;
                }
                else {
                    int[] newTopLeftCornerVector = new int[2];
                    int[] newSize = new int[2];

                    newTopLeftCornerVector[0] = thisMyImageView.getLeft();
                    newTopLeftCornerVector[1] = thisMyImageView.getTop();

                    newSize[0] = thisMyImageView.getWidth();
                    newSize[1] = thisMyImageView.getHeight();

                    double newAspectRatio = (double) newSize[0] / (double) newSize[1];


                    Log.v("init pic","match top left x " + newTopLeftCornerVector[0]+"\tmatch top left y "+newTopLeftCornerVector[1]);
                    //Log.v("init pic","match bot right x " + newBottomRightCornerVector[0]+"\tmatch bot right y "+newBottomRightCornerVector[1]);

                    Log.v("view aspect ratios","wrap "+initialAspectRatio);
                    Log.v("view aspect ratios","match "+newAspectRatio);


                    if(newAspectRatio<initialAspectRatio){
                        initialSize[0] = newSize[0];
                        initialSize[1] = (int) Math.round(((double) newSize[0])/initialAspectRatio);

                        initialTopLeftCornerVector[0] = newTopLeftCornerVector[0];
                        initialTopLeftCornerVector[1] = newTopLeftCornerVector[1] + (int) Math.round((newSize[1] - initialSize[1])/2.0);

                        initialBottomRightCornerVector[0] = initialTopLeftCornerVector[0] + initialSize[0];
                        initialBottomRightCornerVector[1] = initialTopLeftCornerVector[1] + initialSize[1];
                    }
                    else{
                        initialSize[0] = (int) Math.round(((double) newSize[1])*initialAspectRatio);
                        initialSize[1] = newSize[1];

                        initialTopLeftCornerVector[0] = newTopLeftCornerVector[0] + (int) Math.round((newSize[0] - initialSize[0])/2.0);
                        initialTopLeftCornerVector[1] = newTopLeftCornerVector[1];

                        initialBottomRightCornerVector[0] = initialTopLeftCornerVector[0] + initialSize[0];
                        initialBottomRightCornerVector[1] = initialTopLeftCornerVector[1] + initialSize[1];
                    }

                    initialVerschobenDurchMatchParent[0] = (newSize[0]-initialSize[0])/(2.0);
                    initialVerschobenDurchMatchParent[1] = (newSize[1]-initialSize[1])/(2.0);

                    Log.v("scaliertSize","x "+initialSize[0]+"\ty "+initialSize[1]);
                    Log.v("verschoben","x "+initialVerschobenDurchMatchParent[0]+"\ty "+initialVerschobenDurchMatchParent[1]);


                    //original pixel vom bild
                    intrinsicDimensions = new double[2];
                    //https://stackoverflow.com/questions/6536418/why-are-the-width-height-of-the-drawable-in-imageview-wrong

                    intrinsicDimensions[0] = thisMyImageView.getDrawable().getIntrinsicWidth() / getResources().getDisplayMetrics().density * drawableFolderDensity;
                    intrinsicDimensions[1] = thisMyImageView.getDrawable().getIntrinsicHeight() / getResources().getDisplayMetrics().density * drawableFolderDensity;
                    Log.v("intrinsic pic dim","x "+intrinsicDimensions[0]+"\ty "+intrinsicDimensions[1]);
                    double baseScaleFactorX = initialSize[0]/intrinsicDimensions[0];
                    double baseScaleFactorY = initialSize[1]/intrinsicDimensions[1];
                    if(baseScaleFactorX != baseScaleFactorY){
                        //rundungsfehler sin ok (denk ich)
                        if(Math.abs(baseScaleFactorX/baseScaleFactorY-1)>0.01){
                            Log.w("baseScaleFactor", "baseScaleFactor X ("+baseScaleFactorX+") != baseScaleFactor Y ("+baseScaleFactorY+"), ratio "+baseScaleFactorX/baseScaleFactorY+", dh anscheinend wurde das bild in x anders skaliert als in y also verzerrt?!?!?! was komisch is");
                        }
                    }
                    baseScaleFactor = (float) ((baseScaleFactorX+baseScaleFactorY)/2.0);
                    Log.v("baseScaleFactor",""+baseScaleFactor);

                    //Log.v("init pic","top left x "+initialTopLeftCornerVector[0]+"\ttop left y "+initialTopLeftCornerVector[1]);
                    parentWidth = newSize[0];
                    parentHeight = newSize[1];
                    thisMyImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }



            }
        });


    }


    double[] globalInGescalterViewToLocalCoordinates(double[] globalCoords){

        double[] newCoords;

        newCoords = globalCoords;

        newCoords[0] = newCoords[0] - positionX;
        newCoords[1] = newCoords[1] - positionY;

        newCoords[0] = newCoords[0]/gesScaleFactor;
        newCoords[1] = newCoords[1]/gesScaleFactor;
        return newCoords;
    }

    double[] localToGlobalInGescalterViewCoordinates(double[] localCoords){
        if(localCoords==null){
            return null;
        }

        double[] newCoords;

        newCoords = localCoords;

        newCoords[0] = newCoords[0]*gesScaleFactor;
        newCoords[1] = newCoords[1]*gesScaleFactor;

        newCoords[0] = newCoords[0] + positionX;
        newCoords[1] = newCoords[1] + positionY;

        return newCoords;
    }

    double[] globalToLocalCoordinates(double[] globalCoords){
        double[] newCoords = new double[2];
        newCoords[0] = globalCoords[0] - initialTopLeftCornerVector[0]*gesScaleFactor;
        newCoords[1] = globalCoords[1] - initialTopLeftCornerVector[1]*gesScaleFactor;
        newCoords = globalInGescalterViewToLocalCoordinates(newCoords);
        //Log.v("globalToLocal"," \nglobal x "+globalCoords[0]+"\ty "+globalCoords[1]+"\nlocal x "+newCoords[0]+"\ty "+newCoords[1]);
        return newCoords;
    }

    double[] localToGlobalCoordinates(double[] localCoords){
        if(localCoords==null){
            return null;
        }
        double[] newCoords = localToGlobalInGescalterViewCoordinates(localCoords);
        newCoords[0] = newCoords[0] + initialTopLeftCornerVector[0]*gesScaleFactor;
        newCoords[1] = newCoords[1] + initialTopLeftCornerVector[1]*gesScaleFactor;
        return newCoords;
    }

    double[] localToPictureCoordinates(double[] localCoords){
        double[] newCoords = new double[2];
        newCoords[0] = localCoords[0]/baseScaleFactor;
        newCoords[1] = localCoords[1]/baseScaleFactor;
        return newCoords;
    }

    double[] pictureToLocalCoordinates(double[] pictureCoords){
        if(pictureCoords==null){
            return null;
        }
        double[] newCoords = new double[2];
        newCoords[0] = pictureCoords[0]*baseScaleFactor;
        newCoords[1] = pictureCoords[1]*baseScaleFactor;
        return newCoords;
    }

    double[] screenToPictureCoordinates(double[] screenCoords){
        double[] newCoords;
        newCoords = localToPictureCoordinates(globalToLocalCoordinates(screenCoords));
        //Log.v("screenToPicture","\n screencoords x "+ screenCoords[0]+"\ty "+screenCoords[1]+"\npic x "+newCoords[0]+"\ty "+newCoords[1]);
        return newCoords;
    }

    double[] pictureToScreenCoordinates(double[] pictureCoords){
        double[] newCoords;
        newCoords = localToGlobalCoordinates(pictureToLocalCoordinates(pictureCoords));
        return newCoords;
    }


    int[] initialTopLeftCornerVector = new int[2];
    int[] initialBottomRightCornerVector = new int[2];
    int[] initialSize = new int[2];

    double[] initialVerschobenDurchMatchParent = new double[2];

    void setUpScalingValues(float scaleFixPunktGlobalX, float scaleFixPunktGlobalY){

        initialPositionX = positionX;
        initialPositionY = positionY;

        double[] zoomFixPunktGlobalInGescalterView = new double[2];
        zoomFixPunktGlobalInGescalterView[0] = (double) scaleFixPunktGlobalX - positionX*gesScaleFactor;
        zoomFixPunktGlobalInGescalterView[1] = (double) scaleFixPunktGlobalY - positionY*gesScaleFactor;
        zoomFixPunktLocal = globalInGescalterViewToLocalCoordinates(zoomFixPunktGlobalInGescalterView);

        startScaleFactor = gesScaleFactor;
        oldScaleFactor = startScaleFactor;

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        onPinchZoom = ev.getPointerCount()==2;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                lastTouchPosX = ev.getX();
                lastTouchPosY = ev.getY();

                setUpScalingValues(ev.getX(),ev.getY());

                break;
            }
            case MotionEvent.ACTION_MOVE: {


                if(!(onDoubleTapZoom||onPinchZoom)){
                    //translating

                    positionX += ev.getX() - lastTouchPosX;
                    positionY += ev.getY() - lastTouchPosY;

                    lastTouchPosX = ev.getX();
                    lastTouchPosY = ev.getY();

                    invalidate();
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                //actionIndex is der der up geht, dh ich mach den last Touch pos auf den der noch down is dann springt es nicht beim direkt danach action move
                if(ev.getActionIndex()==0){
                    lastTouchPosX = ev.getX(1);
                    lastTouchPosY = ev.getY(1);
                }
                if(ev.getActionIndex()==1){
                    lastTouchPosX = ev.getX(0);
                    lastTouchPosY = ev.getY(0);
                }

                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //wenn 2 pointer da dann pinch move also pinch zoom
                //fixpunkt ist die mitte zwischen den beiden fingern
                if(ev.getPointerCount()==2){
                    float fixPunktX = (ev.getX(0)+ev.getX(1))/2f;
                    float fixPunktY = (ev.getY(0)+ev.getY(1))/2f;
                    setUpScalingValues(fixPunktX,fixPunktY);
                }
                break;
            }
            case MotionEvent.ACTION_BUTTON_PRESS:{
                performClick();
                break;
            }
        }
        gestureDetector.onTouchEvent((ev));
        return mScaleDetector.onTouchEvent(ev);
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }

    void resetScaleTranslate(){
        positionX = 0;
        positionY = 0;
        oldScaleFactor = 1;
        gesScaleFactor = 1;
        invalidate();
    }




    private float oldScaleFactor = 1.f;
    boolean onScale = false;
    boolean singleTapDetector = false;
    public void onDraw(Canvas canvas) {
        canvas.save();

        //verschieben wegen des skalierens
        positionX += ((oldScaleFactor-gesScaleFactor)*(zoomFixPunktLocal[0]+initialPositionX));
        positionY += ((oldScaleFactor-gesScaleFactor)*(zoomFixPunktLocal[1]+initialPositionY));
        oldScaleFactor = gesScaleFactor;

        //normales verschieben
        canvas.translate(positionX, positionY);

        //normales skalieren
        canvas.scale(gesScaleFactor,gesScaleFactor);

        super.onDraw(canvas);
        canvas.restore();
    }

    private class MyImageViewGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            onDoubleTapZoomCounter --;
            onDoubleTapZoom = onDoubleTapZoomCounter>0;
            singleTapDetector = false;
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTapZoomCounter = 2;
            onDoubleTapZoom = true;
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        double positivDetectorScaleFactor = 1.0;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            onScale = true;
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            onScale = false;
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(detector.getScaleFactor()<=0){
                //wenn man ganz schnell double tap zoomt kommt manchmal ein negativer scalefactor
                Log.w("MyImageView","ScaleListener >> onScale >> detector.getScaleFactor()<=0\nwahrscheinlich zu schnell gezoomt wird ignoriert");
                return true;
            }
            else {
                positivDetectorScaleFactor = detector.getScaleFactor();
            }

            //scaleSpeed einbauen
            gesScaleFactor *= Math.pow(positivDetectorScaleFactor,scaleSpeed);

            // Don't let the object get too small or too large.
            gesScaleFactor = Math.max(0.01f, Math.min(gesScaleFactor, 100.0f));

            invalidate();

            return true;
        }
    }
}


