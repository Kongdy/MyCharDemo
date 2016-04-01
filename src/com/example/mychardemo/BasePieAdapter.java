package com.example.mychardemo;


/**
 * 饼图适配基类
 * @author wangk
 */
public interface BasePieAdapter {
	
	/**
	 * 饼图数据条数
	 * @return
	 */
	public int getCount();
	
	/**
	 * 获取对应扇形数据
	 * @return
	 */
	public PieModel getItem(int position);
	
	/**
	 * 获取数据
	 * @param position
	 * @return
	 */
	public int getId(int position);
	
}
