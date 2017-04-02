package com.watermelonfueled.basiccards;

import android.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-02-04.
 */
public class CardListViewAdapter extends RecyclerView.Adapter<CardListViewAdapter.CardListViewHolder>{
    final private ListItemClickListener listener;
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    private ArrayList<String> cardFrontList, cardBackList;

    public CardListViewAdapter(ListItemClickListener listener, ArrayList<String> cardFrontList,
                               ArrayList<String> cardBackList) {
        this.listener = listener;
        this.cardFrontList = cardFrontList;
        this.cardBackList = cardBackList;
    }

    @Override
    public CardListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.card_view, viewGroup, false );
        return new CardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardListViewHolder holder, int position){
        holder.front.setText(cardFrontList.get(position));
        holder.back.setText(cardBackList.get(position));
    }

    @Override
    public int getItemCount() { return cardFrontList.size(); }

    class CardListViewHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener,
                        View.OnLongClickListener,
                        View.OnFocusChangeListener {
        TextView front, back;
        ViewFlipper flipper;
        boolean frontShowing;
        Button deleteButton;

        public CardListViewHolder(View itemView) {
            super(itemView);
            frontShowing = true;
            front = (TextView) itemView.findViewById(R.id.text_front);
            back = (TextView) itemView.findViewById(R.id.text_back);
            flipper = (ViewFlipper) itemView.findViewById(R.id.view_flipper);
            deleteButton = (Button) itemView.findViewById(R.id.delete_button);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            //TODO flip card
//            listener.onListItemClick(getAdapterPosition());
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setFocusableInTouchMode(false);
            deleteButton.setVisibility(View.GONE);
            if (frontShowing) {

                flipper.setInAnimation(view.getContext(), R.anim.card_flip_bottom_in);
                flipper.setOutAnimation(view.getContext(), R.anim.card_flip_top_out);
                flipper.showNext();
            } else {
                flipper.showPrevious();
            }
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
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.GONE);
                view.setFocusableInTouchMode(false);
            }
        }
    }

    public void updated(ArrayList<String> updatedFrontList, ArrayList<String> updatedBackList){
        cardFrontList = updatedFrontList;
        cardBackList = updatedBackList;
        this.notifyDataSetChanged();
    }

}
