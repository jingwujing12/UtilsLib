package com.cchip.charge2.weidge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cchip.charge2.utils.AppUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class IBTLineView extends View {

    private Paint mPaint;
    private final static String TAG = "IBTLineView";
    private String mVerticalLineColor = "#ffA2A2A2";
    private String mHorizontalLineColor = "#ffA2A2A2";
    private String mBrokenLineColor = "#ffB6BD00";
    private String mDottedLineColor = "#FF4E4E4E";
    private String mTextColor = "#FFFFFFFF";
    private String mSmallGridColor = "#FF313131";
    private Paint mPaintDottedLine;
    private Paint mPaintText;
    private int width;
    private int height;
    private int startX = 50;
    private int startEndX = 20;
    private int mTextSize;
    private int days;
    private String mTextYs[] = new String[]{"0", "20", "40", "60", "80", "100"};
    private ArrayList<Point> points = new ArrayList<>();

    private String mTextXs[] = new String[]{};
    private float subHeight;
    private Paint mPaintBrokenLine;
    private float subWidth;
    private Date mStartdate;
    private String[] mTextXYs;
    private PathEffect effectsBroken;
    private int padding = 15;
    private float offset = 0;
    private float startTouchX;
    private float fastOffset;
    private float subMinWidth;
    private float SmallGridNumber = 12f;
    private float displayDays = 5f;
    private int mTextSizeVertical;

    public void setStartEndTextXs(String startTextX, String endTextX) {
        mStartdate = getTimeDate(startTextX);
        Date date1 = getTimeDate(endTextX);
        days = getSubDays(mStartdate, date1);
        this.mTextXs = new String[days + 1];
        for (int i = 0; i <= days; i++) {
//            if (i == 0) {
            mTextXs[i] = getDay(mStartdate, i) + "/" + getMonth(mStartdate);
//            } else {
//                mTextXs[i] = getDay(mStartdate, i) + "";
//            }
        }
        invalidate();
    }

    private int getSubDays(Date date, Date date1) {
        if (date == null || date1 == null) {
            return 0;
        }
        return (int) ((date1.getTime() - date.getTime()) / (1000 * 3600 * 24));
    }

    private int getSubHour(Date date, Date date1) {
        if (date == null || date1 == null) {
            return 0;
        }
        return (int) ((date1.getTime() - date.getTime()) / (1000 * 3600));
    }

    /**
     * 获取日
     *
     * @return
     */
    public static int getDay(Date date, int addDay) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.DAY_OF_MONTH, addDay);// 今天+1天
        return cd.get(Calendar.DAY_OF_MONTH);
    }

    public static int getHour(Date date, int addDay) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.HOUR_OF_DAY, addDay);// 今天+1小时
        return cd.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取月
     *
     * @return
     */
    public static int getMonth(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        return cd.get(Calendar.MONTH) + 1;
    }

    public Date getTimeDate(String str) {
        // 创建 Calendar 对象
        long l = Long.parseLong(str);
        Date date = new Date();
        date.setTime(l);

        return date;
    }
