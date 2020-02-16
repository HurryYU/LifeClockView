package com.hurryyu.lifeclockview;

import android.graphics.RectF;

public class LifeClockViewClickRectWrapper {
    private RectF mRectF;
    private Type mType;

    public LifeClockViewClickRectWrapper(RectF rectF, Type type) {
        mRectF = rectF;
        mType = type;
    }

    public RectF getRect() {
        return mRectF;
    }

    public void setRect(RectF rectF) {
        mRectF = rectF;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    enum Type {
        EDIT,
        ZERO_CIRCLE
    }
}
