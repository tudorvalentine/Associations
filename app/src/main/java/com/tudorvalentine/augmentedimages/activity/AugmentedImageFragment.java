package com.tudorvalentine.augmentedimages.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;
import com.tudorvalentine.augmentedimages.app.AppConfig;
import com.tudorvalentine.augmentedimages.app.AppController;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
public class AugmentedImageFragment extends ArFragment{
  private static final String TAG = AugmentedImageFragment.class.getSimpleName();
  private SQLiteHandler db;

  // This is the name of the image in the sample database.  A copy of the image is in the assets
  // directory.  Opening this image on your computer is a good quick way to test the augmented image
  // matching.
//  private static final String DEFAULT_IMAGE_NAME = "pointer.jpg";
  // This is a pre-created database containing the sample image.
  private static final String SAMPLE_IMAGE_DATABASE = "imgdb.imgdb";
  // Augmented image configuration and rendering.
  // Load a single image (true) or a pre-generated image database (false).
  private static final boolean USE_SINGLE_IMAGE = false;

  // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
  // application.
  private static final double MIN_OPENGL_VERSION = 3.0;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Check for Sceneform being supported on this device.  This check will be integrated into
    // Sceneform eventually.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(getContext(), "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
    }

    String openGlVersionString =
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
      Toast.makeText(getContext(), "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show();
    }
    db = new SQLiteHandler(getActivity());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    // Turn off the plane discovery since we're only looking for images
    getPlaneDiscoveryController().hide();
    getPlaneDiscoveryController().setInstructionView(null);
    getArSceneView().getPlaneRenderer().setEnabled(false);
    return view;
  }

  @Override
  protected Config getSessionConfiguration(Session session) {
    Config config = super.getSessionConfiguration(session);
    config.setFocusMode(Config.FocusMode.AUTO);
    if (!setupAugmentedImageDatabase(config, session)) {
      Toast.makeText(getContext(), "Could not setup augmented image database", Toast.LENGTH_SHORT).show();
    }
    return config;
  }

  private boolean setupAugmentedImageDatabase(Config config, Session session) {
    AugmentedImageDatabase augmentedImageDatabase;
    List<Map<String, String>> associations = db.getUserData();
    AppController.getInstance().downloadFile(AppConfig.URL_GET_IMGDB,"imgdb.imgdb", db.getUserDetails().get("username"));

    // There are two ways to configure an AugmentedImageDatabase:
    // 1. Add Bitmap to DB directly
    // 2. Load a pre-built AugmentedImageDatabase
    // Option 2) has
    // * shorter setup time
    // * doesn't require images to be packaged in apk.
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {

      }
    }, 500);
    if (USE_SINGLE_IMAGE) {


      augmentedImageDatabase = new AugmentedImageDatabase(session);
      Bitmap augmentedImageBitmap = null;
      for (Map<String, String> map : associations) {
        String fileName = map.get("image");
        augmentedImageBitmap = loadAugmentedImageBitmap(fileName);
        augmentedImageDatabase.addImage(fileName, augmentedImageBitmap);
        Log.d(TAG, "Load images in AR session > " + fileName);
      }

//      for (String fileName : imageName) {
//      }

      if (augmentedImageBitmap == null) {
        return false;
      }

      // If the physical size of the image is known, you can instead use:
      //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
      // This will improve the initial detection speed. ARCore will still actively estimate the
      // physical size of the image as it is viewed from multiple viewpoints.
    } else {
      // This is an alternative way to initialize an AugmentedImageDatabase instance,
      // load a pre-existing augmented image database.
      try (FileInputStream is = getContext().openFileInput(SAMPLE_IMAGE_DATABASE)) {
        augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
      } catch (IOException e) {
        Log.e(TAG, "IO exception loading augmented image database.", e);
        return false;
      }
    }

    config.setAugmentedImageDatabase(augmentedImageDatabase);
    return true;
  }

  private Bitmap loadAugmentedImageBitmap(String imageName) {
    try (InputStream is = getContext().openFileInput(imageName)) {
      return BitmapFactory.decodeStream(is);
    } catch (IOException e) {
      Log.e(TAG, "IO exception loading augmented image bitmap.", e);
    }catch (NullPointerException e){
      Log.e(TAG, "OpenFileInput produce NullPointer" + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
