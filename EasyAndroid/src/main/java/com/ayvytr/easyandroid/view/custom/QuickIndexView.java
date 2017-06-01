package com.ayvytr.easyandroid.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ayvytr.easyandroid.R;
import com.ayvytr.easyandroid.tools.BitmapTool;
import com.ayvytr.easyandroid.tools.Colors;
import com.ayvytr.easyandroid.tools.withcontext.DensityTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Do on 2017/5/31.
 */

public class QuickIndexView extends View
{
    private static final int DEFAULT_WIDTH_DP = 50;

    private static final int NO_POSITION = -1;

    private Paint paint;

    private Toast toast;
    private boolean showToast;

    /**
     * 索引顶部图片
     */
    private Drawable topDrawable;

    /**
     * 索引底部图片
     */
    private Drawable bottomDrawable;

    private List<String> letterList;
    private int textColor;
    private int quickTextColor;
    private Context context;
    private OnLetterChangeListener onLetterChangeListener;

    private Rect bitmapRect;
    private Rect outRect;

    /**
     * {@link #toast} 显示的View，{@link Toast#setView(View)}
     */
    private RelativeLayout toastView;
    /**
     * {@link #toastView} 包含的 TextView，绘制文字，直接使用TextView会有问题，宽高不能限定成指定值，所以使用2个View配合.
     */
    private TextView toastTextView;

    /**
     * {@link #toast} 的背景
     */
    private Drawable quickBackground;

    /**
     * {@link #toast} 的宽度
     */
    private int quickWidth;

    /**
     * {@link #toast} 的高度
     */
    private int quickHeight;

    public QuickIndexView(Context context)
    {
        this(context, null);
    }

