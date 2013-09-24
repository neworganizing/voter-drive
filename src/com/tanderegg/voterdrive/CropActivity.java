package com.tanderegg.voterdrive;

import com.tanderegg.voterdrive.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CropActivity extends Activity {
	public static CropView cropView;
	
	private static Bitmap imageToCrop;
	private static ImageView previewImageViewDL;
	private static ImageView previewImageViewADDRESS_1;
	private static ImageView previewImageViewDOB;
	private static String imageFile;
	private static float rotation=0.0f;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		Intent sourceIntent = getIntent();
		Bundle extraData = sourceIntent.getExtras();
		Button cropButton;
		
		if(extraData.containsKey("file_path")) {
			imageFile = extraData.getString("file_path");
		}
		else {
			imageFile = "/mnt/sdcard/dcim/Camera/tiana_license_dl_number_simple.png";
		}
		
		// Load designated image file
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		options.inDither = true;
		options.inSampleSize = 4;
		
        imageToCrop = BitmapFactory.decodeFile(imageFile, options);
        imageToCrop = imageToCrop.copy(Bitmap.Config.ARGB_8888, true);
		
        setContentView(R.layout.crop);
        cropView = (CropView)findViewById(R.id.crop_view);
        cropView.setImage(imageToCrop);
        
        previewImageViewDL 			= (ImageView)findViewById(R.id.preview_image_DL);
        previewImageViewADDRESS_1 	= (ImageView)findViewById(R.id.preview_image_ADDRESS_1);
        previewImageViewDOB			= (ImageView)findViewById(R.id.preview_image_DOB);
        
        cropButton = (Button)findViewById(R.id.crop_finished_button);
        
        cropButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {        		
        		Intent fileIntent = new Intent(v.getContext(), ScanActivity.class);
        		        		
        		fileIntent.putExtra("cropRectTop", cropView.getCropTop());
        		fileIntent.putExtra("cropRectLeft", cropView.getCropLeft());
        		fileIntent.putExtra("cropRectBottom", cropView.getCropBottom());
        		fileIntent.putExtra("cropRectRight", cropView.getCropRight());
        		fileIntent.putExtra("file_path", imageFile);
        		fileIntent.putExtra("cropRotation", rotation);
        		
        		String msg = "top: " + cropView.getCropTop() + "left: " + cropView.getCropLeft() + "bottom: " + cropView.getCropBottom() + "right: " + cropView.getCropRight();
        		System.out.println(msg);
        		
        		startActivityForResult(fileIntent, 0);
        	}
        });
        
        Button rotateLeftButton = (Button)findViewById(R.id.rotate_left_button);
        
        rotateLeftButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Matrix matrix = new Matrix();
        		//Bitmap cropPreviewImage = cropView.getImage();
        		
        		rotation = rotation - 0.5f;
        		
        		matrix.setRotate(rotation);
        		Bitmap rotatedImage = Bitmap.createBitmap(imageToCrop, 0, 0, imageToCrop.getWidth(), imageToCrop.getHeight(), matrix, false);
        		cropView.setImage(rotatedImage);
        		cropView.resizeImage(cropView.getWidth(), cropView.getHeight());
        		cropView.updateCropView();
        	}
        });
        
        Button rotateRightButton = (Button)findViewById(R.id.rotate_right_button);
        
        rotateRightButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Matrix matrix = new Matrix();
        		//Bitmap cropPreviewImage = cropView.getImage();
        		
        		rotation = rotation + 0.5f;
        		
        		matrix.setRotate(rotation);
        		Bitmap rotatedImage = Bitmap.createBitmap(imageToCrop, 0, 0, imageToCrop.getWidth(), imageToCrop.getHeight(), matrix, false);
        		cropView.setImage(rotatedImage);
        		cropView.resizeImage(cropView.getWidth(), cropView.getHeight());
        		cropView.updateCropView();
        	}
        });
	}

	public static class CropView extends SurfaceView implements SurfaceHolder.Callback {
		private Bitmap cursorBitmap = null;
		private Bitmap imageBitmap = null;
		private CropCursor topLeftCursor = null;
		private CropCursor bottomRightCursor = null;
		private boolean topLeftDrag = false;
		private boolean bottomRightDrag = false;
		private int width=0, height=0;
		private int touchXOffset=0, touchYOffset=0;
		private int crosshair_width=0, crosshair_height=0;
		
		public CropView(Context context, AttributeSet attributeSet) {
			super(context, attributeSet);
			getHolder().addCallback(this);
			
			// Instantiate the cursors
			topLeftCursor = new CropCursor();
			bottomRightCursor = new CropCursor();
			
			setFocusable(true);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			canvas.drawColor(Color.BLACK);
			
			if(imageBitmap != null)
				canvas.drawBitmap(imageBitmap, 0, 0, null);
			
			if(topLeftCursor != null && cursorBitmap != null)
				canvas.drawBitmap(cursorBitmap,topLeftCursor.x-(cursorBitmap.getWidth()/2), 	topLeftCursor.y-(cursorBitmap.getHeight()/2), 	null);
			
			if(bottomRightCursor != null && cursorBitmap != null)
				canvas.drawBitmap(cursorBitmap,bottomRightCursor.x-(cursorBitmap.getWidth()/2), bottomRightCursor.y-(cursorBitmap.getHeight()/2), null);
		}
		
		@Override
		protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
			super.onSizeChanged(xNew, yNew, xOld, yOld);
			
			width = xNew;
			height = yNew;
			
			topLeftCursor.x = 64;
			topLeftCursor.y = 64;
			
			bottomRightCursor.x = width-64;
			bottomRightCursor.y = height-64;
			
			resizeImage(width, height);
		}
		
		public void resizeImage(int width, int height) {
			// Scale the image to fit the view height
			if(imageBitmap != null) {
				float aspectRatio = (float)imageBitmap.getWidth() / (float)imageBitmap.getHeight();
				aspectRatio = 1.33333f;
				
				String msg = "img_width: " + (float)imageBitmap.getWidth() + ", img_height: " + (float)imageBitmap.getHeight() + ", aspect: " + aspectRatio;
				System.out.println(msg);
				
				if(height > 0)
					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)((float)height*aspectRatio), height, true);
			}
		}
		
		public Bitmap getImage() {
			return imageBitmap;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			
			if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
				topLeftDrag = false;
				bottomRightDrag = false;
				touchXOffset = 0;
				touchYOffset = 0;
			}
			else {
				int touchX=(int)event.getX();
				int touchY=(int)event.getY();
								
				if(!topLeftDrag && !bottomRightDrag) {
					if(touchX > topLeftCursor.x-crosshair_width && touchX < topLeftCursor.x+crosshair_width && touchY > topLeftCursor.y-crosshair_height && touchY < topLeftCursor.y+crosshair_height) {
						// dragging topLeftCursor
						topLeftDrag = true;
						
						// set drag offset
						touchXOffset = touchX-topLeftCursor.x;
						touchYOffset = touchY-topLeftCursor.y;
					}
					else if(touchX > bottomRightCursor.x-crosshair_width && touchX < bottomRightCursor.x+crosshair_width && touchY > bottomRightCursor.y-crosshair_height && touchY < bottomRightCursor.y+crosshair_height) {
						// dragging bottomRightCursor
						bottomRightDrag = true;
						
						// set drag offset
						touchXOffset = touchX-bottomRightCursor.x;
						touchYOffset = touchY-bottomRightCursor.y;
					}	
				}
				
				if(topLeftDrag) {
					topLeftCursor.moveCursor(touchX-touchXOffset, touchY-touchYOffset, width, height);
				}
				else if(bottomRightDrag) {
					bottomRightCursor.moveCursor(touchX-touchXOffset, touchY-touchYOffset, width, height);
				}
				
				if(bottomRightCursor.x < topLeftCursor.x)
					bottomRightCursor.x = topLeftCursor.x+1;
				
				if(bottomRightCursor.y < topLeftCursor.y)
					bottomRightCursor.y = topLeftCursor.y+1;
				
				if(topLeftCursor.x > bottomRightCursor.x)
					topLeftCursor.x = bottomRightCursor.x-1;
				
				if(topLeftCursor.y > bottomRightCursor.y)
					topLeftCursor.y = bottomRightCursor.y-1;
				
				
				updateCropView();	
				
				if(imageBitmap != null) {
					int imageWidth = bottomRightCursor.x - topLeftCursor.x;
					int imageHeight = bottomRightCursor.y - topLeftCursor.y;
					
					//Preview Pane for the DL Number
					Bitmap bmpRegionDL = Bitmap.createBitmap(imageBitmap, topLeftCursor.x+(int)(0.400527009*imageWidth), topLeftCursor.y+(int)(0.183884298*imageHeight), (int)(0.219587176*imageWidth), (int)(0.075757576*imageHeight));
					previewImageViewDL.setImageBitmap(bmpRegionDL);
					
					//Preview pane for the Address Line 1
					Bitmap bmpRegionAddress = Bitmap.createBitmap(imageBitmap, topLeftCursor.x+(int)(0.356609574*imageWidth), topLeftCursor.y+(int)(0.491046832*imageHeight), (int)(0.219587176*imageWidth), (int)(0.041322314*imageHeight));
					previewImageViewADDRESS_1.setImageBitmap(bmpRegionAddress);
					
					//Preview pane for the DOB
					Bitmap bmpRegionDOB = Bitmap.createBitmap(imageBitmap, topLeftCursor.x+(int)(0.422485727*imageWidth), topLeftCursor.y+(int)(0.584022039*imageHeight), (int)(0.219587176*imageWidth), (int)(0.061983471*imageHeight));
					previewImageViewDOB.setImageBitmap(bmpRegionDOB);
				}
			}
			
			return true;
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			cursorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.crosshair);
			
			crosshair_height = cursorBitmap.getHeight();
			crosshair_width = cursorBitmap.getWidth();
			
			//imageBitmap = null;
			updateCropView();
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			//cursorBitmap.recycle();
			//imageBitmap.recycle();			
		}
		
		public void setImage(Bitmap image) {
			imageBitmap = image;
		}
		
		public float getCropTop() {
			return (float)topLeftCursor.y/(float)imageBitmap.getHeight();
		}
		
		public float getCropLeft() {
			return (float)topLeftCursor.x/(float)imageBitmap.getWidth();
		}
		
		public float getCropBottom() {
			return (float)bottomRightCursor.y/(float)imageBitmap.getHeight();
		}
		
		public float getCropRight() {
			return (float)bottomRightCursor.x/(float)imageBitmap.getWidth();
		}
		
		public Rect getCropRect() {
			Rect theRect = new Rect();
			
			theRect.top = topLeftCursor.y;
			theRect.left = topLeftCursor.x;
			theRect.bottom = bottomRightCursor.y;
			theRect.right = bottomRightCursor.x;
			
			return theRect;
		}
		
		public void updateCropView() {
			Canvas canvas = null;
			
			try {
				canvas = getHolder().lockCanvas(null);
				synchronized (getHolder()) {
					this.onDraw(canvas);
				}
			}
			finally {
				if (canvas != null) {
					getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}
	}
	
	public static class CropCursor {
		int x;
		int y;
		
		public void moveCursor(int touchX, int touchY, int xBound, int yBound) {
			x = touchX;
			y = touchY;
			
			if(x < 1)
				x = 1;
			if(x > xBound-1)
				x = xBound-1;
			if(y < 1)
				y = 1;
			if(y > yBound-1)
				y = yBound-1;	
		}
	}
}