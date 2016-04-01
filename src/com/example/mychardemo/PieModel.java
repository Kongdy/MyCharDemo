package com.example.mychardemo;

/**
 * 饼图model
 * @author wangk
 */
public class PieModel {
	
	public PieModel(String sectorName, float sectorValue, int sectorColor) {
		this.sectorName = sectorName;
		this.sectorValue = sectorValue;
		this.sectorColor = sectorColor;
	}
	

	public PieModel() {
	}



	/**
	 * 扇形名字
	 */
	public String sectorName;
	
	/**
	 * 扇形数值
	 */
	public float sectorValue;
	
	/**
	 * 扇形颜色
	 */
	public int sectorColor;
}
