package com.cchip.charge2.weidge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.cchip.charge2.R;
import com.cchip.charge2.utils.AppUtils;

import static java.lang.Math.PI;


public class CircleSeekBar extends View {
    /**
     * 保存状态
     */
    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";

    /***
     * 事件监听
     */
    private OnCircleSeekBarChangeListener mOnCircleSeekBarChangeListener;
    private Paint mColorWheelPaint;
    private Paint mPointerHaloPaint;
    private Paint mPointerColor;
    private int mColorWheelStrokeWidth = 60;
    private final int mPointerRadius = 20;
    private RectF mColorWheelRectangle = new RectF();
    private boolean mUserIsMovingPointer = false;
    private float mTranslationOffset;
    private float mColorWheelRadius;
    private float mAngle;
    private String text;
    private int conversion = 0;
    private int max = 100;
    private String color_attr;
    private SweepGradient s;
    private Paint mArcColor;
    private String wheel_color_attr, wheel_unactive_color_attr,
            pointer_color_attr, pointer_halo_color_attr;
    private int init_position;
    private boolean block_end = false;
    private float lastX;
    private int last_radians = 0;
    private boolean block_start = false;

    private int arc_finish_radians = 270;
    // 左下角开始
    private int start_arc = 135;

    private float[] pointerPosition;
    private Paint mColorCenterHalo;
    private RectF mColorCenterHaloRectangle = new RectF();
    private int end_wheel;

    private boolean show_text = false;
    private Handler mHandler = new Handler();
    private PathDashPathEffect effect;