//    private Date getTimeDate(String strTime) {
//        long returnMillis = 0;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date d = null;
//        try {
//            d = sdf.parse(strTime);
////            returnMillis = d.getTime();
//        } catch (ParseException e) {
//        }
//        return d;
//    }

    public void setPoints(String[] mTextXYs) {
        this.mTextXYs = mTextXYs;
        invalidate();
    }

    private void initPoits(String[] mTextXYs) {
        if (mTextXYs == null) {
            return;
        }
        points.clear();
        for (int i = 0; i < mTextXYs.length; i++) {
            String[] split = mTextXYs[i].split("\\-");
            String x = split[0];
            String y = split[1];
            Point paint = new Point();
            Date timeDate = getTimeDate(x);
            int subDays = getSubHour(mStartdate, timeDate);

            paint.x = subDays;
            paint.y = Integer.parseInt(y);
            points.add(paint);
        }
    }


    public IBTLineView(Context context) {
        this(context, null);
    }

    public IBTLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IBTLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        points = new ArrayList<>();
        startX = AppUtils.Dp2Px(40);
        mTextSize = AppUtils.Dp2Px(11);
        mTextSizeVertical = AppUtils.Dp2Px(9);
        padding = AppUtils.Dp2Px(5);
        int dp = AppUtils.Dp2Px(1.5f);
        int w = AppUtils.Dp2Px(1);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(w / 2f);
        mPaint.setColor(Color.parseColor(mVerticalLineColor));

        mPaintDottedLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDottedLine.setStyle(Paint.Style.FILL);
        mPaintDottedLine.setAntiAlias(true);
        mPaintDottedLine.setStrokeWidth(w / 2f);
        mPaintDottedLine.setColor(Color.parseColor(mDottedLineColor));

        mPaintBrokenLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBrokenLine.setStyle(Paint.Style.STROKE);
        mPaintBrokenLine.setAntiAlias(true);
        mPaintBrokenLine.setStrokeWidth(w);
        effectsBroken = new DashPathEffect(new float[]{dp, dp}, 0);
        mPaintBrokenLine.setPathEffect(effectsBroken);
        mPaintBrokenLine.setColor(Color.parseColor(mBrokenLineColor));

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setAntiAlias(true);
        mPaintText.setStrokeWidth(1);
        mPaintText.setTextSize(mTextSize);
        mPaintText.setColor(Color.parseColor(mTextColor));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initData(canvas);
        initPoits(mTextXYs);
        onDrawHorizontalText(canvas);
        onDrawVerticalText(canvas);
        onDrawBrokenLines(canvas);
        onDrawVerticalLine(canvas);
        onDrawHorizontalLine(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float w = getWidth() - padding - startEndX;
        float v = (w - startX) / (displayDays - 1);
        offset = -v * 5;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float w = getWidth() - padding - startEndX;
        float v = (w - startX) / (displayDays - 1);
        offset = -v * 5;
        postInvalidate();
    }

    private void initData(Canvas canvas) {
        width = canvas.getWidth() - padding - startEndX;
        height = canvas.getHeight() - startX / 2 - padding;
        if (mTextYs.length > 0) {
            subHeight = height / (mTextYs.length);
        }
        if (mTextXs.length > 0) {
            if (mTextXs.length <= displayDays) {
                subWidth = (width - startX) / (mTextXs.length - 1);
            } else {
                subWidth = (width - startX) / (displayDays - 1);
            }
            subMinWidth = subWidth / SmallGridNumber;
        }
    }

    private void onDrawBrokenLines(Canvas canvas) {
        canvas.save();
        float subh = (height - subHeight) / 100f;
        Bitmap srcRoundBitmap = Bitmap.createBitmap(width - startX, getHeight(), Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(srcRoundBitmap);
        for (int i = 0; i < points.size() - 1; i++) {
            Point point = points.get(i);
            Point point2 = points.get(i + 1);
            int x = point.x;
            int x1 = point2.x;
            if (x1 - x < 3) {
                mPaintBrokenLine.setColor(Color.WHITE);
                mPaintBrokenLine.setPathEffect(null);
            } else {
                mPaintBrokenLine.setColor(Color.parseColor(mBrokenLineColor));
                mPaintBrokenLine.setPathEffect(effectsBroken);
            }
            float startWidth = startX + subMinWidth * x / 2 + offset;
            float endWidth = startX + subMinWidth * x1 / 2 + offset;

            srcCanvas.drawLine(startWidth, (height - point.y * subh), endWidth, (height - point2.y * subh), mPaintBrokenLine);
        }
        canvas.drawBitmap(srcRoundBitmap, startX, 0, mPaint);
        canvas.restore();
    }

    private void onDrawHorizontalText(Canvas canvas) {
        canvas.save();
        if (mTextYs.length <= 0) {
            return;
        }
        mPaintText.setTextSize(mTextSize);
        for (int i = 0; i < mTextYs.length; i++) {
            float textHeight = height - subHeight * i;
            canvas.drawText(mTextYs[i], (startX - padding) - mTextYs[i].length() * mPaintText.getTextSize() / 2, textHeight + mTextSize / (1.9f), mPaintText);
            if (i > 0) {
                canvas.drawLine(startX, textHeight, width, textHeight, mPaintDottedLine);
            }
        }
        canvas.drawLine(startX, subHeight / 2f, width, subHeight / 2f, mPaintDottedLine);
        canvas.restore();
    }

    private void onDrawVerticalText(Canvas canvas) {
        canvas.save();
        mPaintText.setTextSize(mTextSizeVertical);
        mPaint.setColor(Color.parseColor(mSmallGridColor));
        for (int i = 0; i < mTextXs.length; i++) {
            float textWidth = startX + subWidth * i;
            float sw = mTextXs[i].length() * mPaintText.getTextSize() / 2f;
            float x = textWidth - sw / 2f + offset;
            if (x >= startX - padding - mPaintText.getTextSize() && x <= width + sw) {
                canvas.drawText(mTextXs[i] + "", x, height + mPaintText.getTextSize() + padding, mPaintText);
            }
            for (int i1 = 0; i1 < SmallGridNumber; i1++) {
                float startLineX = textWidth + subMinWidth * i1 + offset;
                if (startLineX >= startX && startLineX <= width) {
                    canvas.drawLine(startLineX, subHeight / 2, startLineX, height, mPaint);
                }
            }
        }
        canvas.drawLine(width, subHeight / 2, width, height, mPaint);
        canvas.restore();
    }

    private void onDrawHorizontalLine(Canvas canvas) {
        canvas.save();
        mPaint.setColor(Color.parseColor(mHorizontalLineColor));
        canvas.drawLine(startX, subHeight / 2, startX, height, mPaint);
        canvas.restore();
    }

    private void onDrawVerticalLine(Canvas canvas) {
        canvas.save();
        mPaint.setColor(Color.parseColor(mVerticalLineColor));
        canvas.drawLine(startX, height, width, height, mPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouchX = event.getX();
                fastOffset = offset;
                if (startTouchX > startX && startTouchX < width) {

                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float v = event.getX() - startTouchX;
                offset = fastOffset + v;
                if (offset >= 0) {
                    offset = 0;
                } else if (offset <= -subWidth * displayDays) {
                    offset = -subWidth * displayDays;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;

    }
}
