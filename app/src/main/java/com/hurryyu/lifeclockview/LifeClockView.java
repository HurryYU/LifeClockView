package com.hurryyu.lifeclockview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LifeClockView extends View {
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private Rect mTextHeightRect = new Rect();
    private RectF mZeroAgeTipsRectF = new RectF();
    private Matrix mMatrix = new Matrix();
    private Matrix mCanvasMatrix = new Matrix();
    private Matrix mCanvasInvertMatrix = new Matrix();
    private List<LifeClockViewClickRectWrapper> mClickRectWrapperList = new ArrayList<>();
    private float[] mOriginalXY = new float[2];
    private float[] mInvertDownXY = new float[2];
    private boolean mIsShowZeroAgeTips;

    private OnEditClickListener mOnEditClickListener;

    private static final int MAX_AGE = 120;

    private static final int DEFAULT_AGE_TEXT_COLOR = Color.parseColor("#333333");
    private static final int DEFAULT_PAST_AGE_TEXT_COLOR = Color.parseColor("#C5C5C5");
    private static final float DEFAULT_AGE_TEXT_SIZE = 22;
    private static final float DEFAULT_AGE_TEXT_OFFSET = 24;
    private static final float DEFAULT_ZERO_AGE_DOWN_CIRCLE_RADIUS = 4;
    private static final int DEFAULT_ZERO_AGE_DOWN_CIRCLE_COLOR = Color.parseColor("#F7C747");
    private static final float DEFAULT_ZERO_AGE_DOWN_CIRCLE_OFFSET = 5;
    private static final float DEFAULT_CENTER_CIRCLE_RADIUS = 6;
    private static final int DEFAULT_CENTER_CIRCLE_COLOR = Color.BLACK;
    private static final float DEFAULT_POINTER_LINE_WIDTH = 4;
    private static final int DEFAULT_POINTER_LINE_COLOR = Color.BLACK;
    private static final float DEFAULT_ZERO_AGE_TIPS_RECT_WIDTH = 50;
    private static final float DEFAULT_ZERO_AGE_TIPS_RECT_HEIGHT = 20;
    private static final int DEFAULT_ZERO_AGE_TIPS_RECT_COLOR = Color.parseColor("#F7C747");
    private static final float DEFAULT_ZERO_AGE_TIPS_RECT_CORNERS = 12;
    private static final float DEFAULT_ZERO_AGE_TIPS_TEXT_SIZE = 10;
    private static final int DEFAULT_ZERO_AGE_TIPS_TEXT_COLOR = Color.WHITE;

    private static final float PERCENT_STR_SIZE = 60;
    private static final int PERCENT_STR_COLOR = Color.parseColor("#333333");
    private static final float PERCENT_TIPS_STR_SIZE = 14;
    private static final int PERCENT_TIPS_STR_COLOR = Color.parseColor("#C5C5C5");
    private static final float PERCENT_STR_MARGIN_DOT = 30;
    private static final float PERCENT_TIPS_STR_MARGIN_PERCENT_STR = 14;

    private int mCurrentMaxAge = 80;

    private List<String> mAgeTextList = new ArrayList<>();

    private Bitmap mEditBitmap;

    /**
     * 年龄文本字体颜色
     */
    private int mAgeTextColor = DEFAULT_AGE_TEXT_COLOR;

    private int mPastAgeTextColor = DEFAULT_PAST_AGE_TEXT_COLOR;
    /**
     * 年龄文本字体大小
     */
    private float mAgeTextSize = sp2px(DEFAULT_AGE_TEXT_SIZE);
    /**
     * 年龄文本中心点与外圆的偏移量
     */
    private float mAgeTextOffset = dp2px(DEFAULT_AGE_TEXT_OFFSET);

    /**
     * 0岁年龄下方圆圈的半径
     */
    private float mZeroAgeDownCircleRadius = dp2px(DEFAULT_ZERO_AGE_DOWN_CIRCLE_RADIUS);

    /**
     * 0岁年龄下方圆圈颜色
     */
    private int mZeroAgeDownCircleColor = DEFAULT_ZERO_AGE_DOWN_CIRCLE_COLOR;

    /**
     * 圆圈偏移量
     */
    private float mZeroAgeDownCircleOffset = dp2px(DEFAULT_ZERO_AGE_DOWN_CIRCLE_OFFSET);

    /**
     * 中心圆点半径
     */
    private float mCenterCircleRadius = dp2px(DEFAULT_CENTER_CIRCLE_RADIUS);

    /**
     * 中心圆点颜色
     */
    private int mCenterCircleColor = DEFAULT_CENTER_CIRCLE_COLOR;

    /**
     * 指针宽度
     */
    private float mPointerLineWidth = dp2px(DEFAULT_POINTER_LINE_WIDTH);

    /**
     * 指针颜色
     */
    private int mPointerLineColor = DEFAULT_POINTER_LINE_COLOR;

    /**
     * 出生日说明矩形区域颜色
     */
    private int mZeroAgeTipsRectColor = DEFAULT_ZERO_AGE_TIPS_RECT_COLOR;

    /**
     * 指针角度(此值会变动[属性动画]),必须>0才会绘制指针,可通过设置mCurrentAge从而自动计算角度
     */
    private float mPointerLineAngle;

    /**
     * 最终指针角度
     */
    private float mPointerLineAngleFinal;
    /**
     * 一生已过的百分比
     */
    private String mPercentStr;

    private int mPointerLineAndPercentAlpha;

    /**
     * 是否是第一次计算指针角度
     */
    private boolean isFirstCalc = true;

    /**
     * 当前实际年龄
     */
    private int mCurrentAge;

    private Paint mAgeTextPaint;
    private Paint mZeroAgeDownCirclePaint;
    private Paint mCenterCirclePaint;
    private Paint mPointerLinePaint;
    private Paint mPercentTextPaint;
    private Paint mZeroAgeTipsPaint;

    public LifeClockView(Context context) {
        this(context, null);
    }

    public LifeClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LifeClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.LifeClockView);
        mAgeTextColor = typedArray.getColor(R.styleable.LifeClockView_lcv_age_text_color, DEFAULT_AGE_TEXT_COLOR);
        mPastAgeTextColor = typedArray.getColor(R.styleable.LifeClockView_lcv_past_age_text_color, DEFAULT_PAST_AGE_TEXT_COLOR);
        mAgeTextSize = typedArray.getDimension(R.styleable.LifeClockView_lcv_age_text_size, sp2px(DEFAULT_AGE_TEXT_SIZE));
        mAgeTextOffset = typedArray.getDimension(R.styleable.LifeClockView_lcv_age_text_offset, dp2px(DEFAULT_AGE_TEXT_OFFSET));
        mZeroAgeDownCircleRadius = typedArray.getDimension(R.styleable.LifeClockView_lcv_zero_age_down_circle_radius, dp2px(DEFAULT_ZERO_AGE_DOWN_CIRCLE_RADIUS));
        mZeroAgeDownCircleColor = typedArray.getColor(R.styleable.LifeClockView_lcv_zero_age_down_circle_color, DEFAULT_ZERO_AGE_DOWN_CIRCLE_COLOR);
        mZeroAgeDownCircleOffset = typedArray.getDimension(R.styleable.LifeClockView_lcv_zero_age_down_circle_offset, dp2px(DEFAULT_ZERO_AGE_DOWN_CIRCLE_OFFSET));
        mCenterCircleRadius = typedArray.getDimension(R.styleable.LifeClockView_lcv_center_circle_radius, dp2px(DEFAULT_CENTER_CIRCLE_RADIUS));
        mCenterCircleColor = typedArray.getColor(R.styleable.LifeClockView_lcv_center_circle_color, DEFAULT_CENTER_CIRCLE_COLOR);
        mPointerLineWidth = typedArray.getDimension(R.styleable.LifeClockView_lcv_pointer_line_width, dp2px(DEFAULT_POINTER_LINE_WIDTH));
        mPointerLineColor = typedArray.getColor(R.styleable.LifeClockView_lcv_pointer_line_color, DEFAULT_POINTER_LINE_COLOR);
        int tempMaxAge = typedArray.getInt(R.styleable.LifeClockView_lcv_current_max_age, 80);
        mCurrentMaxAge = tempMaxAge > MAX_AGE ? MAX_AGE : tempMaxAge;
        typedArray.recycle();
    }

    private void init() {
        mEditBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit);
        generateAgeText();
        mAgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAgeTextPaint.setColor(mAgeTextColor);
        mAgeTextPaint.setTextSize(mAgeTextSize);
        mAgeTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mZeroAgeDownCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mZeroAgeDownCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mZeroAgeDownCirclePaint.setColor(mZeroAgeDownCircleColor);

        mCenterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCenterCirclePaint.setColor(mCenterCircleColor);

        mPointerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointerLinePaint.setStrokeWidth(mPointerLineWidth);
        mPointerLinePaint.setColor(mPointerLineColor);
        mPointerLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mPercentTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mZeroAgeTipsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void generateAgeText() {
        for (int i = 0; i <= mCurrentMaxAge; i += 5) {
            mAgeTextList.add(String.valueOf(i));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mCanvasMatrix);
        mClickRectWrapperList.clear();

        drawAgeText(canvas);
        drawZeroAgeDownDot(canvas);
        if (mPointerLineAngleFinal > 0) {
            drawCenterCircle(canvas);
            drawPointerLine(canvas);

            if (mPointerLineAngleFinal > 90 && mPointerLineAngleFinal < 270) {
                drawPercent(canvas, false);
            } else {
                drawPercent(canvas, true);
            }
        }
    }

    private void drawPercent(Canvas canvas, boolean isDrawDown) {
        mPercentTextPaint.setTextSize(sp2px(PERCENT_STR_SIZE));
        mPercentTextPaint.setColor(PERCENT_STR_COLOR);
        mPercentTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPercentTextPaint.getTextBounds(mPercentStr, 0, mPercentStr.length(), mTextHeightRect);
        float percentHeight = mTextHeightRect.bottom - mTextHeightRect.top;
        float percentStrMarginDot = dp2px(PERCENT_STR_MARGIN_DOT);
        float percentTipsStrMarginPercentStr = dp2px(PERCENT_TIPS_STR_MARGIN_PERCENT_STR);
        mPercentTextPaint.setAlpha(mPointerLineAndPercentAlpha);
        canvas.drawText(mPercentStr,
                -mPercentTextPaint.measureText(mPercentStr) / 2F,
                isDrawDown ?
                        percentHeight + percentStrMarginDot :
                        -percentStrMarginDot,
                mPercentTextPaint);

        mPercentTextPaint.setTextSize(sp2px(PERCENT_TIPS_STR_SIZE));
        mPercentTextPaint.setColor(PERCENT_TIPS_STR_COLOR);
        mPercentTextPaint.setTypeface(Typeface.DEFAULT);
        String percentTipsStr = "一生已过";
        mPercentTextPaint.getTextBounds(percentTipsStr, 0, percentTipsStr.length(), mTextHeightRect);
        float percentTipsHeight = mTextHeightRect.bottom - mTextHeightRect.top;
        mPercentTextPaint.setAlpha(mPointerLineAndPercentAlpha);
        canvas.drawText(percentTipsStr,
                -mPercentTextPaint.measureText(percentTipsStr) / 2F,
                isDrawDown ?
                        percentHeight + percentStrMarginDot + percentTipsStrMarginPercentStr + percentTipsHeight
                        : -percentStrMarginDot - percentHeight - percentTipsStrMarginPercentStr,
                mPercentTextPaint);
    }

    private void drawPointerLine(Canvas canvas) {
        mPointerLinePaint.setAlpha(mPointerLineAndPercentAlpha);
        canvas.save();
        canvas.translate(0, dp2px(15));
        canvas.rotate(mPointerLineAngle, 0, -dp2px(15));
        canvas.drawLine(0, 0, 0, -mHeight / 2F - dp2px(18) + mAgeTextOffset, mPointerLinePaint);
        canvas.restore();
    }

    private void drawCenterCircle(Canvas canvas) {
        mCenterCirclePaint.setAlpha(mPointerLineAndPercentAlpha);
        canvas.drawCircle(0, 0, mCenterCircleRadius, mCenterCirclePaint);
    }

    private void drawAgeText(Canvas canvas) {
        float angle = 360F / mAgeTextList.size();
        float ageTextCircleRadius = mWidth / 2F - mAgeTextOffset;
        for (int i = 0; i < mAgeTextList.size(); i++) {
            String ageText = mAgeTextList.get(i);
            if (Integer.parseInt(ageText) > mCurrentAge) {
                mAgeTextPaint.setColor(mAgeTextColor);
            } else {
                mAgeTextPaint.setColor(mPastAgeTextColor);
            }
            float x = ageTextCircleRadius * (float) Math.sin(angle * Math.PI / 180 * i) - mAgeTextPaint.measureText(ageText) / 2F;
            Paint.FontMetrics fontMetrics = mAgeTextPaint.getFontMetrics();
            float offsetY = (fontMetrics.top + fontMetrics.bottom) / 2F;
            float y = -ageTextCircleRadius * (float) Math.cos(angle * Math.PI / 180 * i) - offsetY;
            canvas.drawText(ageText, x, y, mAgeTextPaint);
            if (i == mAgeTextList.size() - 1) {
                int bitmapHeight = mEditBitmap.getHeight();
                float textHeight = fontMetrics.bottom - fontMetrics.top;
                float textWidth = mAgeTextPaint.measureText(ageText);
                float scaleValue = textHeight / 3 / bitmapHeight;
                mMatrix.reset();
                mMatrix.postScale(scaleValue, scaleValue);
                mMatrix.postTranslate(x + textWidth, y - textHeight / 3);
                canvas.drawBitmap(mEditBitmap, mMatrix, null);

                RectF clickRectF = new RectF(x, y + fontMetrics.bottom - textHeight, x + textWidth, y + fontMetrics.bottom);
                mClickRectWrapperList.add(new LifeClockViewClickRectWrapper(clickRectF, LifeClockViewClickRectWrapper.Type.EDIT));
            }
        }
    }

    private void drawZeroAgeDownDot(Canvas canvas) {
        mAgeTextPaint.getTextBounds(mAgeTextList.get(0), 0, 1, mTextHeightRect);
        float circleCenterX = 0;
        float circleCenterY = -mHeight / 2F + mAgeTextOffset +
                (mTextHeightRect.bottom - mTextHeightRect.top) / 2F +
                mZeroAgeDownCircleRadius +
                mZeroAgeDownCircleOffset;

        canvas.drawCircle(circleCenterX, circleCenterY, mZeroAgeDownCircleRadius, mZeroAgeDownCirclePaint);

        // 出生日说明矩形区域宽度
        float zeroAgeTipsRectWidth = dp2px(DEFAULT_ZERO_AGE_TIPS_RECT_WIDTH);
        // 出生日说明矩形区域高度
        float zeroAgeTipsRectHeight = dp2px(DEFAULT_ZERO_AGE_TIPS_RECT_HEIGHT);
        if (mIsShowZeroAgeTips) {
            mZeroAgeTipsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mZeroAgeTipsPaint.setColor(mZeroAgeTipsRectColor);
            mZeroAgeTipsRectF.left = circleCenterX - zeroAgeTipsRectWidth / 2F;
            mZeroAgeTipsRectF.top = circleCenterY - mZeroAgeDownCircleRadius - zeroAgeTipsRectHeight;
            mZeroAgeTipsRectF.right = circleCenterX + zeroAgeTipsRectWidth / 2F;
            mZeroAgeTipsRectF.bottom = circleCenterY - mZeroAgeDownCircleRadius;
            float zeroAgeTipsRectCorners = dp2px(DEFAULT_ZERO_AGE_TIPS_RECT_CORNERS);
            canvas.drawRoundRect(mZeroAgeTipsRectF, zeroAgeTipsRectCorners, zeroAgeTipsRectCorners, mZeroAgeTipsPaint);

            mZeroAgeTipsPaint.setStyle(Paint.Style.FILL);
            mZeroAgeTipsPaint.setColor(DEFAULT_ZERO_AGE_TIPS_TEXT_COLOR);

            // 出生日说明文字大小
            mZeroAgeTipsPaint.setTextSize(sp2px(DEFAULT_ZERO_AGE_TIPS_TEXT_SIZE));
            mZeroAgeTipsPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = mZeroAgeTipsPaint.getFontMetrics();
            float offset = (fontMetrics.bottom + fontMetrics.top) / 2F;
            float baseLineY = mZeroAgeTipsRectF.centerY() - offset;
            String zeroAgeTipsText = "出生日";
            canvas.drawText(zeroAgeTipsText, mZeroAgeTipsRectF.centerX(), baseLineY, mZeroAgeTipsPaint);
        }

        RectF rectF = new RectF(circleCenterX - zeroAgeTipsRectWidth / 2F,
                circleCenterY - mZeroAgeDownCircleRadius - zeroAgeTipsRectHeight,
                circleCenterX + zeroAgeTipsRectWidth / 2F,
                circleCenterY + mZeroAgeDownCircleRadius + dp2px(10));
        mClickRectWrapperList.add(new LifeClockViewClickRectWrapper(rectF, LifeClockViewClickRectWrapper.Type.ZERO_CIRCLE));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mOriginalXY[0] = event.getX();
                mOriginalXY[1] = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                mCanvasInvertMatrix.reset();
                mCanvasMatrix.invert(mCanvasInvertMatrix);
                mCanvasInvertMatrix.mapPoints(mInvertDownXY, mOriginalXY);
                for (LifeClockViewClickRectWrapper rectWrapper : mClickRectWrapperList) {
                    if (rectWrapper.getRect().contains(mInvertDownXY[0], mInvertDownXY[1])) {
                        if (rectWrapper.getType() == LifeClockViewClickRectWrapper.Type.EDIT &&
                                mOnEditClickListener != null) {
                            mOnEditClickListener.onClick();
                        } else if (rectWrapper.getType() == LifeClockViewClickRectWrapper.Type.ZERO_CIRCLE) {
                            mIsShowZeroAgeTips = !mIsShowZeroAgeTips;
                            if (mIsShowZeroAgeTips) {
                                vibrate(50);
                            }
                            invalidate();
                        }
                    }
                }
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mCanvasMatrix.postTranslate(mWidth / 2F, mHeight / 2F);
    }

    public void setCurrentAge(int currentAge) {
        // TODO: 2020/2/12 这里应该根据时间戳来计算比例和角度;在此为了方便,直接用年龄计算
        vibrate(50);
        this.mCurrentAge = currentAge;
        float percent = currentAge / Float.parseFloat(mAgeTextList.get(mAgeTextList.size() - 1));
        mPercentStr = (int) (percent * 100) + "%";
        // 需要减去最后一段(比如0~80,需要忽略80~0的角度)
        float angle = (360 - 360F / mAgeTextList.size()) * percent;
        this.mPointerLineAngleFinal = angle;
        float animStartAngle = isFirstCalc ? 0 : angle - 20 < 0 ? 0 : angle - 20;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(animStartAngle, angle);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPointerLineAngle = (float) animation.getAnimatedValue();
                mPointerLineAndPercentAlpha = (int) (255 * ((float) animation.getCurrentPlayTime() / (float) animation.getDuration()));
                // animation.getCurrentPlayTime()的值可能超出总时间
                if (mPointerLineAndPercentAlpha > 255) {
                    mPointerLineAndPercentAlpha = 255;
                }
                invalidate();
            }
        });

        valueAnimator.setDuration(1500);
        valueAnimator.start();
        this.isFirstCalc = false;
    }

    private void vibrate(long millisecond) {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millisecond, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(millisecond);
        }
    }

    public void setCurrentMaxAge(int currentMaxAge) {
        mCurrentMaxAge = currentMaxAge > MAX_AGE ? MAX_AGE : currentMaxAge;
    }

    private float sp2px(float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getResources().getDisplayMetrics());
    }

    private float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        mOnEditClickListener = onEditClickListener;
    }

    public interface OnEditClickListener {
        void onClick();
    }
}