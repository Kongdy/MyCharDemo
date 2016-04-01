package com.example.mychardemo;

import java.util.List;


/**
 *   ≈‰
 * @author wangk
 */
public class MyPieAdapter implements BasePieAdapter {
	
	private List<PieModel> list;
	
	public MyPieAdapter(List<PieModel> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public PieModel getItem(int position) {
		return list.get(position%list.size());
	}

	@Override
	public int getId(int position) {
		return position;
	}

}
