package cn.incorner.contrast.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;


public class ImageUtils {
	public static Bitmap resizeImageByWidth(Bitmap defaultBitmap,
			int targetWidth) {
		int rawWidth = defaultBitmap.getWidth();
		int rawHeight = defaultBitmap.getHeight();
		//float targetHeight = targetWidth * (float) rawHeight / (float) rawWidth;
		//float scaleWidth = targetWidth / (float) rawWidth;
		//float scaleHeight = targetHeight / (float) rawHeight;
		
		//按高度缩放
		float newWidth= rawWidth * (float) targetWidth / (float) rawHeight;
		float scaleWidth = newWidth / (float) rawWidth;
		float scaleHeight = targetWidth / (float) rawHeight;
				
		Matrix localMatrix = new Matrix();
		localMatrix.postScale(scaleHeight, scaleWidth);
		return Bitmap.createBitmap(defaultBitmap, 0, 0, rawWidth, rawHeight,
				localMatrix, true);
	}
	
}
