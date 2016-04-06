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
 * �ҵı�ͼ
 * @author wangk
 */
public class MyPieChartView extends View {
	/** ��ע������ֳ���Ϊ6 */
	private final int MAX_LABEL_SIZE = 6;
	
	/**
	 * ��ͼ������
	 */
	private BasePieAdapter myPieAdapter;
	
	/**
	 * ��Բ����
	 */
	private Paint paint;
	/**
	 * ��ͼ�����ֻ���
	 */
	private TextPaint circlePaint;
	/**
	 * ��ע����
	 */
	private TextPaint labelPaint;
	/**
	 * Ԥ������
	 */
	private TextPaint notePaint;
	/** װ�λ��� */
	private Paint decoratePaint;

	
	/**
	 * ��ͼ��������ɫ��Ĭ��Ϊ��ɫ
	 */
	private int circleTextColor;
	
	/**
	 * ��ע������ɫ
	 */
	private int lableTextColor;
	
	/**
	 * ͼ��ϲ�ģʽ
	 */
	private PorterDuffXfermode pdfMode;
	/**
	 * ��ǰ�ؼ��е�����
	 */
	private Point centerCoord;
	/** ��ע�����α� */
	private Point cursorLabelPoint;
	/** ��ע����߳� */
	private int labelSquareEdge;
	
	private RectF oval;
	private RectF transOval;
	private RectF bigOval;
	private RectF decorateOval;
	
	/** ��ͼ�ڰ뾶,δָ��������Ӧ�뾶 */
	private int innerRadius;
	/** ��ͼ�뾶 */
	private int radius;
	/**  ���Բװ�ΰ뾶 */
	private int decorateRadius;
	/**  ��ǰ��������� */
	private int clickSector;
	/** ���ע���ֳ��� */
	private int max_label_length;
	
	/** ��ע���ִ�С */
	private float noteTextSize;
	/** ����ͼ����Ϣ���ִ�С */
	private float circleLabelTextSize;
	
	private Point click_down_point;
	
	private CopyOnWriteArrayList<MyPie> pies;
	private List<Integer> randColors;
	/**
	 * �Ƿ���ʾ��ע
	 */
	private boolean isShowLabel;
	/** �Ƿ��������ɫ */
	private boolean isRandColor;
	private int totalValue;
	// �ṩ���ⲿ��ǰѡ���ͼ�Ľӿ�
	private OnPieSelectListener onPieSelectListener;
	

	public MyPieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawTools();
	}
	
	/**
	 * ��ʼ�����ʹ���
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
		
		paint.setAntiAlias(true); // �����
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
		 pdfMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);// ����ͼ�㣬ȡ�ϲ�ǽ���������ʾ
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// �½�ͼ��
		canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint, Canvas.ALL_SAVE_FLAG);
		// û�����ݵ�ʱ������
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
			// ����ע
			if(isShowLabel) {
				p.drawLabel(canvas, paint, labelPaint,labelSquareEdge,cursorLabelPoint);
			}
		}
		// װ��Բ
		canvas.drawArc(decorateOval, 0f, 360f, true, decoratePaint);
		
		paint.setXfermode(pdfMode); 
		
		canvas.drawArc(transOval, 0f, 360f, true, circlePaint);
		
		paint.setXfermode(null);
		/*
		 * �ж��Ƿ���ʾ��ע
		 */
		canvas.restore();
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// ��������Ӧ����
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if(widthSize > 0 && heightSize > 0) {
			// ȡ���padding
			int maxPadding = Math.max(getPaddingLeft(), getPaddingRight()) > Math.max(getPaddingTop(), getPaddingBottom())?
					Math.max(getPaddingLeft(), getPaddingRight()):Math.max(getPaddingTop(), getPaddingBottom());
			if(maxPadding < widthSize/20) {
				// Ĭ����20/1��ȵ�padding
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
			innerRadius = (4*radius)/7; // ��Բ�뾶Ĭ��Ϊ��Բ��4/7
		} else {
			innerRadius = 0;
			radius = 0;
			centerCoord.x = 0;
			centerCoord.y = 0;
		}
		
		/*
		 * �������
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
			// tempDistance ������û�����壬ֻ��Ϊ���ܹ���ԲԶ��
			cursorLabelPoint.x = centerCoord.x +radius+tempDistance;
			cursorLabelPoint.y = centerCoord.y - radius;
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}
	
	/**
	 * ��������Դ
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
	 * ˢ������
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
		// ����������ֵ
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
	 * ���ñ�ͼ�ڰ뾶
	 */
	public void setInnerRaduis(int radius) {
		this.innerRadius = radius;
	}
	
	/**
	 * �Ƿ���ʾ��ע
	 * @param flag
	 */
	public void isShowLabel(boolean flag) {
		this.isShowLabel = flag;
	}

	/**
	 * ��ȡ����Դadapter
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
	 * ���һ�������ɫ������Ψһ
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
	 * �ṩ���ⲿ���õ�����ѡ��״̬
	 * @author wangk
	 */
	public interface OnPieSelectListener{
		public void selectChanged(int position);
	}
}
