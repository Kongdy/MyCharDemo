package com.example.mychardemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
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
	
	private RectF oval;
	private RectF transOval;
	private RectF bigOval;
	
	/**
	 * 饼图内半径
	 * 未指定会自适应半径
	 */
	private int innerRadius;
	/** 饼图半径 */
	private int radius;
	/**  当前点击的扇形 */
	private int clickSector;
	private  Point cursorP;
	
	private Point click_down_point;
	private Point click_up_point;
	
	/**
	 * 是否显示标注
	 */
	private boolean isShowLabel;
	
	private int totalValue;
	

	public MyPieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawTools();
	}
	
	/**
	 * 初始化画笔工具
	 */
	private void initDrawTools() {
		
		paint = new Paint();
		circlePaint = new TextPaint();
		labelPaint = new TextPaint();
		centerCoord= new Point();
		notePaint = new TextPaint();
		bigOval = new RectF();
		click_down_point = new Point();
		click_up_point = new Point();
		
		paint.setColor(Color.RED);
		circlePaint.setColor(getCircleTextColor());
		circlePaint.setTextSize(30);
		circlePaint.setTextAlign(Align.CENTER);
		labelPaint.setColor(getLableTextColor());
		notePaint.setColor(Color.DKGRAY);
		paint.setAntiAlias(true); // 抗锯齿
		labelPaint.setAntiAlias(true);
		circlePaint.setAntiAlias(true);
		notePaint.setAntiAlias(true);
		notePaint.setTextAlign(Align.CENTER);
		notePaint.setFakeBoldText(true);
		
		// init data
		clickSector = -1;
		 cursorP = new Point();
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
		
		canvas.drawColor(Color.TRANSPARENT);
		float cursorAngle = 0f; // 游标角度
		 PieModel p  = null;
		
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			  p = myPieAdapter.getItem(i);
			float sweepAngle =  ((p.sectorValue/totalValue)*360f);
			paint.setColor(p.sectorColor);
			if(i == clickSector) {
				canvas.drawArc(bigOval, cursorAngle, sweepAngle, true, paint);
			//	canvas.drawText(p.sectorName, x, y, circlePaint);
			} else {
				canvas.drawArc(oval, cursorAngle, sweepAngle, true, paint);
			}
			cursorAngle =cursorAngle+ sweepAngle;
		}
	
		paint.setXfermode(pdfMode); 
		
		canvas.drawArc(transOval, 0f, 360f, true, circlePaint);
		
		paint.setXfermode(null);
		canvas.restore();
	}
	
	/**
	 * 计算扇形重点的坐标
	 * @param centerPoint
	 * @param startAngle
	 * @param endAngle
	 * @return
	 */
	private Point computeSectorMiddleCoord(Point centerPoint,float startAngle,float endAngle) {
		Point point = new Point();
		// 因为android画圆是从右边中间开始，度数在计算的时候用开始度数减去结束度数
		int wDistance = (int) (Math.cos((startAngle-endAngle)/2)*radius);
		
		return point;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 进行自适应适配
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if(widthSize > 0 && heightSize > 0) {
			centerCoord.x = widthSize/2;
			centerCoord.y = heightSize/2;
			// 取最大padding
			int maxPadding = Math.max(getPaddingLeft(), getPaddingRight()) > Math.max(getPaddingTop(), getPaddingBottom())?
					Math.max(getPaddingLeft(), getPaddingRight()):Math.max(getPaddingTop(), getPaddingBottom());
			if(maxPadding == 0 && isShowLabel) {
				// TODO
			}
			
			radius = (widthSize > heightSize ? heightSize:widthSize)/2-maxPadding;
			innerRadius = (2*radius)/3; // 内圆半径默认为外圆的3/2
			
		} else {
			innerRadius = 0;
			radius = 0;
			centerCoord.x = 0;
			centerCoord.y = 0;
		}
		
		oval = new RectF(centerCoord.x-radius, centerCoord.y-radius,
				centerCoord.x+radius, centerCoord.y+radius);
		transOval = new RectF(centerCoord.x-innerRadius, centerCoord.y-innerRadius,
				centerCoord.x+innerRadius, centerCoord.y+innerRadius);
		int tempDistance = (radius-innerRadius)/2;
		bigOval = new RectF(centerCoord.x-radius-tempDistance, centerCoord.y-radius-tempDistance,
				centerCoord.x+radius+tempDistance, centerCoord.y+radius+tempDistance);
		
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
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * 刷新数据
	 */
	public void notifySetDataChanged() {
		if(myPieAdapter == null) {
			return;
		}
		initDrawTools();
		totalValue = 0;
		// 计算数据总值
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			totalValue += myPieAdapter.getItem(i).sectorValue;
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

	
	
}
