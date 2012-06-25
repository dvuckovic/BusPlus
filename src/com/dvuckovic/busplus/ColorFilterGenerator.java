package com.dvuckovic.busplus;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class ColorFilterGenerator {
	/**
	 * Creates a HUE adjustment ColorFilter
	 * 
	 * @param value
	 *            degrees to shift the hue
	 * @return ColorFilter
	 */
	public static ColorFilter adjustHue(float value) {
		ColorMatrix cm = new ColorMatrix();

		adjustHue(cm, value);

		return new ColorMatrixColorFilter(cm);
	}

	/**
	 * Adjusts HUE for a supplied value using supplied ColorMatrix
	 * 
	 * @param cm
	 * @param value
	 */
	public static void adjustHue(ColorMatrix cm, float value) {
		value = cleanValue(value, 180f) / 180f * (float) Math.PI;
		if (value == 0) {
			return;
		}
		float cosVal = (float) Math.cos(value);
		float sinVal = (float) Math.sin(value);
		float lumR = 0.213f;
		float lumG = 0.715f;
		float lumB = 0.072f;
		float[] mat = new float[] {
				lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
				lumG + cosVal * (-lumG) + sinVal * (-lumG),
				lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
				lumR + cosVal * (-lumR) + sinVal * (0.143f),
				lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
				lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
				lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
				lumG + cosVal * (-lumG) + sinVal * (lumG),
				lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0, 0f, 0f, 0f,
				1f, 0f, 0f, 0f, 0f, 0f, 1f };
		cm.postConcat(new ColorMatrix(mat));
	}

	/**
	 * Makes sure HUE value doesn't go out of bounds and cause artifacts
	 * 
	 * @param p_val
	 * @param p_limit
	 * @return float
	 */
	protected static float cleanValue(float p_val, float p_limit) {
		return Math.min(p_limit, Math.max(-p_limit, p_val));
	}
}