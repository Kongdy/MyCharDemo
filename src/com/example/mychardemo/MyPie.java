package com.example.mychardemo;

import java.math.BigDecimal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;

/**
 * 扇形对象
 * @author wangk
 */
public class MyPie {
	/**
	 * 开始坐标
	 */
	public float startAngle;
	/**
	 * 结束坐标
	 */
	public float sweepAngle;
	
	public RectF oval;
	/**
	 * 圆重点坐标
	 */
	private Point centerCoord;
	/** 圆半径 */
	private int radius;
	/** 内圆半径 */
	private int innerRadius;
	/** 扇形数据对象 */
	private PieModel p;
	/** 所占百分比 */
	private String rate;
	/** 是否被点中 */
	private boolean isTouch;
	/** 饼图文字信息坐标 */
	private Point textP;
	/** 标注颜色示例边长 */
	private int labelSqaureEdge;
	/** 标注位置 */
	private int labelGrivity;
	/** 标注坐标 */
	private Point labelPoint;
	/** 标注位置 */
	private int position;
	/** 标注矩阵位置 */
	private Rect rect;
	/** 辅助动画的游标角度 */
	public float cursorEndAngle;
	/** 扇形区域 */
	/* region适合path所构成的不规则图形，不适合本图形判断 */
	//private Region region;
	
	public MyPie(float startAngle, float sweepAngle,Point centerCoord,PieModel p,int labelGrivity,int position) {
		this.startAngle = startAngle;
		this.sweepAngle = sweepAngle;
		this.centerCoord = centerCoord;
		this.p = p;
		this.labelGrivity = labelGrivity;
		this.position = position;
		textP = new Point();
		cursorEndAngle =  startAngle;
	}

	/**
	 * 绘画对象
	 * @param canvas 
	 * @param oval
	 * @param paint 扇形底色画笔
	 * @param textPaint 扇形上显示信息画笔
	 * @param radius 圆半径
	 * @param innerRadius 内圆半径
	 * @param isDrawLabel 是否画右侧标注
	 */
	public void drawSelf(Canvas canvas,RectF oval,Paint paint,Paint textPaint,int radius,int innerRadius) {
		if(centerCoord == null) {
			throw new IllegalArgumentException("center coord is null");
		}
		this.radius = radius;
		this.innerRadius = innerRadius;
		this.oval = oval;
		double numRate = sweepAngle/3.6;
		BigDecimal bigRate = new BigDecimal(numRate);
		bigRate = bigRate.setScale(1, BigDecimal.ROUND_HALF_UP);
		rate = "%"+bigRate;
		myDraw(canvas, startAngle, sweepAngle, paint, textPaint, oval);
	}
	
	/**
	 * easyDraw
	 * @param canvas
	 * @param startAngle
	 * @param sweepAngle
	 * @param paint
	 * @param textPaint
	 * @param oval
	 */
	public void myDraw(Canvas canvas,float startAngle,float sweepAngle,Paint paint,Paint textPaint,RectF oval) {
		if(TextUtils.isEmpty(rate)) {
			double numRate = this.sweepAngle/3.6;
			BigDecimal bigRate = new BigDecimal(numRate);
			bigRate = bigRate.setScale(1, BigDecimal.ROUND_HALF_UP);
			rate = "%"+bigRate;
		}
		if(sweepAngle > 0) {
			paint.setColor(p.sectorColor);
			canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
			textP = computeSectorMiddleCoord(textP, startAngle+sweepAngle, sweepAngle);
			canvas.drawText(p.sectorName,textP.x, textP.y, textPaint);
			canvas.drawText(rate,textP.x, textP.y+textPaint.getTextSize(), textPaint);
		}
	}
	
	public void setRadius(int radius,int innerRadius) {
		this.radius = radius;
		this.innerRadius = innerRadius;
	}
	
	
	/**
	 * 初始化label参数
	 * @param labelGrivity
	 * @param labelSqaureEdge
	 * @param labelPoint
	 */
	private void initLabel(int labelSqaureEdge,Point labelPoint) {
		// label标志越界，则不画
		if(!LABEL_GRIVITY.isBelongLabel_grivity(labelGrivity)) {
			labelGrivity = LABEL_GRIVITY.DRAW_NONE_LABEL.navtieInt;
		}
		this.rect = new Rect();
		this.labelPoint = new Point();
		
		this.labelPoint.x = (int) (labelPoint.x+labelSqaureEdge*1.5f); 
		this.labelPoint.y = (int) (labelPoint.y+labelSqaureEdge*0.8f+position*labelSqaureEdge*1.5);
		
		this.labelSqaureEdge = labelSqaureEdge;
		rect.left = (int) (this.labelPoint.x-labelSqaureEdge*1.5f);
		rect.top = (int) (this.labelPoint.y-labelSqaureEdge*0.8f);
		rect.right = (int) (this.labelPoint.x-labelSqaureEdge*0.5f);
		rect.bottom = this.labelPoint.y;
	}
	
