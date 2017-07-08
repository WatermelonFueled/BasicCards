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
import android.widget.Button;
import android.widget.EditText;
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

    private Bitmap image;
    private Uri imageUri;
    private String imagePath;
    private File imageFile, oldFile;
    private ImageView thumbnailView;
    private boolean fileExists = false, photoExists = false;


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        listener.onAddCardDialogSelectSubstack(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onAddCardDialogSelectSubstack(0);  // default 1st option
    }

    public interface AddCardDialogListener{
        void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back, boolean photoExists, Uri imageUri, String imagePath);
        void onAddCardDialogSelectSubstack(int position);
    }

    private AddCardDialogListener listener;
    private ArrayList<String> substack;

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

        builder.setTitle("Add Card")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        EditText frontInput = (EditText) dialogView.findViewById(R.id.inputQuestion);
                        EditText backInput = (EditText) dialogView.findViewById(R.id.inputAnswer);
                        listener.onAddCardDialogPositiveClick(AddCardDialog.this,
                                frontInput.getText().toString(), backInput.getText().toString(),
                                photoExists, imageUri, imagePath);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        oldFile = imageFile;
                        checkAndDeleteCapturedPhoto();
                    }
                });
        return builder.create();
    }

    static final int PICK_IMAGE = 1, TAKE_PHOTO = 2, REQUEST_CAMERA_PERMISSION = 3;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            // not okay result code

            return;
        }
        photoExists = true;

        switch (requestCode) {
            case PICK_IMAGE:
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                    //thumbnail options
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 2; //factor for smaller size (powers of 2)
                    image = BitmapFactory.decodeStream(inputStream, null, opts);
                    inputStream.close(); //do i need it?
                    imageUri = data.getData();
                    imagePath = null;
                    checkAndDeleteCapturedPhoto();
                    fileExists = false;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO:
                checkAndDeleteCapturedPhoto(); // delete previous captured photo
                fileExists = true;
                imageUri = null;
                imagePath = imageFile.getAbsolutePath();
                image = ImageHelper.loadImage(imagePath,thumbnailView.getWidth(),thumbnailView.getHeight());
                break;

        }

        thumbnailView.setImageBitmap(image);

    }

    private void checkAndDeleteCapturedPhoto() {
        if (fileExists) {
            Log.d(TAG, "Deleting: " + oldFile.toString());
            oldFile.delete();
        }
    }

    private void pickGalleryImage(){
        oldFile = imageFile;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            try {
                oldFile = imageFile;
                imageFile = DbHelper.createImageFile();
                Uri uri = FileProvider.getUriForFile(getContext(), "com.watermelonfueled.basiccards.fileprovider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, TAKE_PHOTO);
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
