package com.watermelonfueled.basiccards;

import android.provider.BaseColumns;

/**
 * Created by dapar on 2017-01-15.
 */

public final class CardsContract {
    private CardsContract() {}

    public static class StackEntry implements BaseColumns {
        public static final String TABLE_NAME = "stack";
        public static final String COLUMN_NAME = "stackName";
    }

    public static class SubstackEntry implements BaseColumns {
        public static final String TABLE_NAME = "substack";
        public static final String COLUMN_STACK = "stack_id";
        public static final String COLUMN_NAME = "substackName";
    }

    public static class CardEntry implements BaseColumns {
        public static final String TABLE_NAME = "card";
        public static final String COLUMN_SUBSTACK = "substack_id";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_ANSWER = "answer";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
