package com.watermelonfueled.basiccards;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Created by dapar on 2017-01-17.
 */

public class AddCardDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{
    private final String TAG = "AddCardDialog";

    Bitmap image;
    Uri imageUri;
    String imagePath, oldPath;
    ImageView thumbnailView;
    boolean fileExists = false, photoExists = false;

    EditText frontInput, backInput;
    String front, back;
    int selectedSubstackIndex = 0;

    private ArrayList<String> substack;

    private AddCardDialogListener listener;
    public interface AddCardDialogListener{
        void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back, boolean fileExists, Uri imageUri, String imagePath);
        void onAddCardDialogSelectSubstack(int position);
        void onAddCardDialogNegativeClick();
        void onAddCardDialogImageRemove();
    }

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
        bundle.putString("front",front);
        bundle.putString("back",back);
        bundle.putInt("selectedSubstackIndex", selectedSubstackIndex);
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
        front = bundle.getString("front");
        back = bundle.getString("back");
        selectedSubstackIndex = bundle.getInt("selectedSubstackIndex");

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
        selectedSubstackIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onAddCardDialogSelectSubstack(0);  // default 1st option
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
        spinner.setSelection(selectedSubstackIndex);

        ImageButton addImageButton = (ImageButton) dialogView.findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickGalleryImage();
            }
        });

        ImageButton takePhotoButton = (ImageButton) dialogView.findViewById(R.id.takePhotoButton);
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

        final ImageButton removeImageButton = (ImageButton) dialogView.findViewById(R.id.removeImageButton);

        thumbnailView = (ImageView) dialogView.findViewById(R.id.thumbnail);
        thumbnailView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (imagePath != null) { image = ImageHelper.loadImage(imagePath, thumbnailView.getWidth(),thumbnailView.getHeight()); }
                if (imageUri != null) {
                    try {
                        InputStream is = getActivity().getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 4;
                        image = BitmapFactory.decodeStream(is, null, opts);
                        is.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (image != null) {
                    thumbnailView.setImageBitmap(image);
                    removeImageButton.setClickable(true);
                    removeImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "RemoveImageButton clicked.");
                            thumbnailView.setImageDrawable(null);
                            image = null;
                            imageUri = null;

                            oldPath = imagePath;
                            imagePath = null;

                            listener.onAddCardDialogImageRemove();
                            checkAndDeleteCapturedPhoto();

                            fileExists = false;
                            photoExists = false;

                            removeImageButton.setClickable(false);

                            thumbnailView.invalidate();
                        }
                    });
                }
            }
        });

        frontInput = (EditText) dialogView.findViewById(R.id.inputQuestion);
        backInput = (EditText) dialogView.findViewById(R.id.inputAnswer);

        frontInput.setText(front);
        backInput.setText(back);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){

                        front = frontInput.getText().toString();
                        back = backInput.getText().toString();
                        Log.d(TAG, "Positive click: " + front +", "+ back+", "+
                                photoExists+", "+imageUri+", "+imagePath);

                        listener.onAddCardDialogPositiveClick(AddCardDialog.this,
                                front, back, photoExists, imageUri, imagePath);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //oldFile = imageFile;
                        oldPath = imagePath;
                        //checkAndDeleteCapturedPhoto();
                        listener.onAddCardDialogNegativeClick();
                    }
                });
        return builder.create();
    }

    public static final int PICK_IMAGE = 1, TAKE_PHOTO = 2, REQUEST_CAMERA_PERMISSION = 3;

    public void checkAndDeleteCapturedPhoto() {
        if (oldPath != null && !oldPath.equals("")) {
            Log.d(TAG, "Deleting: " + oldPath);
            File oldFile = new File(oldPath);
            oldFile.delete();
            oldPath = null;
        }
    }

    private void pickGalleryImage(){
        oldPath = imagePath;

        front = frontInput.getText().toString();
        back = backInput.getText().toString();

        CardListActivity activity = (CardListActivity) getActivity();
        activity.setAddCardDialogBundle(bundleState());

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activity.startAddCardDialogIntent(intent,PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            try {
                oldPath = imagePath;
                File imageFile = DbHelper.createImageFile();
                imagePath = imageFile.getAbsolutePath();

                front = frontInput.getText().toString();
                back = backInput.getText().toString();

                CardListActivity activity = (CardListActivity) getActivity();
                activity.setAddCardDialogBundle(bundleState());

                Uri uri = FileProvider.getUriForFile(getContext(), "com.watermelonfueled.basiccards.fileprovider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                activity.startAddCardDialogIntent(intent, TAKE_PHOTO);
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
