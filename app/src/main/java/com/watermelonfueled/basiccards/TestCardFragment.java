package com.watermelonfueled.basiccards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by dapar on 2017-03-06.
 */

public class TestCardFragment extends Fragment implements View.OnClickListener{
    private AnswerClickListener answerClickListener;
    public interface AnswerClickListener {
        void onAnswerClick(boolean correct, int substackId);
    }

    private boolean done = false;
    private int correctId, correctPosition, substackId;
    private String[] questionAnswers;
    private String imagePath;
    private ViewFlipper flipper;

    public static TestCardFragment newInstance(AnswerClickListener listener, int correctPosition,
                                               int substackId, String imagePath,
                                               String... questionAnswers) {
        TestCardFragment testCardFragment = new TestCardFragment();
        testCardFragment.answerClickListener = listener;
        Bundle args = new Bundle();
        args.putInt("correctPosition", correctPosition);
        args.putStringArray("questionAnswers", questionAnswers);
        args.putString("imagePath",imagePath);
        args.putInt("substackId", substackId);
        testCardFragment.setArguments(args);
        return testCardFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCorrect(getArguments().getInt("correctPosition"));
        setQuestionAnswers(getArguments().getStringArray("questionAnswers"));
        imagePath = getArguments().getString("imagePath");
        substackId = getArguments().getInt("substackId");
    }

    private void setQuestionAnswers(String[] questionAnswers) {
        this.questionAnswers = new String[5];
        for (int i = 0; i < this.questionAnswers.length; i++) {
            if (i >= questionAnswers.length) {
                this.questionAnswers[i] = "";
            } else {
                this.questionAnswers[i] = questionAnswers[i];
            }
        }
    }

    private void setCorrect(int position) {
        correctPosition = position;
        switch (position) {
            case 1:
                correctId = R.id.answer_1;
                break;
            case 2:
                correctId = R.id.answer_2;
                break;
            case 3:
                correctId = R.id.answer_3;
                break;
            case 4:
                correctId = R.id.answer_4;
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_card_view, container, false);
        flipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        Button button1 = (Button) view.findViewById(R.id.answer_1);
        Button button2 = (Button) view.findViewById(R.id.answer_2);
        Button button3 = (Button) view.findViewById(R.id.answer_3);
        Button button4 = (Button) view.findViewById(R.id.answer_4);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button1.setText(questionAnswers[1]);
        button2.setText(questionAnswers[2]);
        button3.setText(questionAnswers[3]);
        button4.setText(questionAnswers[4]);
        TextView front = (TextView) view.findViewById(R.id.text_front);
        front.setText(questionAnswers[0]);
        TextView back = (TextView) view.findViewById(R.id.text_back);
        back.setText(questionAnswers[correctPosition]);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(back, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(back, 12, 40, 2, TypedValue.COMPLEX_UNIT_DIP);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(front, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(front, 12, 40, 2, TypedValue.COMPLEX_UNIT_DIP);


        if (imagePath != null && !imagePath.equalsIgnoreCase("")) {
            ((ImageView) view.findViewById(R.id.image)).setImageBitmap(ImageHelper.loadImage(imagePath,
                    ImageHelper.getScreenWidthPx(getContext()),
                    ImageHelper.getPixelsFromDp(getContext(), R.dimen.card_list_item_height)));
        } else {
            front.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        return view;
    }

    @Override
    public void onClick(View selectedAnswer) {
        if (done) { return; }
        done = true;
        flipper.setInAnimation(this.getContext(),R.anim.card_flip_bottom_in);
        flipper.setOutAnimation(this.getContext(),R.anim.card_flip_top_out);
        flipper.showNext();
        if (selectedAnswer.getId() == correctId) {
            selectedAnswer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            answerClickListener.onAnswerClick(true, substackId);
        } else {
            selectedAnswer.setBackgroundColor(getResources().getColor(R.color.backgroundGrey));
            answerClickListener.onAnswerClick(false, substackId);
        }
    }



}
