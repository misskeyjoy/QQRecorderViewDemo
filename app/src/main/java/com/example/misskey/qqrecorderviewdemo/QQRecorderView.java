package com.example.misskey.qqrecorderviewdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by misskey on 15-10-27.
 */
public class QQRecorderView extends View implements AudioManager.AudioStateListener ,Runnable{
    private static final String TAG = "QQRecoder";
    /**
     * 播放的图片
     */
    private Bitmap mBitmapRecorder;
    /**
     * 删除的图片
     */
    private Bitmap mBitmapDelete;
    /**
     * 松手试听
     */
    private String mTextLisntener;
    /**
     * 按住说话
     */
    private String mTextHolder;
    /**
     * 松手取消发送
     */
    private String mTextCancle;
    /**
     * 是否要取消发送
     */
    private boolean isCancle;
    /**
     * 是否想要播放
     */
    private boolean isWantToListener;
    /**
     * 是否想要发送
     */
    private boolean isWantToSender;
    /**
     * 初始化和还原操作
     */
    private boolean isBack = true;

    private Paint mPaint;
    /**
     * 控件的宽度
     */
    private int mWidth;
    private int mHeight;
    /**
     *
     */
    private int mDeleteRadius;
    private int mCenteRadius;
    /**
     * 左边增大的幅度（半径）
     */
    private int mLeftIncreamtent;
    /**
     * 右边增大的幅度
     */
    private int mRightIncreamtent;
    /**
     * 是否长按
     */
    private boolean isLongClick;
    /**
     * 是否按下中央的园
     */
    private boolean isCenterDown;
    private GestureDetector mGestureDetecotr;
    private QQRecorderViewListener listener;
    private Path path = new Path();
    private Point mRecoderPoint;
    private Point mDeletePoint;
    private Point tempPoint;
    private Point mCenterPoint;
    private AudioManager maudioManager;
    /**
     * 录音的时间
     */
    private Timer mtime;
    /**
     * 开始录音
     */
    private boolean isBeginAudio;
    private boolean isPrepared;
    private int duration;

    public QQRecorderView(Context context) {
        this(context, null);
    }

