package com.example.mychardemo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 我的饼图
 * @author wangk
 */
public class MyPieChartView extends View {
	/** 标注最大文字长度为6 */
	private final int MAX_LABEL_SIZE = 6;
	
	/**
	 * 饼图适配器
	 */
	private BasePieAdapter myPieAdapter;
	
	/**
	 * 外圆画笔
	 */
	private Paint paint;
	/**
	 * 饼图上文字画笔
	 */
	private TextPaint circlePaint;
	/**
	 * 标注画笔
	 */
	private TextPaint labelPaint;
	/**
	 * 预留画笔
	 */
	private TextPaint notePaint;
	/** 装饰画笔 */
	private Paint decoratePaint;

	
	/**
	 * 饼图上文字颜色，默认为白色
	 */
	private int circleTextColor;
	
	/**
	 * 标注文字颜色
	 */
	private int lableTextColor;
	
	/**
	 * 图层合并模式
	 */
	private PorterDuffXfermode pdfMode;
	/**
	 * 当前控件中点坐标
	 */
	private Point centerCoord;
	/** 标注坐标游标 */
	private Point cursorLabelPoint;
	/** 标注方块边长 */
	private int labelSquareEdge;
	
	private RectF oval;
	private RectF transOval;
	private RectF bigOval;
	private RectF decorateOval;
	
	/** 饼图内半径,未指定会自适应半径 */
	private int innerRadius;
	/** 饼图半径 */
	private int radius;
	/**  外层圆装饰半径 */
	private int decorateRadius;
	/**  当前点击的扇形 */
	private int clickSector;
	/** 最长标注文字长度 */
	private int max_label_length;
	
	/** 标注文字大小 */
	private float noteTextSize;
	/** 扇形图上信息文字大小 */
	private float circleLabelTextSize;
	
	private Point click_down_point;
	
	private CopyOnWriteArrayList<MyPie> pies;
	private List<Integer> randColors;
	/**
	 * 是否显示标注
	 */
	private boolean isShowLabel;
	/** 是否设置随机色 */
	private boolean isRandColor;
	private int totalValue;
	// 提供给外部当前选择饼图的接口
	private OnPieSelectListener onPieSelectListener;
	

