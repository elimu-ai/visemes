package org.literacyapp.visemes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import org.literacyapp.visemes.util.MediaPlayerHelper;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.renderer.RajawaliRenderer;

public class PoseRenderer extends RajawaliRenderer {

    private static final double ONE_BILLION = 1000 * 1000 * 1000;
    static final double PEAK_OFFSET = 0.5;

    private Context context;

    Object3D object3dFace;
    Object3D object3dFaceAh;
    Object3D object3dFaceEh;
//    Object3D object3dFaceSurprise;
    Object3D object3dFaceClone;
    Object3D object3dFaceMorph;

    Object3D object3dHair;
//    Object3D object3dLeftCornea;
    Object3D object3dLeftEye;
    Object3D object3dRightEye;
    Object3D object3dUpperTeeth;
    Object3D object3dLowerTeeth;
    Object3D object3dTongue;

    public PoseRenderer(Context context) {
        super(context);
        Log.i(getClass().getName(), "PoseRenderer");
        this.context = context;
    }

    protected void initScene() {
        Log.i(getClass().getName(), "initScene");

        try {
            getCurrentScene().setBackgroundColor(Color.WHITE);

            LoaderOBJ loaderObjFace = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.face_tri_20_obj);
            loaderObjFace.parse();
            object3dFace = loaderObjFace.getParsedObject();
            Log.d(getClass().getName(), "object3dFace.getGeometry().getVertices(): " + object3dFace.getGeometry().getVertices());
            object3dFace.setTransparent(false);

            LoaderOBJ loaderObjFaceAh = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.face_ah_tri_20_obj);
            loaderObjFaceAh.parse();
            object3dFaceAh = loaderObjFaceAh.getParsedObject();
            Log.d(getClass().getName(), "object3dFaceAh.getGeometry().getVertices(): " + object3dFaceAh.getGeometry().getVertices());
            object3dFaceAh.setTransparent(false);

            LoaderOBJ loaderObjFaceEh = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.face_eh_tri_20_obj);
            loaderObjFaceEh.parse();
            object3dFaceEh = loaderObjFaceEh.getParsedObject();
            Log.d(getClass().getName(), "object3dFaceEh.getGeometry().getVertices(): " + object3dFaceEh.getGeometry().getVertices());
            object3dFaceEh.setTransparent(false);

//            LoaderOBJ loaderObjFaceSurprise = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.face_surprise_obj);
//            loaderObjFaceSurprise.parse();
//            object3dFaceSurprise = loaderObjFaceSurprise.getParsedObject();
//            Log.d(getClass().getName(), "object3dFaceSurprise.getGeometry().getVertices(): " + object3dFaceSurprise.getGeometry().getVertices());
//            object3dFaceSurprise.setTransparent(false);

            LoaderOBJ loaderObjHair = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.hair_tri_20_obj);
            loaderObjHair.parse();
            object3dHair = loaderObjHair.getParsedObject();
            object3dHair.setTransparent(false);

//            LoaderOBJ loaderObjLeftCornea = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.left_cornea_obj);
//            loaderObjLeftCornea.parse();
//            object3dLeftCornea = loaderObjLeftCornea.getParsedObject();
//            object3dLeftCornea.setTransparent(false);


            LoaderOBJ loaderObjLeftEye = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.left_eye_tri_20_obj);
            loaderObjLeftEye.parse();
            object3dLeftEye = loaderObjLeftEye.getParsedObject();
            object3dLeftEye.setTransparent(false);

            LoaderOBJ loaderObjRightEye = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.right_eye_tri_20_obj);
            loaderObjRightEye.parse();
            object3dRightEye = loaderObjRightEye.getParsedObject();
            object3dRightEye.setTransparent(false);

            LoaderOBJ loaderObjUpperTeeth = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.upper_teeth_tri_20_obj);
            loaderObjUpperTeeth.parse();
            object3dUpperTeeth = loaderObjUpperTeeth.getParsedObject();
            object3dUpperTeeth.setTransparent(false);

            LoaderOBJ loaderObjLowerTeeth = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.lower_teeth_tri_20_obj);
            loaderObjLowerTeeth.parse();
            object3dLowerTeeth = loaderObjLowerTeeth.getParsedObject();
            object3dLowerTeeth.setTransparent(false);

            LoaderOBJ loaderObjTongue = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.tongue_tri_20_obj);
            loaderObjTongue.parse();
            object3dTongue = loaderObjTongue.getParsedObject();
            object3dTongue.setTransparent(false);

