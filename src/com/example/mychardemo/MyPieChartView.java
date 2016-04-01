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
 * �ҵı�ͼ
 * @author wangk
 */
public class MyPieChartView extends View {
	
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
	
	private RectF oval;
	private RectF transOval;
	private RectF bigOval;
	
	/**
	 * ��ͼ�ڰ뾶
	 * δָ��������Ӧ�뾶
	 */
	private int innerRadius;
	/** ��ͼ�뾶 */
	private int radius;
	/**  ��ǰ��������� */
	private int clickSector;
	private  Point cursorP;
	
	private Point click_down_point;
	private Point click_up_point;
	
	/**
	 * �Ƿ���ʾ��ע
	 */
	private boolean isShowLabel;
	
	private int totalValue;
	

	public MyPieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawTools();
	}
	
	/**
	 * ��ʼ�����ʹ���
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
		paint.setAntiAlias(true); // �����
		labelPaint.setAntiAlias(true);
		circlePaint.setAntiAlias(true);
		notePaint.setAntiAlias(true);
		notePaint.setTextAlign(Align.CENTER);
		notePaint.setFakeBoldText(true);
		
		// init data
		clickSector = -1;
		 cursorP = new Point();
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
		
		canvas.drawColor(Color.TRANSPARENT);
		float cursorAngle = 0f; // �α�Ƕ�
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
	 * ���������ص������
	 * @param centerPoint
	 * @param startAngle
	 * @param endAngle
	 * @return
	 */
	private Point computeSectorMiddleCoord(Point centerPoint,float startAngle,float endAngle) {
		Point point = new Point();
		// ��Ϊandroid��Բ�Ǵ��ұ��м俪ʼ�������ڼ����ʱ���ÿ�ʼ������ȥ��������
		int wDistance = (int) (Math.cos((startAngle-endAngle)/2)*radius);
		
		return point;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// ��������Ӧ����
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if(widthSize > 0 && heightSize > 0) {
			centerCoord.x = widthSize/2;
			centerCoord.y = heightSize/2;
			// ȡ���padding
			int maxPadding = Math.max(getPaddingLeft(), getPaddingRight()) > Math.max(getPaddingTop(), getPaddingBottom())?
					Math.max(getPaddingLeft(), getPaddingRight()):Math.max(getPaddingTop(), getPaddingBottom());
			if(maxPadding == 0 && isShowLabel) {
				// TODO
			}
			
			radius = (widthSize > heightSize ? heightSize:widthSize)/2-maxPadding;
			innerRadius = (2*radius)/3; // ��Բ�뾶Ĭ��Ϊ��Բ��3/2
			
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
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * ˢ������
	 */
	public void notifySetDataChanged() {
		if(myPieAdapter == null) {
			return;
		}
		initDrawTools();
		totalValue = 0;
		// ����������ֵ
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

	
	
}
