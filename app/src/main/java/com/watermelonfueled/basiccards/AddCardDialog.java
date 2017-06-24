package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by dapar on 2017-01-17.
 */

public class AddCardDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{
    private Bitmap image;


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        listener.onAddCardDialogSelectSubstack(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onAddCardDialogSelectSubstack(0);  // default 1st option
    }

    public interface AddCardDialogListener{
        void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back, Bitmap image);
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

    static final int PICK_IMAGE = 1;

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
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
            }
        });

        builder.setTitle("Add Card")
//                .setSingleChoiceItems(substackNames, 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        listener.onAddCardDialogSelectSubstack(which);
//                    }
//                })
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        EditText frontInput = (EditText) dialogView.findViewById(R.id.inputQuestion);
                        EditText backInput = (EditText) dialogView.findViewById(R.id.inputAnswer);
                        listener.onAddCardDialogPositiveClick(AddCardDialog.this,
                                frontInput.getText().toString(), backInput.getText().toString(), image);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //cancel
                    }
                });
        return builder.create();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close(); //do i need it?
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void setSubstackNames(ArrayList<String> names) {
        substack = names;
    }
}
