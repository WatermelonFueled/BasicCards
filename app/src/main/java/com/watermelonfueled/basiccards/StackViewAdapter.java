package com.watermelonfueled.basiccards;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-01-02.
 */
public class StackViewAdapter extends RecyclerView.Adapter<StackViewAdapter.StackViewHolder> {
    final private ListItemClickListener onClickListener;
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    private ArrayList<String> stackNameList;
    private ArrayList<Boolean> substackSelectedList;
    private Context context;
    int layout;
    int textViewId;

    public StackViewAdapter(ListItemClickListener listener, ArrayList<String> stackName, int layout, Context context) {
        onClickListener = listener;
        stackNameList = stackName;
        this.context = context;
        this.layout = layout;
        if (isSubstackActivity()) {
            textViewId = R.id.checkedTextView;
        } else {
            textViewId = R.id.stack_textView;
        }
    }

    @Override
    public StackViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                layout, viewGroup, false );
        return new StackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StackViewHolder holder, int position) {
        holder.stackTextView.setText(stackNameList.get(position));
        if (isSubstackActivity()) {
            ((CheckedTextView)holder.stackTextView).setChecked(substackSelectedList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return stackNameList.size();
    }

    class StackViewHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener,
                        View.OnLongClickListener,
                        View.OnFocusChangeListener {
        TextView stackTextView;
        ImageButton deleteButton, editButton;
        boolean buttonsShowing;

        public StackViewHolder(View itemView) {
            super(itemView);
            stackTextView = (TextView) itemView.findViewById(textViewId);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            editButton = (ImageButton) itemView.findViewById(R.id.edit_button);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);

            itemView.setFocusable(true);
            buttonsShowing = false;
        }

        @Override
        public void onClick(View view) {
            view.requestFocusFromTouch();

            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
            if (isSubstackActivity()) {
                ((CheckedTextView)stackTextView).toggle();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int clickedPosition = getAdapterPosition();
            deleteButton.setTag(clickedPosition);
            editButton.setTag(clickedPosition);
            view.requestFocusFromTouch();
            buttonsShow();
            return true;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus && buttonsShowing) {
                buttonsHide();
            }
        }

        private void buttonsHide(){
            deleteButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_right_out));
            deleteButton.setVisibility(View.GONE);
            editButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_right_out));
            editButton.setVisibility(View.GONE);
            buttonsShowing = false;
        }
        private void buttonsShow(){
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_left_in));
            editButton.setVisibility(View.VISIBLE);
            editButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_left_in));
            buttonsShowing = true;
        }
    }

    //substackactivity specific methods
    private boolean isSubstackActivity() {
        return layout == R.layout.substack_view;
    }
    public void setSubstackSelectedList(ArrayList list) { substackSelectedList = list; }
}
