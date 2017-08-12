package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by dapar on 2017-01-24.
 */

public class AddStackDialog extends DialogFragment{

    public interface AddStackDialogListener{
        void onAddStackDialogPositiveClick(DialogFragment dialog, String stackName);
        enum Type{CREATE,EDIT}
        Type getType();
        String getNameToEdit();
    }

    private AddStackDialogListener listener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try { listener = (AddStackDialogListener) activity; }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddStackDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_addstack,null);

        String positiveButtonText;
        switch (listener.getType()) {
            case CREATE:
                positiveButtonText = "Create";
                break;
            case EDIT:
                positiveButtonText = "Make edit";
                EditText textbox = (EditText) dialogView.findViewById(R.id.stackname);
                textbox.setText(listener.getNameToEdit());
                break;
            default:
                positiveButtonText = "";
        }

        builder.setView(dialogView)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText stackNameInput = (EditText) dialogView.findViewById(R.id.stackname);
                        listener.onAddStackDialogPositiveClick(AddStackDialog.this, stackNameInput.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }
}
