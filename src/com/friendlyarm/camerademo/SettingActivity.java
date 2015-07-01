package com.friendlyarm.camerademo;

import java.io.File;
import java.util.List;

import com.friendlyarm.camerademo.R;
import com.friendlyarm.camerademo.Util;
import com.friendlyarm.camerademo.Main.btnListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SettingActivity extends Activity {
	private List<Size> supportedPictureSizes;
	private List<Size> supportedVideoSizes;
	
	private Integer videoWidth;
	private Integer videoHeight;
	private Integer pictureWidth;
	private Integer pictureHeight;
	
	@SuppressLint("NewApi")
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map_file_setting);

		Camera camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		supportedPictureSizes = parameters.getSupportedPictureSizes();
		supportedVideoSizes = parameters.getSupportedVideoSizes();
		camera.release();
		camera = null;
		
		Intent i = getIntent();
		pictureWidth = i.getIntExtra("pictureWidth", Util.getMaxSize(supportedPictureSizes).width);
		pictureHeight = i.getIntExtra("pictureHeight", Util.getMaxSize(supportedPictureSizes).height);
		videoWidth = i.getIntExtra("videoWidth", Util.getMaxSize(supportedVideoSizes).width);
		videoHeight = i.getIntExtra("videoHeight", Util.getMaxSize(supportedVideoSizes).height);

        Spinner pictureSizeSpinner = (Spinner) findViewById(R.id.pictureSizeSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Util.getSizesStringList(supportedPictureSizes));
        pictureSizeSpinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(""+pictureWidth+"x"+pictureHeight);
        pictureSizeSpinner.setSelection(spinnerPosition);
        
        pictureSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	Size s = supportedPictureSizes.get(position);
            	pictureWidth = s.width;
            	pictureHeight = s.height;
            }
            
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
        });
        
        
        Spinner videoSizeSpinner = (Spinner) findViewById(R.id.videoSizeSpinner);
        ArrayAdapter adapter2 = new ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Util.getSizesStringList(supportedVideoSizes));
        videoSizeSpinner.setAdapter(adapter2);
        int spinnerPosition2 = adapter2.getPosition(""+videoWidth+"x"+videoHeight);
        videoSizeSpinner.setSelection(spinnerPosition2);
        
        videoSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	Size s = supportedVideoSizes.get(position);
            	videoWidth = s.width;
            	videoHeight = s.height;
            }
            
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
        });
        
        Button btnSave = (Button) findViewById(R.id.arc_hf_btnSave);
        btnSave.setOnClickListener(new btnListener());
	}
	
	class btnListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.arc_hf_btnSave:
				Intent output = new Intent();
				output.putExtra("pictureWidth", pictureWidth);
				output.putExtra("pictureHeight", pictureHeight);
				output.putExtra("videoWidth", videoWidth);
				output.putExtra("videoHeight", videoHeight);
				setResult(RESULT_OK, output);
				finish();
				break;
			default:
				break;
			}
		}

	}
}