    public QuickIndexView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public QuickIndexView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs)
    {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);

        toastView = new RelativeLayout(context);
        toastView.setGravity(Gravity.CENTER);
        toast.setView(toastView);

        toastTextView = new TextView(context);
        toastTextView.setGravity(Gravity.CENTER);
        toastView.addView(toastTextView);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QuickIndexView);
        textColor = typedArray.getColor(R.styleable.QuickIndexView_textColor, Colors.BLACK);
        quickTextColor = typedArray.getColor(R.styleable.QuickIndexView_quickTextColor, Colors.BLACK);
        topDrawable = typedArray.getDrawable(R.styleable.QuickIndexView_topDrawable);
        bottomDrawable = typedArray.getDrawable(R.styleable.QuickIndexView_bottomDrawable);

        showToast = typedArray.getBoolean(R.styleable.QuickIndexView_showToast, true);
        quickBackground = typedArray.getDrawable(R.styleable.QuickIndexView_quickBackground);
        quickWidth = typedArray.getDimensionPixelSize(R.styleable.QuickIndexView_quickWidth,
                DensityTool.dp2px(context, DEFAULT_WIDTH_DP));
        quickHeight = typedArray.getDimensionPixelSize(R.styleable.QuickIndexView_quickHeight,
                DensityTool.dp2px(context, DEFAULT_WIDTH_DP));

        changeToastViewSize();

        CharSequence[] textArray = typedArray.getTextArray(R.styleable.QuickIndexView_quickLetters);
        if(textArray != null)
        {
            letterList = new ArrayList<>();
            for(CharSequence charSequence : textArray)
            {
                letterList.add(charSequence.toString());
            }
        }

        typedArray.recycle();

        bitmapRect = new Rect();
        outRect = new Rect();
    }

    /**
     * 设置 {@link #toastView} 和它包含的 {@link #toastTextView} 的宽高.
     * <p>
     * {@link #toastTextView} 宽高设置为 {@link #toastView} 的 0.8倍尺寸.
     * {@link #toastTextView} 文字高度定为 {@link #quickWidth} 和 {@link #quickHeight} 中最小值的 0.2倍数值。这个尺寸比较合理.
     */
    private void changeToastViewSize()
    {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(quickWidth, quickHeight);
        toastView.setLayoutParams(lp);
        toastView.setBackgroundDrawable(quickBackground);

        lp = new RelativeLayout.LayoutParams((int) (quickWidth * .8f), (int) (quickHeight * .8f));
        toastTextView.setLayoutParams(lp);

        float textSize = Math.min(quickWidth, quickHeight) * .2f;
        toastTextView.setTextSize(textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if(widthMode == MeasureSpec.AT_MOST)
        {
            width = DensityTool.dp2px(context, DEFAULT_WIDTH_DP);
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        paint.setColor(textColor);

        int letterLength = getLetterLength();
        paint.setTextSize(letterLength);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int y = getPaddingTop();

        int x = getWidth() >> 1;
        int halfLetterLength = letterLength >> 1;

        if(topDrawable != null)
        {
            Bitmap topBitmap = BitmapTool.toBitmap(topDrawable);
            bitmapRect.bottom = topBitmap.getHeight();
            bitmapRect.right = topBitmap.getWidth();
            outRect.left = x - halfLetterLength;
            outRect.right = x + halfLetterLength;
            outRect.top = y;
            outRect.bottom = y + letterLength;
            canvas.drawBitmap(topBitmap, bitmapRect, outRect, null);
            y += letterLength;
        }

        int fontY = (int) (halfLetterLength - fontMetrics.top / 2 - fontMetrics.bottom / 2);
        int size = letterList.size();
        for(int i = 0; i < size; i++)
        {
            canvas.drawText(letterList.get(i), x, y + fontY, paint);
            y += letterLength;
        }

        if(bottomDrawable != null)
        {
            Bitmap bottomBitmap = BitmapTool.toBitmap(bottomDrawable);
            bitmapRect.bottom = bottomBitmap.getHeight();
            bitmapRect.right = bottomBitmap.getWidth();
            outRect.left = x - halfLetterLength;
            outRect.right = x + halfLetterLength;
            outRect.top = y;
            outRect.bottom = y + letterLength;
            canvas.drawBitmap(bottomBitmap, bitmapRect, outRect, null);
        }
    }

    /**
     * 返回实际绘制的每个Letter文字高度，因为动态设置时，如果设置的文字高度太大，绘制出来也没有意义.
     *
     * @return 实际每个Letter文字高度
     */
    private int getLetterLength()
    {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int letterCount = getLetterCount();

        return Math.min(width, (getHeight() - getPaddingTop() - getPaddingBottom()) / letterCount);
    }

    private int getLetterCount()
    {
        int itemCount = letterList.size();
        if(topDrawable != null)
        {
            itemCount++;
        }
        if(bottomDrawable != null)
        {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //点击顶部或者底部Padding时，不响应事件
        if(event.getY() < getPaddingTop() || event.getY() > (getHeight() - getPaddingBottom()))
        {
            return true;
        }

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            {
                int letterCount = getLetterCount();
                int letterLength = getLetterLength();
                int index = 0;
                for(int i = 0; i < letterCount; i++)
                {
                    int y = getPaddingTop() + letterLength * i;
                    if(event.getY() > y && event.getY() < y + letterLength)
                    {
                        index = i;
                        break;
                    }
                }

                String letter;
                if(topDrawable != null)
                {
                    index--;
                }

                toastView.setBackgroundDrawable(quickBackground);

                if(index == NO_POSITION)
                {
                    index = 0;
                    letter = "";

                    toastTextView.setBackgroundDrawable(topDrawable);
                }
                else if(index >= letterList.size())
                {
                    index = letterList.size() - 1;
                    letter = "";

                    toastTextView.setBackgroundDrawable(topDrawable);
                }
                else
                {
                    letter = letterList.get(index);

                    toastTextView.setBackgroundDrawable(null);
                }

                toastTextView.setText(letter);

                if(showToast)
                {
                    toast.show();
                }

                if(onLetterChangeListener != null)
                {
                    onLetterChangeListener.onLetterChange(index, letter, this);
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                if(showToast)
                {
                    toast.cancel();
                }
                break;
        }

        return true;
    }

    public void setOnLetterChangeListener(OnLetterChangeListener onLetterChangeListener)
    {
        this.onLetterChangeListener = onLetterChangeListener;
    }

    /**
     * 字母索引变化监听器
     */
    public interface OnLetterChangeListener
    {
        /**
         * {@link #onTouchEvent(MotionEvent)} 触发时，调用此字母索引变化方法
         *
         * @param position       当前position.<br>如果指向 {@link #topDrawable}, {@code position=0}<br>
         *                       如果指向 {@link #bottomDrawable}, {@code position=} {@link #letterList} {@code .size()}.
         * @param text           当前指向的文本.<br>如果指向 {@link #topDrawable} 或者 {@link #bottomDrawable}，{@code text=""}.
         * @param quickIndexView {@link QuickIndexView}
         */
        void onLetterChange(int position, String text, QuickIndexView quickIndexView);
    }
}
