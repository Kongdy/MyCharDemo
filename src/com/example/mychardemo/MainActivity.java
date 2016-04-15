package com.example.mychardemo;

import java.util.ArrayList;
import java.util.List;




import com.example.mychardemo.MyPieChartView.OnPieSelectListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private MyPieChartView mPie;
	
	private MyPieAdapter adapter;
	
	private List<PieModel> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		data = new ArrayList<PieModel>();
		PieModel p1 = new PieModel("test1",20f,Color.RED);
		PieModel p2 = new PieModel("test2",30f,Color.BLUE);
		PieModel p3 = new PieModel("test3",40f,Color.CYAN);
		PieModel p4 = new PieModel("test4",50f,Color.GREEN);
		
		data.add(p1);
		data.add(p2);
		data.add(p3);
		data.add(p4);
		
		adapter = new MyPieAdapter(data);
		
		mPie = (MyPieChartView) findViewById(R.id.myPie);
		mPie.setShowLabel(true);
		mPie.setInnerRaduis(0);
		mPie.setOnPieSelectListener(new OnPieSelectListener() {
			
			@Override
			public void selectChanged(int position) {
				System.out.println("position:"+position);
			}
		});
	//	mPie.setRandColor(true);
		mPie.setAdapter(adapter);
	}
	
}