//            Material material = new Material();
//            material.enableLighting(true);
//            material.setDiffuseMethod(new DiffuseMethod.Lambert());
////            material.setColor(Color.rgb(185, 140, 111));
//            Texture texture = new Texture("Face", R.drawable.face);
//            try{
//                material.addTexture(texture);
//            } catch (ATexture.TextureException error){
////                Log.d(getClass(), "TEXTURE ERROR");
//            }
//            object3dFace.setMaterial(material);
//            object3dFaceAh.setMaterial(material);

            object3dFaceClone = object3dFace.clone(true);
//            Log.d(getClass(), "object3dFaceClone.getGeometry().getVertices(): " + object3dFaceClone.getGeometry().getVertices());
            getCurrentScene().addChild(object3dFaceClone);
            getCurrentScene().addChild(object3dHair);
//            getCurrentScene().addChild(object3dLeftCornea);
            getCurrentScene().addChild(object3dLeftEye);
            getCurrentScene().addChild(object3dRightEye);
            getCurrentScene().addChild(object3dUpperTeeth);
            getCurrentScene().addChild(object3dLowerTeeth);
            getCurrentScene().addChild(object3dTongue);

            ArcballCamera arcball = new ArcballCamera(mContext, ((Activity) mContext).findViewById(R.id.rajwali_surface));
//            arcball.setLookAt(object3dFaceClone.getPosition());
            arcball.setPosition(0, 0, 7);
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
////        Log.d(getClass().getName(), "onRender");
//        super.onRender(ellapsedRealtime, deltaTime);
////        Log.d(getClass().getName(), "ellapsedRealtime: " + ellapsedRealtime);
////        Log.d(getClass().getName(), "deltaTime: " + deltaTime);
//        double s = ellapsedRealtime/ONE_BILLION;
//        Log.d(getClass().getName(), "s: " + s);
//        double t = s / 4;
//        Log.d(getClass().getName(), "t: " + t);
//        object3dFaceClone.getGeometry().setVertices(vertices(object3dFace, object3dFaceAh, t), true);
//        object3dFaceClone.getGeometry().createBuffers();

        if (object3dFaceMorph == null) {
            object3dFaceMorph = object3dFaceAh;
        }

        super.onRender(ellapsedRealtime, deltaTime);
        double s = ellapsedRealtime/ONE_BILLION - PEAK_OFFSET;
        object3dFaceClone.getGeometry().setVertices(vertices(object3dFaceMorph, object3dFace, s), true);
        object3dFaceClone.getGeometry().createBuffers();
    }

    float[] vertices(Object3D pose1, Object3D pose2, double t) {
        //float weight = (float)(Math.sin(2 * Math.PI * t)/2 + 0.5);
        float weight = (float)(1/(1+t*t));
        float[] v1 = Geometry3D.getFloatArrayFromBuffer(pose1.getGeometry().getVertices());
        float[] v2 = Geometry3D.getFloatArrayFromBuffer(pose2.getGeometry().getVertices());
        float[] result = new float[v1.length];
        if(v1.length == v2.length) {
            for(int i=0; i<v1.length; i++) {
                result[i] = (v1[i]*weight + v2[i]*(1-weight));
            }
        }
        return result;
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        Log.d(getClass().getName(), "onOffsetsChanged");
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        Log.i(getClass().getName(), "onTouchEvent");


    }

    public void renderViseme(final String letter) {
        Log.i(getClass().getName(), "renderViseme");

        Log.i(getClass().getName(), "letter: " + letter);

        if ("e".equals(letter)) {
            object3dFaceMorph = object3dFaceEh;
        } else if ("t".equals(letter)) {
            // TODO
            object3dFaceMorph = object3dFaceEh;
        } else if ("a".equals(letter)) {
            object3dFaceMorph = object3dFaceAh;
        } else {
            // TODO
        }

        // Trigger animation
        onResume();

        playLetterSound(letter);

        // TODO
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                double milliseconds = phonetics.length() * 1000;
//                for (int i = 0; i < milliseconds; i += 1000) {
//                    Log.d(getClass().getName(), "i: " + i);
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        Log.e(getClass().getName(), null, e);
//                    }
//                }
//            }
//        }).start();
    }

    private void playLetterSound(String letter) {
        Log.i(getClass().getName(), "playLetterSound");

        // Look up corresponding Audio
        String audioFileName = "letter_sound_" + letter;
        int resourceId = context.getResources().getIdentifier(audioFileName, "raw", context.getPackageName());
        try {
            if (resourceId != 0) {
                MediaPlayerHelper.play(context, resourceId);
            } else {
                // Fall-back to TTS
                // TODO
            }
        } catch (Resources.NotFoundException e) {
            // Fall-back to TTS
            // TODO
        }
    }
}
