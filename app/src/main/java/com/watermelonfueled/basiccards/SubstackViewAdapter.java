package com.watermelonfueled.basiccards;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-01-13.
 */
public class SubstackViewAdapter extends RecyclerView.Adapter<SubstackViewAdapter.SubstackViewHolder> {
    final private ListItemClickListener onClickListener;
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    private ArrayList<String> substackNameList;

    public SubstackViewAdapter(ListItemClickListener listener, ArrayList<String> substackNameList) {
        onClickListener = listener;
        this.substackNameList = substackNameList;
    }

    @Override
    public SubstackViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.substack_view, viewGroup, false );
        return new SubstackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubstackViewHolder holder, int position){
        holder.substackView.setText(substackNameList.get(position));
    }

    @Override
    public int getItemCount() { return substackNameList.size(); }

    class SubstackViewHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener,
                        View.OnLongClickListener,
                        View.OnFocusChangeListener {
        CheckedTextView substackView;
        Button deleteButton;

        public SubstackViewHolder(View itemView) {
            super(itemView);
            substackView = (CheckedTextView) itemView.findViewById(R.id.checkedTextView);
            deleteButton = (Button) itemView.findViewById(R.id.delete_button);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
            substackView.toggle();
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setFocusableInTouchMode(false);
            deleteButton.setVisibility(View.GONE);
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

    public void updated(ArrayList<String> updatedList){
        substackNameList = updatedList;
        this.notifyDataSetChanged();
    }
}