    public QQRecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQRecorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.QQRecorderView, defStyleAttr, 0);
        int index = a.getIndexCount();
        for (int i = 0; i < index; i++) {
            int att = a.getIndex(i);
            switch (att) {
                case R.styleable.QQRecorderView_srcDelte:
                    mBitmapDelete = BitmapFactory.decodeResource(getResources(), a.getResourceId(att, 0));
                    break;
                case R.styleable.QQRecorderView_srcListener:
                    mBitmapRecorder = BitmapFactory.decodeResource(getResources(), a.getResourceId(att, 0));
                    break;
                case R.styleable.QQRecorderView_textCancle:
                    mTextCancle = a.getString(att);
                    break;
                case R.styleable.QQRecorderView_textHolder:
                    mTextHolder = a.getString(att);
                    break;
                case R.styleable.QQRecorderView_textListener:
                    mTextLisntener = a.getString(att);
                default:
                    break;
            }
        }
        a.recycle();
        mBitmapRecorder = Bitmap.createScaledBitmap(mBitmapRecorder, 30, 30, true);
        mBitmapDelete = Bitmap.createScaledBitmap(mBitmapDelete, 30, 30, true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        path = new Path();
        mDeleteRadius = mBitmapRecorder.getWidth();
        mGestureDetecotr = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                LogUtils.loge("onDonw");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                LogUtils.loge("onShonwPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                LogUtils.loge("onSigleTapUp");
                isCenterDown = false;
                isLongClick = false;
                postInvalidate();
                if (listener != null) {
                    listener.cancleClick();
                }
                mLeftIncreamtent = 0;
                mRightIncreamtent = 0;
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                LogUtils.loge("onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                isCenterDown = isCenteRanger(e.getX(), e.getY());
                isLongClick = true;
                oldPoint.x = e.getX();
                oldPoint.y = e.getY();
                postInvalidate();
                maudioManager.prepareAudio();
                LogUtils.loge("onLongPress");
                if (listener != null) {
                    listener.onLongClick();
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                LogUtils.loge("OnFling");
                return false;
            }
        });
        Log.e(TAG, mCenteRadius + "");
        //TODo check SD state
        maudioManager=AudioManager.getIntance(Environment.getExternalStorageDirectory()+"/QQRecorder");
        maudioManager.setOnAudioStateListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        mWidth= Math.min(getMeasuredHeight(), getMeasuredWidth());
//        setMeasuredDimension(mWidth, mWidth);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredWidth();
        mCenteRadius = Math.round(mHeight * 0.15f);
        mDeletePoint = new Point(mWidth * 1.0f / 2 + (0.3f * mWidth) + mBitmapDelete.getWidth() * 1.0f / 2,
                mHeight / 2 - mHeight * 0.1f + mBitmapDelete.getHeight() / 2);
        mRecoderPoint = new Point(mWidth / 2 - (0.3f * mWidth) + mBitmapRecorder.getWidth() / 2,
                mHeight / 2 - mHeight * 0.1f + mBitmapRecorder.getHeight() / 2);
        mCenterPoint = new Point(getMeasuredWidth() / 2, getMeasuredHeight() / 2);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, getMeasuredHeight() + " " + getMeasuredWidth());
        /**
         *
         */
        if (isCenterDown && isLongClick) {

//            开始画弧线 曲线 没有没思路
            tempPoint = computeCicleCenter(mDeletePoint, mCenterPoint);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            RectF mRectF = new RectF(mCenterPoint.x, mDeletePoint.y, mCenterPoint.x, mCenterPoint.y);
            canvas.drawArc(mRectF, 0, 180, false, mPaint);
//

            mPaint.setColor(Color.rgb(244, 255, 244));
            canvas.drawCircle(mDeletePoint.x, mDeletePoint.y, mDeleteRadius + mRightIncreamtent, mPaint);
            canvas.drawBitmap(mBitmapDelete, mWidth / 2 + (mWidth * 0.3f), mHeight / 2 - 0.1f * mHeight, mPaint);
//        canvas.drawBitmap(mBitmapRecorder,);

            canvas.drawCircle(mRecoderPoint.x, mRecoderPoint.y, mDeleteRadius + mLeftIncreamtent, mPaint);
            canvas.drawBitmap(mBitmapRecorder, mWidth / 2 - mWidth * 0.3f, mHeight / 2 - 0.1f * mHeight, mPaint);
            mPaint.setColor(Color.parseColor("#9F79EE"));
            canvas.drawCircle(mWidth / 2, mHeight / 2, mCenteRadius, mPaint);

        } else {
            mPaint.setColor(Color.parseColor("#9FB6CD"));
            Paint paint = new Paint();
            mPaint.setTextSize(ActivityUtil.dip2px(getContext(), 14));
            canvas.drawCircle(mWidth / 2, mHeight / 2, mCenteRadius, mPaint);
            mPaint.setColor(Color.BLACK);
            Rect rect = new Rect();
            mPaint.getTextBounds(mTextHolder, 0, mTextHolder.length(), rect);
            int mTextWidth = rect.right - rect.left;
            canvas.drawText(mTextHolder, mWidth / 2 - mTextWidth / 2, mHeight * 0.3f, mPaint);
        }

    }

    private Point oldPoint = new Point(0, 0);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isIntercpt = mGestureDetecotr.onTouchEvent(event);
        if (isIntercpt) {
            return true;
        }
        oldPoint.x = event.getX();
        oldPoint.y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isCenterDown = isCenteRanger(event.getX(), event.getY());
                LogUtils.loge(isCenterDown + "");
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtils.loge("Move");
                if (isLongClick) {
                    if (!isCenteRanger(event.getX(), event.getY())) {
                        //滑动到左端
                        if (mCenterPoint.x > event.getX()) {
                            if (event.getX() >= oldPoint.x)
                                mLeftIncreamtent++;
                            else {
                                if (mLeftIncreamtent >= 1) {
                                    mLeftIncreamtent--;
                                }
                            }
                        }
                    }
                    if (event.getX() >= mCenterPoint.x) {
                        if (event.getX() < oldPoint.x) {
                            if (event.getX() > oldPoint.x) {
                                mRightIncreamtent++;
                            } else {
                                if (mLeftIncreamtent >= 1) {
                                    mRightIncreamtent--;
                                }
                                mRightIncreamtent--;
                            }
                        }

                    }
                    oldPoint.x = event.getX();
                    oldPoint.y = event.getY();

                }
                postInvalidate();


                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isIntercpt = false;
                isCenterDown = false;
                isLongClick = false;
                postInvalidate();
                if (listener != null) {
                    listener.cancleClick();
                }
                if(isPrepared==false){
                    //还没有准备好
                    LogUtils.loge("还没有准备好");
                    maudioManager.cancle();
                }else{
                    maudioManager.releas();
                    if(listener!=null){
                        listener.completeSucess(maudioManager.getmCurrentFilePath());
                    }
                }
                mLeftIncreamtent = 0;
                mRightIncreamtent = 0;
                oldPoint.x = 0;
                oldPoint.y = 0;
                break;
        }

        return true;
    }

    public boolean isCenteRanger(float x, float y) {
        float dx = Math.abs(mWidth / 2 - x);
        float dy = Math.abs(mHeight / 2 - y);
        float distance = (int) Math.sqrt(dx * dx + dy * dy);
        LogUtils.loge(distance);
        if (distance <= mCenteRadius) {
            return true;
        }
        return false;
    }

    public void setRecoderViewListener(QQRecorderViewListener qqRecorderViewListener) {
        this.listener = qqRecorderViewListener;
    }

    /**
     * 两点计算圆心
     *
     * @param start
     * @param start
     * @return
     */
    public Point computeCicleCenter(Point start, Point end) {
        float x = (start.x + end.x) / 2;
        float y = (start.y + end.y) / 2;
        Point point = new Point(x, y);
        return point;
    }

    @Override
    public void wellPrepared() {
        isPrepared=true;
        //可以开始 计算录制的时间

    }

    @Override
    public void run() {
        duration++;
    }

    public interface QQRecorderViewListener {
        /**
         * 当长按回到该接口
         */
        public void onLongClick();

        /**
         * 当松手后回调该接口
         */
        public void cancleClick();

        public void completeSucess(String file);
    }

    class Point {
        public float x;
        public float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
