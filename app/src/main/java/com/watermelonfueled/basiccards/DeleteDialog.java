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

public class DeleteDialog extends DialogFragment{

    public interface DeleteDialogListener {
        void onDeleteDialogPositiveClick(DialogFragment dialog);
    }
    private DeleteDialogListener listener;
    private String confirmMessage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try { listener = (DeleteDialogListener) activity; }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteStackDialogListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        outstate.putString("confirmMessage", confirmMessage);
        super.onSaveInstanceState(outstate);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            confirmMessage = savedInstanceState.getString("confirmMessage");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(confirmMessage)
        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteDialogPositiveClick(DeleteDialog.this);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public void setConfirmMessage(String message) {
        confirmMessage = message;
    }
}