	public MyPieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawTools();
	}
	
	/**
	 * 初始化画笔工具
	 */
	private void initDrawTools() {
		
		oval = new RectF();
		transOval = new RectF();
		bigOval = new RectF();
		decorateOval = new RectF();
		paint = new Paint();
		circlePaint = new TextPaint();
		labelPaint = new TextPaint();
		centerCoord= new Point();
		notePaint = new TextPaint();
		bigOval = new RectF();
		click_down_point = new Point();
		decoratePaint = new Paint();
		cursorLabelPoint = new Point();
		pies = new CopyOnWriteArrayList<MyPie>();
		
		circlePaint.setTextSize(getCircleTextSize());
		labelPaint.setTextSize(getLabelTextSize());
		notePaint.setTextSize(getLabelTextSize());
		
		paint.setColor(Color.RED);
		circlePaint.setColor(getCircleTextColor());
		labelPaint.setColor(getLableTextColor());
		notePaint.setColor(Color.DKGRAY);
		decoratePaint.setColor(Color.argb(150, 255, 255, 255));
		
		paint.setAntiAlias(true); // 抗锯齿
		labelPaint.setAntiAlias(true);
		circlePaint.setAntiAlias(true);
		notePaint.setAntiAlias(true);
		decoratePaint.setAntiAlias(true);
		
		notePaint.setTextAlign(Align.CENTER);
		circlePaint.setTextAlign(Align.CENTER);
		
		notePaint.setFakeBoldText(true);
		circlePaint.setFakeBoldText(true);
		
		// init data
		clickSector = -1;
		isRandColor = false;
		isShowLabel = true;
		 pdfMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);// 两个图层，取上层非交集部分显示
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// 新建图层
		canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint, Canvas.ALL_SAVE_FLAG);
		// 没有数据的时候提醒
		if(myPieAdapter == null || totalValue <= 0) {
			canvas.drawText("it's not have data or value is 0", centerCoord.x, centerCoord.y, notePaint);
			canvas.restore();
			return;
		}
		
	//	canvas.drawColor(Color.TRANSPARENT);
		
		for (MyPie p : pies) {
			if(p.isTouch()) {
				p.drawSelf(canvas, bigOval, paint, circlePaint,radius,innerRadius);
			} else {
				p.drawSelf(canvas, oval, paint, circlePaint,radius,innerRadius);
			}
			// 画标注
			if(isShowLabel) {
				p.drawLabel(canvas, paint, labelPaint,labelSquareEdge,cursorLabelPoint);
			}
		}
		// 装饰圆
		canvas.drawArc(decorateOval, 0f, 360f, true, decoratePaint);
		
		paint.setXfermode(pdfMode); 
		
		canvas.drawArc(transOval, 0f, 360f, true, circlePaint);
		
		paint.setXfermode(null);
		/*
		 * 判断是否显示标注
		 */
		canvas.restore();
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 进行自适应适配
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if(widthSize > 0 && heightSize > 0) {
			// 取最大padding
			int maxPadding = Math.max(getPaddingLeft(), getPaddingRight()) > Math.max(getPaddingTop(), getPaddingBottom())?
					Math.max(getPaddingLeft(), getPaddingRight()):Math.max(getPaddingTop(), getPaddingBottom());
			if(maxPadding < widthSize/20) {
				// 默认有20/1宽度的padding
				maxPadding = widthSize/20;
			}
			if(isShowLabel) {
				labelSquareEdge = widthSize/35;
				maxPadding += labelSquareEdge+max_label_length*getLabelTextSize();
				centerCoord.x = widthSize/2-maxPadding/2;
				centerCoord.y = heightSize/2;
			} else {
				centerCoord.x = widthSize/2;
				centerCoord.y = heightSize/2;
			}
			
			radius = (widthSize > heightSize ? heightSize:widthSize)*7/15-maxPadding/2;
			innerRadius = (4*radius)/7; // 内圆半径默认为外圆的4/7
		} else {
			innerRadius = 0;
			radius = 0;
			centerCoord.x = 0;
			centerCoord.y = 0;
		}
		
		/*
		 * 计算矩阵
		 */
		int tempDistance = (radius-innerRadius)/4;
		decorateRadius = innerRadius<=0?0:innerRadius+tempDistance/2;
		
		oval.left = centerCoord.x-radius;
		oval.top = centerCoord.y-radius;
		oval.right = centerCoord.x+radius;
		oval.bottom = centerCoord.y+radius;
		
		transOval.left = centerCoord.x-innerRadius;
		transOval.top = centerCoord.y-innerRadius;
		transOval.right = centerCoord.x+innerRadius;
		transOval.bottom = centerCoord.y+innerRadius;
		
		bigOval.left = centerCoord.x-radius-tempDistance;
		bigOval.top = centerCoord.y-radius-tempDistance;
		bigOval.right = centerCoord.x+radius+tempDistance;
		bigOval.bottom = centerCoord.y+radius+tempDistance;
		
		decorateOval.left = centerCoord.x-decorateRadius;
		decorateOval.top = centerCoord.y-decorateRadius;
		decorateOval.right = centerCoord.x+decorateRadius;
		decorateOval.bottom = centerCoord.y+decorateRadius;	
		if(isShowLabel) {
			// tempDistance 在这里没有意义，只是为了能够离圆远点
			cursorLabelPoint.x = centerCoord.x +radius+tempDistance;
			cursorLabelPoint.y = centerCoord.y - radius;
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}
	
	/**
	 * 设置数据源
	 * @param myPieAdapter
	 */
	public void setAdapter(BasePieAdapter myPieAdapter){
		this.myPieAdapter = myPieAdapter;
		notifySetDataChanged();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			click_down_point.x = (int) event.getX();
			click_down_point.y = (int) event.getY();
			for (int i = 0; i < pies.size(); i++) {
				MyPie p = pies.get(i);
				if(p.isInArea(click_down_point)) {
					clickSector = i;
					if(onPieSelectListener != null) {
						onPieSelectListener.selectChanged(i);
					}
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		invalidate();
		return super.dispatchTouchEvent(event);
	}
	
	
	
	/**
	 * 刷新数据
	 */
	public void notifySetDataChanged() {
		if(myPieAdapter == null) {
			return;
			//throw new IllegalArgumentException("adapter is null!");
		}
		if(pies == null) {
			pies = new CopyOnWriteArrayList<MyPie>();
		}
		pies.clear();
		//initDrawTools();
		totalValue = 0;
		// 计算数据总值
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			totalValue += myPieAdapter.getItem(i).sectorValue;
		}
		float cursorAngle = 0f;
		PieModel p  = null;
		max_label_length = 0;
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			p = myPieAdapter.getItem(i);
			if(isRandColor) {
				p.sectorColor = getRandomColor();
			}
			if(max_label_length < p.sectorName.length()) {
				max_label_length =  p.sectorName.length();
			}
			float sweepAngle =  ((p.sectorValue/totalValue)*360f);
			pies.add(new MyPie(cursorAngle, sweepAngle, centerCoord,p,MyPie.LABEL_GRIVITY.DRAW_NONE_LABEL.navtieInt,i));
			cursorAngle = cursorAngle+ sweepAngle;
		}
		if(max_label_length > MAX_LABEL_SIZE) {
			max_label_length = MAX_LABEL_SIZE;
		}
		invalidate();
	}
	
	public int getCircleTextColor() {
		return circleTextColor == 0?Color.WHITE:circleTextColor;
	}
	
	public int getLableTextColor() {
		return lableTextColor == 0?Color.BLACK:lableTextColor;
	}
	
	/**
	 * 设置饼图内半径
	 */
	public void setInnerRaduis(int radius) {
		this.innerRadius = radius;
	}
	
	/**
	 * 是否显示标注
	 * @param flag
	 */
	public void isShowLabel(boolean flag) {
		this.isShowLabel = flag;
	}

	/**
	 * 获取数据源adapter
	 * @return
	 */
	public BasePieAdapter getAdapter() {
		return myPieAdapter;
	}
	
	public void setRandColor(boolean flag) {
		this.isRandColor = flag;
		notifySetDataChanged();
	}

	/**
	 * 获得一个随机颜色，并且唯一
	 * @return
	 */
	private int getRandomColor() {
		if(randColors == null) {
			randColors = new ArrayList<Integer>();
		}
		if(randColors.size() >= 16581375) {
			randColors.clear();
		}
		int r = (int) (Math.random()*255);
		int g = (int) (Math.random()*255);
		int b = (int) (Math.random()*255);
		
		int color = Color.rgb(r, g, b);
		if(!randColors.contains(color)) {
			randColors.add(color);
		} else {
			color = getRandomColor();
		}
		return color;
	}
	
	public void setLabelTextSize(float pixelSize) {
		this.noteTextSize = pixelSize;	
		notePaint.setTextSize(noteTextSize);
		invalidate();
	}
	
	public float getLabelTextSize() {
		return noteTextSize == 0f?30f:noteTextSize;
	}
	
	public void setCircleTextSize(float pixelSize) {
		this.circleLabelTextSize = pixelSize;
		circlePaint.setTextSize(circleLabelTextSize);
		invalidate();
	}
	
	public float getCircleTextSize() {
		return circleLabelTextSize == 0f?40f:circleLabelTextSize;
	}
	
	public void setShowLabel(boolean flag) {
		this.isShowLabel = flag;
		notifySetDataChanged();
	}
	
	public void setOnPieSelectListener(OnPieSelectListener onPieSelectListener) {
		this.onPieSelectListener = onPieSelectListener;
	}
	
	/**
	 * 提供给外部调用的扇形选择状态
	 * @author wangk
	 */
	public interface OnPieSelectListener{
		public void selectChanged(int position);
	}
}
