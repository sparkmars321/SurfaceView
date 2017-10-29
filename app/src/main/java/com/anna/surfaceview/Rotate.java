package com.anna.surfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class Rotate extends SurfaceView implements Callback, Runnable {


    private SurfaceHolder mHolder;
    private Canvas canvas;
    //绘制的线程
    private Thread t;
    //线程的开关
    private boolean isRunning;
    private String[] strs = new String[]{"单反相机", "iPad", "恭喜发财", "iPhone", "服装一套", "恭喜发财"};
    private int[] images = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.xialian, R.drawable.iphone, R.drawable.meizi, R.drawable.xialian};
    private int[] colors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};
    private int itemCount = 6;
    private Bitmap[] bitmap;
    /**
     * 整个盘块的范围
     */
    private RectF mRange = new RectF();

    /**
     * 整个盘块的直径
     */
    private int mRadius;

    /**
     * 绘制盘块的画笔
     */
    private Paint mArcPaint;

    /**
     * 绘制文本的画笔
     */
    private Paint mTextPanit;

    /**
     * 滚动的速度
     */
    private double mSpeed = 0;
    /**
     * 绘制的角度
     */
    private volatile float mStartAngle = 0;

    /**
     * 判断是否点击了停止按钮
     */
    private boolean isShouldEnd;

    /**
     * 转盘的中心位置
     */
    private int mCenter;

    /**
     * 这里我们的padding直接以paddingLeft为准
     */
    private int mPadding;

    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.bg2);

    private float mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20, getResources()
                    .getDisplayMetrics());

    public Rotate(Context context) {
        this(context, null);
    }

    public Rotate(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();

        mHolder.addCallback(this);

        // 可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);

        // 设置常量
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		System.out.println("宽度："+getMeasuredWidth()+"   高度："+getMeasuredHeight());
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
//		System.out.println("width="+width);
        mPadding = getPaddingLeft();
//		System.out.println("===================mPadding="+mPadding);
        // 直径
        mRadius = width - mPadding * 2;
        // 中心
        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 初始化绘制盘块的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        // 初始化绘制盘块的文本画笔
        mTextPanit = new Paint();
        mTextPanit.setColor(0xffffffff);
        mTextPanit.setTextSize(mTextSize);

        // 初始化盘块的绘制范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);

        //初始化图片
        bitmap = new Bitmap[itemCount];

        for (int i = 0; i < itemCount; i++) {
            bitmap[i] = BitmapFactory.decodeResource(getResources(), images[i]);
        }

        isRunning = true;

        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        isRunning = false;

    }

    @Override
    public void run() {
        // 不断进行绘制
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();

            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            canvas = mHolder.lockCanvas();

            if (canvas != null) {
                //绘制背景
                drawBg();

                //绘制盘块
                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / itemCount;

                for (int i = 0; i < itemCount; i++) {
                    mArcPaint.setColor(colors[i]);
                    //绘制盘块
                    canvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);

                    //绘制文本
                    drawText(tmpAngle, sweepAngle, strs[i]);

                    //绘制icon
                    drawIcon(tmpAngle, bitmap[i]);

                    tmpAngle += sweepAngle;
                }

                mStartAngle += mSpeed;

                //如果点击了停止按钮
                if (isShouldEnd) {
                    mSpeed -= 1;//递减
                }

                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                mHolder.unlockCanvasAndPost(canvas);
            }
        }

    }

    /**
     * 点击启动旋转
     */
    public void luckyStart(int index) {
        //计算每一项的角度
        float angle = 360 / itemCount;

        //计算每一项中奖范围（当前index）
        //1-->150~210
        //0-->210~270
        float from = 270 - (index
                + 1) * angle;
        float end = from + angle;

        //设置停下来需要旋转的距离
        float targetFrom = 4 * 360 + from;
        float targetEnd = 4 * 360 + end;

        float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
        float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);

        mSpeed = v1 + Math.random() * (v2 - v1);
        isShouldEnd = false;
    }

    /**
     * 停止
     */
    public void luckyEnd() {
        mStartAngle = 0;
        isShouldEnd = true;
    }


    /**
     * 转盘是否在旋转
     *
     * @return
     */
    public boolean isRuning() {
        return mSpeed != 0;
    }

    public boolean isShoundEnd() {
        return isShouldEnd;
    }

    /**
     * 绘制盘块图片
     *
     * @param tmpAngle
     * @param bitmap
     */
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 8;

        float angle = (float) ((tmpAngle + 360 / itemCount / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        //确定那个图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);

        canvas.drawBitmap(bitmap, null, rect, null);
    }

    /**
     * 绘制文本
     *
     * @param tmpAngle
     * @param sweepAngle
     * @param string
     */
    private void drawText(float tmpAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);

        // 利用水平偏移量让文字居中
        float textWidth = mTextPanit.measureText(string);
        int hOffset = (int) (mRadius * Math.PI / itemCount / 2 - textWidth / 2);

        //垂直偏移量
        int vOffset = mRadius / 2 / 6;

        canvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPanit);
    }

    private void drawBg() {
        canvas.drawColor(0xffffffff);
        canvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);
    }
}
