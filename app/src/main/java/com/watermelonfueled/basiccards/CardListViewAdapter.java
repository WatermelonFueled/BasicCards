package com.watermelonfueled.basiccards;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-02-04.
 */
public class CardListViewAdapter extends RecyclerView.Adapter<CardListViewAdapter.CardListViewHolder>{
    private final String TAG = "CardListViewAdapter";

    private ArrayList<String> cardFrontList, cardBackList;
    private SparseArray<String> cardImageList;
    private int targetW,targetH;
    private Context context;

    public CardListViewAdapter(ArrayList<String> cardFrontList, ArrayList<String> cardBackList,
                               SparseArray<String> cardImageList, Context context) {
        this.cardFrontList = cardFrontList;
        this.cardBackList = cardBackList;
        this.cardImageList = cardImageList;
        this.context = context;
        targetW = ImageHelper.getScreenWidthPx(context);
        targetH = ImageHelper.getPixelsFromDp(context,250);
        Log.d(TAG, "target dimen WxH: " + targetW + " x " + targetH);
    }

    @Override
    public CardListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.card_view, viewGroup, false );
        return new CardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardListViewHolder holder, int position){
        holder.imagePath = cardImageList.valueAt(position);
        new GetImageAsyncTask().execute(holder);
        holder.front.setText(cardFrontList.get(position));
        holder.back.setText(cardBackList.get(position));;

    }

    private class GetImageAsyncTask extends AsyncTask<CardListViewHolder, Void, Bitmap> {
        CardListViewHolder holder;
        protected Bitmap doInBackground(CardListViewHolder... holders){
            holder = holders[0];
            if (holder.imagePath != null && !holder.imagePath.equals("")) {
                return ImageHelper.loadImage(holder.imagePath, targetW, targetH);
            } else {
                return null;
            }
        }

        protected void onPostExecute(Bitmap result){
            if (result != null) {
                holder.image.setImageBitmap(result);
            } else {
                holder.image.setImageDrawable(null);
                holder.front.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                holder.front.requestLayout();
                //holder.image.setImageResource(android.R.color.transparent);
            }
        }

    }

    @Override
    public int getItemCount() { return cardFrontList.size(); }

    class CardListViewHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener,
                        View.OnLongClickListener,
                        View.OnFocusChangeListener {
        TextView front, back;
        ImageView image;
        ViewFlipper flipper;
        boolean frontShowing, buttonsShowing;
        ImageButton deleteButton, editButton;
        String imagePath;

        public CardListViewHolder(View itemView) {
            super(itemView);
            frontShowing = true;
            front = (TextView) itemView.findViewById(R.id.text_front);
            back = (TextView) itemView.findViewById(R.id.text_back);
            image = (ImageView) itemView.findViewById(R.id.image);
            flipper = (ViewFlipper) itemView.findViewById(R.id.view_flipper);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            editButton = (ImageButton) itemView.findViewById(R.id.edit_button);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);
            TextViewCompat.setAutoSizeTextTypeWithDefaults(back, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(back,12,40,2, TypedValue.COMPLEX_UNIT_DIP);
            TextViewCompat.setAutoSizeTextTypeWithDefaults(front, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(front, 12, 40, 2, TypedValue.COMPLEX_UNIT_DIP);
            itemView.setFocusable(true);
            buttonsShowing = false;
        }

        @Override
        public void onClick(View view) {
            view.requestFocusFromTouch();
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
            editButton.setTag(clickedPosition);
            view.requestFocusFromTouch();
            buttonsShow();
            return true;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus && buttonsShowing) {
                buttonsHide();
            }
        }

        private void buttonsHide() {
            deleteButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_right_out));
            deleteButton.setVisibility(View.GONE);
            editButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_right_out));
            editButton.setVisibility(View.GONE);
            buttonsShowing = false;
        }

        private void buttonsShow() {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_left_in));
            editButton.setVisibility(View.VISIBLE);
            editButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.button_slide_left_in));
            buttonsShowing = true;
        }
    }

}
