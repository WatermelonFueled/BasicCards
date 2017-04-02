package com.watermelonfueled.basiccards;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    public StackViewAdapter(ListItemClickListener listener, ArrayList<String> stackName) {
        onClickListener = listener;
        stackNameList = stackName;
    }

    @Override
    public StackViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.stack_view, viewGroup, false );
        return new StackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StackViewHolder holder, int position) {
        holder.stackTextView.setText(stackNameList.get(position));
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
        Button deleteButton;

        public StackViewHolder(View itemView) {
            super(itemView);
            stackTextView = (TextView) itemView.findViewById(R.id.stack_textView);
            deleteButton = (Button) itemView.findViewById(R.id.delete_button);
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

            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
        }

        @Override
        public boolean onLongClick(View view) {
            int clickedPosition = getAdapterPosition();
            deleteButton.setTag(clickedPosition);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            return true;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.GONE);
                v.setFocusableInTouchMode(false);
            }
        }
    }

    public void updated(ArrayList<String> updatedStackNameList) {
        stackNameList = updatedStackNameList;
        this.notifyDataSetChanged();
    }
}
