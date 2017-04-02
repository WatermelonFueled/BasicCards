package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by dapar on 2017-01-29.
 */

public class DeleteStackDialog extends DialogFragment{

    public interface DeleteStackDialogListener {
        void onDeleteStackDialogPositiveClick(DialogFragment dialog);
    }
    private DeleteStackDialogListener listener;
    private String stackName;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try { listener = (DeleteStackDialogListener) activity; }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteStackDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete " + stackName + "?")
        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteStackDialogPositiveClick(DeleteStackDialog.this);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public void setStackName(String name) {
        stackName = name;
    }
}
