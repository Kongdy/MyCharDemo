package com.example.mychardemo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.res.Resources;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * 我的饼图
 * 
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
	/** 外层圆装饰半径 */
	private int decorateRadius;
	/** 当前点击的扇形 */
	private int clickSector;
	/** 最长标注文字长度 */
	private int max_label_length;

	/** 标注文字大小 */
	private float noteTextSize;
	/** 扇形图上信息文字大小 */
	private float circleLabelTextSize;

	private Point click_down_point;
	private Point click_move_point;

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
	/** 当没有数据的时候显示得字 */
	private String defaultText;
	/** 动画 */
	private PieAnimlThread animThread;
	/** 开始拖动 */
	private boolean beginDrag;

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
		centerCoord = new Point();
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

		// 字体加粗
		notePaint.setFakeBoldText(true);
		// circlePaint.setFakeBoldText(true);

		// init data
		clickSector = -1;
		isRandColor = false;
		isShowLabel = true;
		pdfMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);// 两个图层，取上层非交集部分显示
		defaultText = "it's not have data or value is 0";
		innerRadius = -1;
		animThread = new PieAnimlThread();
		beginDrag = false;
		click_move_point = new Point();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
				// 新建图层
		canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint,
				Canvas.ALL_SAVE_FLAG);
		// 没有数据的时候提醒
		if (myPieAdapter == null || totalValue <= 0) {
			canvas.drawText(defaultText, centerCoord.x, centerCoord.y,
					notePaint);
			canvas.restore();
			return;
		}

		for (MyPie p : pies) {
			if (p.isTouch()) {
				p.myDraw(canvas, p.startAngle, p.cursorEndAngle - p.startAngle,
						paint, circlePaint, bigOval);
			} else {
				p.myDraw(canvas, p.startAngle, p.cursorEndAngle - p.startAngle,
						paint, circlePaint, oval);
			}
			// 画标注
			if (isShowLabel) {
				p.drawLabel(canvas, paint, labelPaint, labelSquareEdge,
						cursorLabelPoint);
			}
		}
		// 装饰圆
		canvas.drawArc(decorateOval, 0f, 360f, true, decoratePaint);

		paint.setXfermode(pdfMode);

		canvas.drawArc(transOval, 0f, 360f, true, circlePaint);

		paint.setXfermode(null);

		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 进行自适应适配
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthSize > 0 && heightSize > 0) {
			// 取最大padding
			int maxPadding = Math.max(getPaddingLeft(), getPaddingRight()) > Math
					.max(getPaddingTop(), getPaddingBottom()) ? Math.max(
					getPaddingLeft(), getPaddingRight()) : Math.max(
					getPaddingTop(), getPaddingBottom());
			if (maxPadding < widthSize / 20) {
				// 默认有20/1宽度的padding
				maxPadding = widthSize / 20;
			}
			if (isShowLabel) {
				labelSquareEdge = widthSize / 35;
				maxPadding += labelSquareEdge + max_label_length
						* getLabelTextSize();
				centerCoord.x = widthSize / 2 - maxPadding / 2;
				centerCoord.y = heightSize / 2;
			} else {
				centerCoord.x = widthSize / 2;
				centerCoord.y = heightSize / 2;
			}

			radius = (widthSize > heightSize ? heightSize : widthSize) * 7 / 15
					- maxPadding / 2;
			if (innerRadius == -1) {
				innerRadius = (4 * radius) / 7; // 内圆半径默认为外圆的4/7
			}
		} else {
			innerRadius = 0;
			radius = 0;
			centerCoord.x = 0;
			centerCoord.y = 0;
		}

		/*
		 * 计算矩阵
		 */
		int tempDistance = (radius - innerRadius) / 4;
		decorateRadius = innerRadius <= 0 ? 0 : innerRadius + tempDistance / 2;

		oval.left = centerCoord.x - radius;
		oval.top = centerCoord.y - radius;
		oval.right = centerCoord.x + radius;
		oval.bottom = centerCoord.y + radius;

		transOval.left = centerCoord.x - innerRadius;
		transOval.top = centerCoord.y - innerRadius;
		transOval.right = centerCoord.x + innerRadius;
		transOval.bottom = centerCoord.y + innerRadius;

		bigOval.left = centerCoord.x - radius - tempDistance;
		bigOval.top = centerCoord.y - radius - tempDistance;
		bigOval.right = centerCoord.x + radius + tempDistance;
		bigOval.bottom = centerCoord.y + radius + tempDistance;

		decorateOval.left = centerCoord.x - decorateRadius;
		decorateOval.top = centerCoord.y - decorateRadius;
		decorateOval.right = centerCoord.x + decorateRadius;
		decorateOval.bottom = centerCoord.y + decorateRadius;
		if (isShowLabel) {
			double a = getLabelTextSize()/2 * max_label_length - labelSquareEdge * 1.5;
			// tempDistance 在这里没有意义，只是为了能够离圆远点
			cursorLabelPoint.x = (int) (centerCoord.x + radius + tempDistance
					- a);
			cursorLabelPoint.y = centerCoord.y - radius;
		}
		// 初始化半径
		for (MyPie p : pies) {
			p.setRadius(radius, innerRadius);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	/**
	 * 设置数据源
	 * 
	 * @param myPieAdapter
	 */
	public void setAdapter(BasePieAdapter myPieAdapter) {
		this.myPieAdapter = myPieAdapter;
		notifySetDataChanged();
	}

	/**
	 * 手动指定饼图选中状态
	 * 
	 * @param position
	 */
	public void setSelectPie(int position) {
		if (position > pies.size()) {
			throw new IllegalArgumentException(
					"position is more than max size,the position is:"
							+ position + "," + "but the size is :"
							+ pies.size());
		}
		clickSector = position;
		for (int i = 0; i < pies.size(); i++) {
			if (i == position) {
				pies.get(i).setSelect(true);
			} else {
				pies.get(i).setSelect(false);
			}
		}
		invalidate();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			click_move_point.x = (int) event.getX();
			click_move_point.y = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			final int tempX = x-click_move_point.x;
			final int tempY = y-click_move_point.y;
			final int relativeCenterX = x-centerCoord.x;
			final int relativeCenterY = y-centerCoord.y;
			int keyFactor = 1;
			double relativeDistance =  Math.sqrt(Math.pow(relativeCenterX, 2)
					+Math.pow(relativeCenterY, 2));
			double tempDistance = Math.sqrt(Math.pow(tempX, 2)
					+Math.pow(tempY, 2));
			if(relativeDistance <= radius && relativeDistance>= innerRadius) {
				beginDrag = true;
				// 判断象限
				switch (getPointQuadrntnt(click_move_point)) {
				case 1:
					if(tempX <= 0 && tempY >= 0) {
						keyFactor = 1;
					} else if((tempX < 0 && tempY <0) || (tempX > 0 && tempY >0)) {
						keyFactor = 2;
					} else {
						keyFactor = -1;
					}
					break;
				case 2:
					if(tempX <= 0 && tempY <= 0) {
						keyFactor = 1;
					} else if((tempX > 0 && tempY <0) || (tempX < 0 && tempY >0)) {
						keyFactor = 2;
					} else{
						keyFactor = -1;
					}
					break;
				case 3:
					if((tempX >=0 && tempY <= 0)) {
						keyFactor = 1;
					} else if((tempX < 0 && tempY <0) || (tempX > 0 && tempY >0)) {
						keyFactor = 2;
					}else {
						keyFactor = -1;
					}
						
					break;
				case 4:
					if(tempX >= 0 && tempY >= 0) {
						keyFactor = 1;
					} else if((tempX > 0 && tempY <0) || (tempX < 0 && tempY >0)) {
						keyFactor = 2;
					} else {
						keyFactor = -1;
					}
					break;
				default:
					Log.e(VIEW_LOG_TAG, "wrong point");
					break;
				}
				if(keyFactor != 2) {
					for (MyPie mp : pies) {
						mp.startAngle+=(tempDistance/5.0f)*keyFactor;
						if(mp.startAngle >= 360) {
							mp.startAngle = mp.startAngle-360;
						}
						mp.cursorEndAngle = mp.startAngle+mp.sweepAngle;
						
					}
				}
				click_move_point.x  = x;
				click_move_point.y = y;
				beginDrag = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(!beginDrag) {
				click_down_point.x = (int) event.getX();
				click_down_point.y = (int) event.getY();
				for (int i = 0; i < pies.size(); i++) {
					MyPie p = pies.get(i);
					if (p.isInArea(click_down_point)) {
						clickSector = i;
						if (onPieSelectListener != null) {
							onPieSelectListener.selectChanged(i);
						}
					}
				}
			}
			break;
		default:
			break;
		}
	
		invalidate();
		return true;
	}

	/**
	 * 刷新数据
	 */
	public void notifySetDataChanged() {
		if (myPieAdapter == null) {
			return;
			// throw new IllegalArgumentException("adapter is null!");
		}
		if (pies == null) {
			pies = new CopyOnWriteArrayList<MyPie>();
		}
		pies.clear();
		// initDrawTools();
		totalValue = 0;
		// 计算数据总值
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			totalValue += myPieAdapter.getItem(i).sectorValue;
		}
		float cursorAngle = 0f;
		PieModel p = null;
		max_label_length = 0;
		for (int i = 0; i < myPieAdapter.getCount(); i++) {
			p = myPieAdapter.getItem(i);
			if (isRandColor) {
				p.sectorColor = getRandomColor();
			}
			if (max_label_length < p.sectorName.length()) {
				max_label_length = p.sectorName.length();
			}
			float sweepAngle = ((p.sectorValue / totalValue) * 360f);
			pies.add(new MyPie(cursorAngle, sweepAngle, centerCoord, p,
					MyPie.LABEL_GRIVITY.DRAW_NONE_LABEL.navtieInt, i));
			cursorAngle = cursorAngle + sweepAngle;
		}

		if (max_label_length > MAX_LABEL_SIZE) {
			max_label_length = MAX_LABEL_SIZE;
		}
		if(!animThread.isAlive()) {
			animThread.start();
		}
	//	invalidate();
	}

	public int getCircleTextColor() {
		return circleTextColor == 0 ? Color.WHITE : circleTextColor;
	}

	public int getLableTextColor() {
		return lableTextColor == 0 ? Color.BLACK : lableTextColor;
	}

	/**
	 * 设置饼图内半径
	 */
	public void setInnerRaduis(int radius) {
		this.innerRadius = radius;
		invalidate();
	}

	/**
	 * 是否显示标注
	 * 
	 * @param flag
	 */
	public void isShowLabel(boolean flag) {
		this.isShowLabel = flag;
	}

	/**
	 * 获取数据源adapter
	 * 
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
	 * 
	 * @return
	 */
	public int getRandomColor() {
		if (randColors == null) {
			randColors = new ArrayList<Integer>();
		}
		if (randColors.size() >= 16581375) {
			randColors.clear();
		}
		int r = (int) (Math.random() * 255);
		int g = (int) (Math.random() * 255);
		int b = (int) (Math.random() * 255);

		int color = Color.rgb(r, g, b);
		if (!randColors.contains(color)) {
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
		return noteTextSize == 0f ? getRawSize(getContext(),
				TypedValue.COMPLEX_UNIT_SP, 10) : noteTextSize;
	}

	public void setCircleTextSize(float pixelSize) {
		this.circleLabelTextSize = pixelSize;
		circlePaint.setTextSize(circleLabelTextSize);
		invalidate();
	}

	/**
	 * 修改没有数据时候的默认显示文字
	 * 
	 * @param text
	 */
	public void setDefaultText(String text) {
		this.defaultText = text;
		invalidate();
	}

	public float getCircleTextSize() {
		return circleLabelTextSize == 0f ? getRawSize(getContext(),
				TypedValue.COMPLEX_UNIT_SP, 12) : circleLabelTextSize;
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
	 * 
	 * @author wangk
	 */
	public interface OnPieSelectListener {
		public void selectChanged(int position);
	}

	/**
	 * 通过指定单位，返回一个像素值 xml单位适配
	 * 
	 * @param context
	 * @param unit
	 *            单位
	 * @param value
	 *            数值
	 * @return
	 */
	public float getRawSize(Context context, int unit, float value) {
		Resources res = context.getResources();
		return TypedValue.applyDimension(unit, value, res.getDisplayMetrics());
	}

	/**
	 * 饼图旋转动画
	 * @author wangk
	 */
	class PieAnimlThread extends Thread {
		@Override
		public void run() {
			// activity有个加载动画，防止线程提前开始绘制，增加一个等待时间
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (MyPie p : pies) {
				while (p.cursorEndAngle < (p.startAngle + p.sweepAngle)) {
					long startTime = System.currentTimeMillis();
					p.cursorEndAngle+=1f;
					postInvalidate();
					long endTime = System.currentTimeMillis();
					// 限定帧数
					if ((endTime - startTime) < 2) {
						try {
							Thread.sleep(2 - (endTime - startTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
			
		}
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
	 * 根据所给坐标，返回一个相对于圆心的象限
	 * @param point
	 * @return
	 */
	private int getPointQuadrntnt(Point point) {
		double angle = getPointAngle(point);
		double realAngle = Math.abs(angle)%360;
		if(realAngle>=0 && realAngle < 90) {
			return 1;
		} else if(realAngle >= 90 && realAngle < 180) {
			return 2;
		} else if(realAngle >= 180 && realAngle < 270) {
			return 3;
		} else if(realAngle >= 270 && realAngle < 360) {
			return 4;
		}
					
		return 0;
	}
}
