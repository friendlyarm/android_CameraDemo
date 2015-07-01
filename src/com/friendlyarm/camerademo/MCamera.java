package com.friendlyarm.camerademo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.friendlyarm.camerademo.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MCamera extends Activity {
	private Button mVideoStartBtn;
	private SurfaceView mSurfaceview;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	private File mRecVedioPath;
	private File mRecAudioFile;
	private TextView timer;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;
	private int parentId;
	protected Camera camera = null;
	protected boolean isPreview;
	private Drawable iconStart;
	private Drawable iconStop;
	private boolean isRecording = true;
	private String mode;
	
	private Integer videoWidth;
	private Integer videoHeight;
	private Integer pictureWidth;
	private Integer pictureHeight;
	
	private Size getMinPreviewSize()
	{
		Camera.Parameters parameters = camera.getParameters();
		List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Size minPreviewSize = camera.new Size(Integer.MAX_VALUE,Integer.MAX_VALUE);
		for(int i=0; i<supportedPreviewSizes.size(); i++)    {   
			Size s = supportedPreviewSizes.get(i);
			if (minPreviewSize.width > s.width) {
				minPreviewSize = s;
			}
		} 
		return minPreviewSize;
	}
	
	private Size getMaxPreviewSize()
	{
		Camera.Parameters parameters = camera.getParameters();
		List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Size maxPreviewSize = camera.new Size(320,240);
		for(int i=0; i<supportedPreviewSizes.size(); i++)    {   
			Size s = supportedPreviewSizes.get(i);
			if (s.width > maxPreviewSize.width) {
				maxPreviewSize = s;
			}
		} 
		return maxPreviewSize;
	}
	
	static SharedPreferences settings;
	private void initCamera() {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPictureSize(pictureWidth, pictureHeight);
		parameters.set("jpeg-quality", 100);
		parameters.setPreviewFrameRate(30);

		Size previewSize = getMaxPreviewSize();
		if (mode.equalsIgnoreCase("img")) {
			previewSize = getMinPreviewSize();
		}
		if (mode.equalsIgnoreCase("video")) {
			previewSize = camera.new Size(videoWidth, videoHeight);
		}

		parameters.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(parameters);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		mode = i.getStringExtra("mode");
		pictureWidth = i.getIntExtra("pictureWidth", -1);
		pictureHeight = i.getIntExtra("pictureHeight", -1);
		videoWidth = i.getIntExtra("videoWidth", -1);
		videoHeight = i.getIntExtra("videoHeight", -1);
		
		if (pictureWidth<0 || pictureHeight<0 || videoWidth<0 || videoHeight<0) {
			showMsg("Invalid Size: pictureSize " + pictureWidth + "x" + pictureHeight + ", videoSize " + videoWidth + "x" + videoHeight);
			return ;
		}

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.map_video);
		
		iconStart = getResources().getDrawable(
				R.drawable.arc_hf_btn_video_start);
		iconStop = getResources().getDrawable(R.drawable.arc_hf_btn_video_stop);

		parentId = getIntent().getIntExtra("parentId", 0);
		timer = (TextView) findViewById(R.id.arc_hf_video_timer);

		mVideoStartBtn = (Button) findViewById(R.id.arc_hf_video_start);
		if (mode.equalsIgnoreCase("img")) {
			mVideoStartBtn.setVisibility(View.GONE);
		}
		
		mSurfaceview = (SurfaceView) this.findViewById(R.id.arc_hf_video_view);
		timer.setVisibility(View.GONE);
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/FACameraDemo/video/temp/");
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}

		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (camera != null) {
					if (isPreview) {
						camera.stopPreview();
						isPreview = false;
					}
					camera.release();
					camera = null;
				}
				mSurfaceview = null;
				mSurfaceHolder = null;
				mMediaRecorder = null;
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					camera = Camera.open();
					initCamera();
					camera.setPreviewDisplay(holder);
					camera.startPreview();
					isPreview = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				mSurfaceHolder = holder;
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mSurfaceHolder = holder;
			}
		});
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					if (isPreview) {
						camera.stopPreview();
						camera.release();
						camera = null;
					}
					second = 0;
					minute = 0;
					hour = 0;
					bool = true;
					if (mMediaRecorder == null)
						mMediaRecorder = new MediaRecorder();
					else
						mMediaRecorder.reset();
					mMediaRecorder.setPreviewDisplay(mSurfaceHolder
							.getSurface());
					mMediaRecorder
							.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					mMediaRecorder
							.setAudioSource(MediaRecorder.AudioSource.MIC);
					mMediaRecorder
							.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					mMediaRecorder
							.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					mMediaRecorder
							.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

					mMediaRecorder.setVideoSize(videoWidth, videoHeight);
					mMediaRecorder.setVideoFrameRate(30);
					try {
						mRecAudioFile = File.createTempFile("Vedio", ".3gp",
								mRecVedioPath);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mMediaRecorder.setOutputFile(mRecAudioFile
							.getAbsolutePath());
					try {
						mMediaRecorder.prepare();
						timer.setVisibility(View.VISIBLE);
						handler.postDelayed(task, 1000);
						mMediaRecorder.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					showMsg("Recording");
					mVideoStartBtn.setBackgroundDrawable(iconStop);
					isRecording = !isRecording;
				} else {
					try {
						bool = false;
						mMediaRecorder.stop();
						timer.setText(format(hour) + ":" + format(minute) + ":"
								+ format(second));
						mMediaRecorder.release();
						mMediaRecorder = null;
						videoRename();
					} catch (Exception e) {
						e.printStackTrace();
					}
					isRecording = !isRecording;
					mVideoStartBtn.setBackgroundDrawable(iconStart);
					showMsg("Finish");

					try {
						camera = Camera.open();
						initCamera();
						camera.setPreviewDisplay(mSurfaceHolder);
						camera.startPreview();
						isPreview = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		Button btnImgStart = (Button) findViewById(R.id.arc_hf_img_start);
		if (mode.equalsIgnoreCase("video")) {
			btnImgStart.setVisibility(View.GONE);
		}
		btnImgStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMediaRecorder != null) {
					try {
						bool = false;
						mMediaRecorder.stop();
						timer.setText(format(hour) + ":" + format(minute) + ":"
								+ format(second));
						mMediaRecorder.release();
						mMediaRecorder = null;
						videoRename();
					} catch (Exception e) {
						e.printStackTrace();
					}
					isRecording = !isRecording;
					mVideoStartBtn.setBackgroundDrawable(iconStart);
					showMsg("Take photo");

					try {
						camera = Camera.open();
						initCamera();
						camera.setPreviewDisplay(mSurfaceHolder);
						camera.startPreview();
						isPreview = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (camera != null) {
					camera.autoFocus(null);
					camera.takePicture(null, null, new PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(data,
									0, data.length);
							String path = Environment
									.getExternalStorageDirectory()
									.getAbsolutePath()
									+ "/FACameraDemo/img/"
									+ String.valueOf(parentId) + "/";
							String fileName = new SimpleDateFormat(
									"yyyyMMddHHmmss").format(new Date())
									+ ".jpg";
							File out = new File(path);
							if (!out.exists()) {
								out.mkdirs();
							}
							out = new File(path, fileName);
							try {
								FileOutputStream outStream = new FileOutputStream(
										out);
								bitmap.compress(CompressFormat.JPEG, 100,
										outStream);
								outStream.close();
								camera.startPreview();
							} catch (Exception e) {
								e.printStackTrace();
							}
							showMsg("Finish");
						}
					});
				}
			}
		});
	}
	
	private Toast toast;
	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	protected void videoRename() {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/FACameraDemo/video/"
				+ String.valueOf(parentId) + "/";
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date()) + ".3gp";
		File out = new File(path);
		if (!out.exists()) {
			out.mkdirs();
		}
		out = new File(path, fileName);
		if (mRecAudioFile.exists())
			mRecAudioFile.renameTo(out);
	}

	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(format(hour) + ":" + format(minute) + ":"
						+ format(second));
			}
		}
	};

	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	@Override
	public void onBackPressed() {
		
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
			videoRename();
		}
		
		if (camera != null) {
			if (isPreview) {
				camera.stopPreview();
				isPreview = false;
			}
			camera.release();
			camera = null;
		}

		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		onBackPressed();
	}
}