    public CircleSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HoloCircleSeekBar, defStyle, 0);
        initAttributes(a);
        a.recycle();
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        Path path = new Path();
        RectF rect1 = new RectF(0, 0, 10, 20);
        path.addRoundRect(rect1, 10, 10, Path.Direction.CCW);
        double yuan = getWidth() * PI;
        double v = yuan / 100d;
        PathDashPathEffect effect = new PathDashPathEffect(path, (float) v, 0, PathDashPathEffect.Style.ROTATE);
        mColorWheelStrokeWidth = AppUtils.Dp2Px(13);
        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setColor(Color.GRAY);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelStrokeWidth);
        mColorWheelPaint.setPathEffect(effect);

        mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorCenterHalo.setColor(Color.CYAN);
        mColorCenterHalo.setAlpha(0xCC);
        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerHaloPaint.setColor(getResources().getColor(R.color.color_main));
        mPointerHaloPaint.setStrokeWidth(mPointerRadius + 50);
        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setStrokeWidth(mPointerRadius);
        mPointerColor.setColor(Color.BLUE);
        mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcColor.setColor(getResources().getColor(R.color.color_main));

        mArcColor.setPathEffect(effect);
        mArcColor.setStyle(Paint.Style.STROKE);
        mArcColor.setStrokeWidth(mColorWheelStrokeWidth);
        arc_finish_radians = (int) calculateAngleFromText(init_position) - 90;
        if (arc_finish_radians > end_wheel)
            arc_finish_radians = end_wheel;
        mAngle = calculateAngleFromRadians(arc_finish_radians > end_wheel ? end_wheel : arc_finish_radians);
        text = String.valueOf(calculateTextFromAngle(arc_finish_radians));
        invalidate();

    }

    private void initAttributes(TypedArray a) {
        max = a.getInteger(R.styleable.HoloCircleSeekBar_max, 100);
        color_attr = a.getString(R.styleable.HoloCircleSeekBar_default_color);
        wheel_color_attr = a.getString(R.styleable.HoloCircleSeekBar_wheel_active_color);
        wheel_unactive_color_attr = a.getString(R.styleable.HoloCircleSeekBar_wheel_unactive_color);
        pointer_color_attr = a.getString(R.styleable.HoloCircleSeekBar_pointer_color);
        pointer_halo_color_attr = a.getString(R.styleable.HoloCircleSeekBar_pointer_halo_color);
        a.getString(R.styleable.HoloCircleSeekBar_text_color);
        a.getInteger(R.styleable.HoloCircleSeekBar_text_size, 95);
        init_position = a.getInteger(R.styleable.HoloCircleSeekBar_init_position, 0);
        start_arc = a.getInteger(R.styleable.HoloCircleSeekBar_start_angle, 0);
        end_wheel = a.getInteger(R.styleable.HoloCircleSeekBar_end_angle, 360);
        show_text = a.getBoolean(R.styleable.HoloCircleSeekBar_show_text, true);
        last_radians = end_wheel;
        if (init_position < start_arc)
            init_position = calculateTextFromStartAngle(start_arc);
        if (color_attr != null) {
            try {
                Color.parseColor(color_attr);
            } catch (IllegalArgumentException e) {
            }
            Color.parseColor(color_attr);
        } else {
        }
        if (wheel_color_attr != null) {
            try {
                Color.parseColor(wheel_color_attr);
            } catch (IllegalArgumentException e) {
            }
        } else {
        }
        if (wheel_unactive_color_attr != null) {
            try {
                Color.parseColor(wheel_unactive_color_attr);
            } catch (IllegalArgumentException e) {
            }

        } else {
        }

        if (pointer_color_attr != null) {
            try {
                Color.parseColor(pointer_color_attr);
            } catch (IllegalArgumentException e) {
            }

        } else {
        }

        if (pointer_halo_color_attr != null) {
            try {
                Color.parseColor(pointer_halo_color_attr);
            } catch (IllegalArgumentException e) {
            }

        } else {
        }

    }

    public void setProgress(int pge) {
        init_position = pge;
        arc_finish_radians = (int) calculateAngleFromText(init_position) - 90;

        if (arc_finish_radians > end_wheel)
            arc_finish_radians = end_wheel;
        mAngle = calculateAngleFromRadians(arc_finish_radians > end_wheel ? end_wheel
                : arc_finish_radians);
        text = String.valueOf(calculateTextFromAngle(arc_finish_radians));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {



        mColorWheelPaint.setPathEffect(effect);
        mArcColor.setPathEffect(effect);

        canvas.translate(mTranslationOffset, mTranslationOffset);

        // 滑过的弧
        canvas.drawArc(mColorWheelRectangle, start_arc + 270, end_wheel
                - (start_arc), false, mColorWheelPaint);

        // 背景弧
        canvas.drawArc(mColorWheelRectangle, start_arc + 270,
                (arc_finish_radians) > (end_wheel) ? end_wheel - (start_arc)
                        : arc_finish_radians - start_arc, false, mArcColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        mTranslationOffset = min * 0.5f;
        mColorWheelRadius = mTranslationOffset - mPointerRadius;

        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
                mColorWheelRadius, mColorWheelRadius);

        mColorCenterHaloRectangle.set(-mColorWheelRadius / 2,
                -mColorWheelRadius / 2, mColorWheelRadius / 2,
                mColorWheelRadius / 2);

        pointerPosition = calculatePointerPosition(mAngle);
        Path path = new Path();
        mColorWheelStrokeWidth = AppUtils.Dp2Px(13);
        int dp2Px = AppUtils.Dp2Px(3);
        RectF rect1 = new RectF(0, 0, dp2Px, mColorWheelStrokeWidth);
        path.addRoundRect(rect1, 5, 5, Path.Direction.CCW);
        double yuan = ((mColorWheelRectangle.right - mColorWheelRectangle.left)) * PI;
        float v = (float) (yuan / 100);
        effect = new PathDashPathEffect(path,  v, 0, PathDashPathEffect.Style.ROTATE);

    }

    private int calculateTextFromAngle(float angle) {
        float m = angle - start_arc;

        float f = (float) ((end_wheel - start_arc) / m);

        return (int) (max / f);
    }

    private int calculateTextFromStartAngle(float angle) {
        float m = angle;

        float f = (float) ((end_wheel - start_arc) / m);

        return (int) (max / f);
    }

    private double calculateAngleFromText(int position) {
        if (position == 0 || position > max)
            return (float) 90;

        double f = (double) max / (double) position;

        double f_r = 360 / f;

        double ang = f_r + 90;

        return ang;

    }

    private int calculateRadiansFromAngle(float angle) {
        float unit = (float) (angle / (2 * PI));
        if (unit < 0) {
            unit += 1;
        }
        int radians = (int) ((unit * 360) - ((360 / 4) * 3));
        if (radians < 0)
            radians += 360;
        return radians;
    }

    private float calculateAngleFromRadians(int radians) {
        return (float) (((radians + 270) * (2 * PI)) / 360);
    }

    public int getValue() {
        return conversion;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Convert coordinates to our internal coordinate system
//        float x = event.getX() - mTranslationOffset;
//        float y = event.getY() - mTranslationOffset;
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                // Check whether the user pressed on (or near) the pointer
//                mAngle = (float) java.lang.Math.atan2(y, x);
//
//                block_end = false;
//                block_start = false;
//                mUserIsMovingPointer = true;
//
//                arc_finish_radians = calculateRadiansFromAngle(mAngle);
//
//                if (arc_finish_radians > end_wheel) {
//                    arc_finish_radians = end_wheel;
//                    block_end = true;
//                }
//
//                if (!block_end && !block_start) {
//                    text = String
//                            .valueOf(calculateTextFromAngle(arc_finish_radians));
//                    pointerPosition = calculatePointerPosition(mAngle);
//                    invalidate();
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (mUserIsMovingPointer) {
//                    mAngle = (float) java.lang.Math.atan2(y, x);
//
//                    int radians = calculateRadiansFromAngle(mAngle);
//
//                    if (last_radians > radians && radians < (360 / 6) && x > lastX
//                            && last_radians > (360 / 6)) {
//
//                        if (!block_end && !block_start)
//                            block_end = true;
//
//                    } else if (last_radians >= start_arc
//                            && last_radians <= (360 / 4) && radians <= (360 - 1)
//                            && radians >= ((360 / 4) * 3) && x < lastX) {
//                        if (!block_start && !block_end)
//                            block_start = true;
//
//                    } else if (radians >= end_wheel && !block_start
//                            && last_radians < radians) {
//                        block_end = true;
//                    } else if (radians < end_wheel && block_end
//                            && last_radians > end_wheel) {
//                        block_end = false;
//                    } else if (radians < start_arc && last_radians > radians
//                            && !block_end) {
//                        block_start = true;
//                    } else if (block_start && last_radians < radians
//                            && radians > start_arc && radians < end_wheel) {
//                        block_start = false;
//                    }
//
//                    if (block_end) {
//
//                        arc_finish_radians = end_wheel - 1;
//                        text = String.valueOf(100);
//                        mAngle = calculateAngleFromRadians(arc_finish_radians);
//                        pointerPosition = calculatePointerPosition(mAngle);
//                    } else if (block_start) {
//
//                        arc_finish_radians = start_arc;
//                        mAngle = calculateAngleFromRadians(arc_finish_radians);
//                        text = String.valueOf(0);
//                        pointerPosition = calculatePointerPosition(mAngle);
//                    } else {
//                        // text = String.valueOf(calculateTextFromAngle(mAngle));
//                        arc_finish_radians = calculateRadiansFromAngle(mAngle);
//                        text = String
//                                .valueOf(calculateTextFromAngle(arc_finish_radians));
//                        pointerPosition = calculatePointerPosition(mAngle);
//                    }
//                    invalidate();
//                    if (mOnCircleSeekBarChangeListener != null)
//                        mOnCircleSeekBarChangeListener.onProgressChanged(this,
//                                Integer.parseInt(text), true);
//
//                    last_radians = radians;
//
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                mUserIsMovingPointer = false;
//                mHandler.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        CircleSeekBar.this.invalidate();
//                    }
//                }, 2000);
//
//                if (mOnCircleSeekBarChangeListener != null)
//                    mOnCircleSeekBarChangeListener.onStopTrackingTouch(this,
//                            Integer.parseInt(text));
//                break;
//        }
//        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
//            getParent().requestDisallowInterceptTouchEvent(true);
//        }
//        lastX = x;
//
//        return true;
//    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle The position of the pointer expressed as angle (in rad).
     * @return The coordinates of the pointer's center in our internal
     * coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        // if (calculateRadiansFromAngle(angle) > end_wheel)
        // angle = calculateAngleFromRadians(end_wheel);
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[]{x, y};
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloat(STATE_ANGLE, mAngle);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        mAngle = savedState.getFloat(STATE_ANGLE);
        arc_finish_radians = calculateRadiansFromAngle(mAngle);
        text = String.valueOf(calculateTextFromAngle(arc_finish_radians));
        pointerPosition = calculatePointerPosition(mAngle);

    }

    public void setOnSeekBarChangeListener(OnCircleSeekBarChangeListener l) {
        mOnCircleSeekBarChangeListener = l;
    }

    public interface OnCircleSeekBarChangeListener {

        public abstract void onProgressChanged(CircleSeekBar seekBar,
                                               int progress, boolean fromUser);

        public abstract void onStopTrackingTouch(CircleSeekBar seekBar,
                                                 int progress);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }
}