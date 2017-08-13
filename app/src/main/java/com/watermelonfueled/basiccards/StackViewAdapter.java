package com.watermelonfueled.basiccards;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    int layout;
    int textViewId;

    public StackViewAdapter(ListItemClickListener listener, ArrayList<String> stackName, int layout) {
        onClickListener = listener;
        stackNameList = stackName;
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

        public StackViewHolder(View itemView) {
            super(itemView);
            stackTextView = (TextView) itemView.findViewById(textViewId);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            editButton = (ImageButton) itemView.findViewById(R.id.edit_button);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setFocusableInTouchMode(false);

            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);

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
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            return true;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                deleteButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                v.setFocusableInTouchMode(false);
            }
        }
    }

    public void updated(ArrayList<String> updatedStackNameList) {
        stackNameList = updatedStackNameList;
        this.notifyDataSetChanged();
    }

    //substackactivity specific methods
    private boolean isSubstackActivity() {
        return layout == R.layout.substack_view;
    }
    public void setSubstackSelectedList(ArrayList list) { substackSelectedList = list; }
}
