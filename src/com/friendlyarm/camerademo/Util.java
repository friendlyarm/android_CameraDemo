package com.friendlyarm.camerademo; 

import java.util.List;

import android.hardware.Camera.Size;

class Util {
	static Size getMaxSize(List<Size> sizes) {
		Size maxSize = sizes.get(0);
		for(int i=1; i<sizes.size(); i++)    {   
			Size s = sizes.get(i);
			if (s.width > maxSize.width) {
				maxSize = s;
			}
		}
		return maxSize;
	}
	
	static String[] getSizesStringList(List<Size> sizes) {
		String[] array=new String[sizes.size()];
		for(int i=0; i<sizes.size(); i++)    {   
			Size s = sizes.get(i);
			array[i] = "" + s.width + "x" + s.height;
		}
		return array;
	}
}