	/**
	 * 画标注
	 * @param canvas
	 * @param textPaint
	 */
	public void drawLabel(Canvas canvas,Paint rectPaint,TextPaint textPaint,int labelSqaureEdge,Point labelPoint) {
		initLabel(labelSqaureEdge, labelPoint);
		rectPaint.setColor(p.sectorColor);
		canvas.drawRect(rect, rectPaint);
		canvas.drawText(getSelectorName(), this.labelPoint.x, this.labelPoint.y, textPaint);
		// 预留
		if(labelGrivity == LABEL_GRIVITY.DRAW_TOP_RIGHT_LABEL.navtieInt) {
		}
	}
	
	/**
	 * 判断传进来的坐标是否在当前扇形中
	 * @param point 需要判断的坐标
	 * @return
	 */
	public boolean isInArea(Point point) {
		if(point == null) {
			isTouch = false;
			return false;
		}
		float bevelledEdge = (float) Math.sqrt(Math.pow(point.x-centerCoord.x, 2)+Math.pow(point.y-centerCoord.y, 2));
		if(bevelledEdge <= radius && bevelledEdge > innerRadius) {
			double angle = getPointAngle(point);
			if(angle > startAngle && angle < (startAngle+sweepAngle) ) {
				isTouch = true;
				return true;
			}
		}
		isTouch = false;
		return false;
	}
	
	
	
	/**
	 * 计算坐标的相对于圆心的度数
	 * @param point
	 * @return
	 */
	private double getPointAngle(Point point) {
		float a = point.x - centerCoord.x;
		float b = point.y - centerCoord.y;
		if(b <= 0) {
			return 180f+(180f+Math.atan2(b, a)*(180f/Math.PI));
		}
		return Math.atan2(b, a)*(180f/Math.PI);
	}
	
	/**
	 * 计算扇形中点的坐标
	 * @param centerPoint 坐标
	 * @param totalSweepAngle 当前已经扇形已经扫荡的总度数
	 * @param currentSweepAngle  当前所在扇形所占的度数
	 * @return
	 */
	private Point computeSectorMiddleCoord(Point centerPoint,float totalSweepAngle,float currentSweepAngle) {
		if(centerPoint == null) {
			centerPoint = new Point();
		}
		Point point = new Point();
		float angle = totalSweepAngle-currentSweepAngle/2;
		angle = (float) Math.floor(angle);
		int delRadius = radius - (radius-innerRadius)/2;
		int x = 0;
		int y = 0;
		x =  (int)(Math.cos(Math.toRadians(angle))*delRadius);
		y =  (int)(Math.sin(Math.toRadians(angle))*delRadius);
		point.x = centerCoord.x+x;
		point.y = centerCoord.y+y;
		return point;
	}
	
	public int getSelectorColor() {
		return p.sectorColor;
	}
	
	public String getSelectorName() {
		return p.sectorName;
	}
	
	public String getSelectorRate() {
		return rate;
	}
	
	public boolean isTouch() {
		return isTouch;
	}
	
	/**
	 * 设置选中状态
	 */
	public void setSelect(boolean flag) {
		isTouch = flag;
	}
	
	public static enum LABEL_GRIVITY {
		/**
		 * 不画标注
		 */
		DRAW_NONE_LABEL(0),
		/**
		 * 在上左侧画标注
		 */
		DRAW_TOP_LEFT_LABEL(1),
		/**
		 * 在上右侧画标注
		 */
		DRAW_TOP_RIGHT_LABEL(2),
		/**
		 * 在底左侧画标注
		 */
		DRAW_BOTTOM_LEFT_LABEL(3),
		/**
		 * 在底右侧画标注
		 */
		DRAW_BOTTOM_RIGHT_LABEL(4);
		
		public int navtieInt;
		private LABEL_GRIVITY(int navtieInt) {
			this.navtieInt = navtieInt;
		}
		
		public static boolean isBelongLabel_grivity(int num) {
			if(num == 0 || num == 1 || num == 2 || num ==3 || 
					num == 4) {
				return true;
			}
			return false;
		}
	}
	
}
