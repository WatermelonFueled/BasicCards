package com.watermelonfueled.basiccards;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by dapar on 2017-01-17.
 */

public class AddCardDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{
    private final String TAG = "AddCardDialog";

    Bitmap image;
    Uri imageUri;
    String imagePath, oldPath;
    //File imageFile, oldFile;
    ImageView thumbnailView;
    boolean fileExists = false, photoExists = false;

    private AddCardDialogListener listener;
    private ArrayList<String> substack;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("bundle", bundleState());
        super.onSaveInstanceState(outState);
    }

    private Bundle bundleState(){
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        bundle.putString("oldPath", oldPath);
        bundle.putBoolean("fileExists", fileExists);
        bundle.putBoolean("photoExists", photoExists);
        if (imageUri != null) {
            bundle.putString("imageUri", imageUri.toString());
        }
        bundle.putStringArrayList("substack", substack);
        Log.d(TAG, "Bundled: " + imagePath + ", " + oldPath + ", " + fileExists + ", " + photoExists);
        return bundle;
    }

    public void unbundleState(Bundle bundle) {
        imagePath = bundle.getString("imagePath");
        oldPath = bundle.getString("oldPath");
        fileExists = bundle.getBoolean("fileExists");
        photoExists = bundle.getBoolean("photoExists");
        try {
            imageUri = Uri.parse(bundle.getString("imageUri"));
        } catch (NullPointerException e) {
            //no uri saved to bundle
        }
        substack = bundle.getStringArrayList("substack");
        Log.d(TAG, "Unbundled: " + imagePath + ", " + oldPath + ", " + fileExists + ", " + photoExists);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        unbundleState(savedInstanceState.getBundle("bundle"));

        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        listener.onAddCardDialogSelectSubstack(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onAddCardDialogSelectSubstack(0);  // default 1st option
    }

    public interface AddCardDialogListener{
        void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back, boolean fileExists, Uri imageUri, String imagePath);
        void onAddCardDialogSelectSubstack(int position);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try { listener = (AddCardDialogListener) activity; }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddCardDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_addcard,null);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.substackSpinner);
        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, substack);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Button addImageButton = (Button) dialogView.findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickGalleryImage();
            }
        });

        Button takePhotoButton = (Button) dialogView.findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check camera feature
                if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
                    //check camera permission
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        takePhoto();
                    }
                }//TODO (else) if no camera feature ?
            }
        });

        thumbnailView = (ImageView) dialogView.findViewById(R.id.thumbnail);
        thumbnailView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (imagePath != null) { image = ImageHelper.loadImage(imagePath, thumbnailView.getWidth(),thumbnailView.getHeight()); }
                if (image != null) { thumbnailView.setImageBitmap(image); }
            }
        });

        builder.setTitle("Add Card")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        EditText frontInput = (EditText) dialogView.findViewById(R.id.inputQuestion);
                        EditText backInput = (EditText) dialogView.findViewById(R.id.inputAnswer);

                        Log.d(TAG, "Positive click: " + frontInput.getText().toString() +", "+
                        backInput.getText().toString()+", "+photoExists+", "+imageUri+", "+imagePath);

                        listener.onAddCardDialogPositiveClick(AddCardDialog.this,
                                frontInput.getText().toString(), backInput.getText().toString(),
                                photoExists, imageUri, imagePath);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //oldFile = imageFile;
                        oldPath = imagePath;
                        checkAndDeleteCapturedPhoto();
                    }
                });
        return builder.create();
    }

    public static final int PICK_IMAGE = 1, TAKE_PHOTO = 2, REQUEST_CAMERA_PERMISSION = 3;


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult called - requestCode: " + requestCode+ " resultCode: " + resultCode);
//        super.onActivityResult(requestCode,resultCode,data);
//        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
//            // not okay result code
//            if (oldPath != null) {
//                imagePath = oldPath;
//            }
//            return;
//        }
//        photoExists = true;
//
//        switch (requestCode) {
//            case PICK_IMAGE:
//                Log.d(TAG, "OnActivityResult for PICK IMAGE intent");
//                try {
//                    InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
//                    //thumbnail options
//                    BitmapFactory.Options opts = new BitmapFactory.Options();
//                    opts.outHeight = thumbnailView.getHeight();
//                    opts.outWidth = thumbnailView.getWidth();
//                    opts.inSampleSize = 4; //factor for smaller size (powers of 2)
//                    image = BitmapFactory.decodeStream(inputStream, null, opts);
//                    inputStream.close(); //do i need it?
//                    imageUri = data.getData();
//                    imagePath = null;
//                    oldPath = null;
//                    checkAndDeleteCapturedPhoto();
//                    fileExists = false;
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case TAKE_PHOTO:
//                Log.d(TAG, "OnActivityResult for TAKE PHOTO intent");
//                checkAndDeleteCapturedPhoto(); // delete previous captured photo
//                fileExists = true;
//                imageUri = null;
//                //imagePath = imageFile.getAbsolutePath();
//                image = ImageHelper.loadImage(imagePath,thumbnailView.getWidth(),thumbnailView.getHeight());
//                break;
//
//        }
//
//        thumbnailView.setImageBitmap(image);
//
//    }

    public void checkAndDeleteCapturedPhoto() {
        if (fileExists) {
            Log.d(TAG, "Deleting: " + oldPath);
//            oldFile.delete();
            File oldFile = new File(oldPath);
            oldFile.delete();
        }
    }

    private void pickGalleryImage(){
        //oldFile = imageFile;
        oldPath = imagePath;

        SubstackActivity activity = (SubstackActivity) getActivity();
        activity.setAddCardDialogBundle(bundleState());

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activity.startAddCardDialogIntent(intent,PICK_IMAGE);

        //getActivity().startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            try {
                //oldFile = imageFile;
                oldPath = imagePath;
                File imageFile = DbHelper.createImageFile();
                imagePath = imageFile.getAbsolutePath();

                SubstackActivity activity = (SubstackActivity) getActivity();
                activity.setAddCardDialogBundle(bundleState());

                Uri uri = FileProvider.getUriForFile(getContext(), "com.watermelonfueled.basiccards.fileprovider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                activity.startAddCardDialogIntent(intent, TAKE_PHOTO);

                //getActivity().startActivityForResult(intent, TAKE_PHOTO);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
        }
    }

    public void setSubstackNames(ArrayList<String> names) {
        substack = names;
    }
}
