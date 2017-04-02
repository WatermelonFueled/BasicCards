package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-01-17.
 */

public class AddCardDialog extends DialogFragment {

    public interface AddCardDialogListener{
        void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back);
        void onAddCardDialogSelectSubstack(int position);
    }

    private AddCardDialogListener listener;
    private String[] substackNames;

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
        builder.setTitle("Add Card")
                .setSingleChoiceItems(substackNames, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onAddCardDialogSelectSubstack(which);
                    }
                })
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        EditText frontInput = (EditText) dialogView.findViewById(R.id.inputQuestion);
                        EditText backInput = (EditText) dialogView.findViewById(R.id.inputAnswer);
                        listener.onAddCardDialogPositiveClick(AddCardDialog.this,
                                frontInput.getText().toString(), backInput.getText().toString());
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

    public void setSubstackNames(ArrayList<String> names) {
        substackNames = new String[names.size()];
        substackNames = names.toArray(substackNames);
    }
}
