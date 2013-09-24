package com.tanderegg.voterdrive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.leptonica.android.Enhance;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.AdaptiveMap;
//import com.googlecode.leptonica.android.Enhance;
import com.tanderegg.voterdrive.R;

import android.content.res.AssetManager;

import android.app.ListActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;

import java.util.ArrayList;

public class ScanActivity extends ListActivity {
	private static final String TESSBASE_PATH = "/sdcard/tesseract/";
	private static final String DEFAULT_LANGUAGE = "eng";
	private static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/" + DEFAULT_LANGUAGE + ".traineddata";
	public static final String ALPHANUMERIC = "0123456789ABCDEDFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALPHABET = "ABCDEDFGHIJKLMNOPQRSTUVWXYZ-";
	private static final String DATE = "0123456789/";
	
    //private static String license_number = "";
    //private String last_name;
    /*private String first_m_name;
    private String address;
    private String city_state_zip;
    private String dob;*/

    //private static Bitmap license_number_bmp	= null;
    //private Bitmap last_name_bmp 		= null;
    /*private Bitmap first_m_name_bmp 	= null;
    private Bitmap address_bmp 			= null;
    private Bitmap city_state_zip_bmp 	= null;
    private Bitmap dob_bmp 				= null;
    private Bitmap signature_bmp 		= null;*/
    
    private static String resultText = "";
    //private static Bitmap bmp=null;
	private static Bitmap cropped_bmp=null;
    
    private static ScannerAdapter adap;
	private static ArrayList<ScanResult> scanResultList;
	
	private static TessBaseAPI tessApi;
	private static boolean isOrientationChange = false;
	
	/* Method called when activity is first created */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent sourceIntent = getIntent();
		Bundle extraData = sourceIntent.getExtras();
		String imageFile;
		
		setContentView(R.layout.scan);
		
		if(!isOrientationChange) {
			scanResultList = new ArrayList<ScanResult>();
	        
	        adap = new ScannerAdapter(this, R.layout.scan_row, scanResultList);
			
			if(extraData.containsKey("file_path")) {
				imageFile = extraData.getString("file_path");
			}
			else {
				imageFile = "/mnt/sdcard/dcim/Camera/tiana_license_dl_number_simple.png";
			}
	        
			
			
	        // Ensure that language data is present
	        if(!(new File(EXPECTED_FILE).exists())) {
	        	
	        	String msg = "Tesseract trained data file not found.  Creating folder: " + TESSBASE_PATH + "tessdata/";
	        	System.out.println(msg);
	        	
	        	// Write file from assets folder to sdcard for Tesseract
	        	AssetManager assetManager = getAssets();
	        	InputStream in = null;
	        	OutputStream out = null;
	        	String filename = DEFAULT_LANGUAGE + ".traineddata";
	        	
	        	File tesseract_dir = new File(TESSBASE_PATH);
	        	tesseract_dir = new File(tesseract_dir, "tessdata");
	        	tesseract_dir.mkdirs();
	        	
	        	try {
		        	in = assetManager.open(filename);
		        	out = new FileOutputStream(TESSBASE_PATH + "tessdata/" + filename);
		        	copyFile(in, out);
		        	in.close();
		        	in = null;
		        	out.flush();
		        	out.close();
		        	out = null;
	        	} catch (Exception e) {
	        		msg = "Error: " + e.getMessage();
	        		System.out.println(msg);
	        	}
	        }
	        
	        if(!(new File(imageFile).exists())) {
	        	resultText = "Error: image file not found";
	        }
	        else {
	        	// Initialize Tesseract-OCR
		        tessApi = new TessBaseAPI();
		        tessApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
		        
		        Bitmap bmp=null;
		        
		        float rotation = extraData.getFloat("cropRotation");
		        
		        Matrix matrix = new Matrix();
		        matrix.setRotate(rotation);
		        
		        // Load designated image file
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				long memsize = Runtime.getRuntime().maxMemory();
				
				if(memsize > 47*1024*1024) {
					options.inSampleSize = 1;
					String msg = "Memory 48 MB or greater, sample size = 1";
					System.out.println(msg);
				}
				else if(memsize > 32*1024*1024) {
					options.inSampleSize = 2;
					String msg = "Memory 32 MB or greater, sample size = 2";
					System.out.println(msg);
				}
				else {
					options.inSampleSize = 4;
					String msg = "Memory 16 MB or greater, sample size = 4";
					System.out.println(msg);
				}
				
				options.inDither = false;
				
		        // Load designated image file
		        bmp = BitmapFactory.decodeFile(imageFile, options);
		        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
		        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
		        
		        int top = (int)((float)bmp.getHeight()*extraData.getFloat("cropRectTop"));
		        int left = (int)((float)bmp.getWidth()*extraData.getFloat("cropRectLeft"));
		        int bottom = (int)((float)bmp.getHeight()*extraData.getFloat("cropRectBottom"));
		        int right = (int)((float)bmp.getWidth()*extraData.getFloat("cropRectRight"));
		        
		        String msg = "top: " + top + "left: " + left + "bottom: " + bottom + "right: " + right;
        		System.out.println(msg);
		        
		        int width = right - left;
		        int height = bottom - top;
		        
		        if(cropped_bmp != null) {
		        	cropped_bmp.recycle();
		        	cropped_bmp = null;
		        }
		        
		        //cropped_bmp = Bitmap.createBitmap(bmp, 168, 282, 2277, 1452);
		        cropped_bmp = Bitmap.createBitmap(bmp, left, top, width, height);
		        
		        bmp.recycle();
		        bmp=null;
		        
		        //int width = 2277;
		        //int height = 1452;
		        
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.400527009*width), (int)(0.183884298*height), (int)(0.219587176*width), (int)(0.075757576*height), ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.400527009*width), (int)(0.373966942*height), (int)(0.351339482*width), (int)(0.065426997*height), ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.400527009*width), (int)(0.432506887*height), (int)(0.351339482*width), (int)(0.061983471*height), ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.356609574*width), (int)(0.491046832*height), (int)(0.219587176*width), (int)(0.041322314*height), ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.356609574*width), (int)(0.52892562*height),  (int)(0.351339482*width), (int)(0.048209366*height), ALPHANUMERIC + ","));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.422485727*width), (int)(0.584022039*height), (int)(0.219587176*width), (int)(0.061983471*height), DATE));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, (int)(0.036012297*width), (int)(0.811294766*height), (int)(0.307422047*width), (int)(0.123966942*height), ALPHABET));
		        adap.notifyDataSetChanged();
		        
		        /*scanResultList.add(scanRegion(cropped_bmp, 912, 267, 500, 110, ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 912, 543, 800, 95, ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 912, 628, 800, 90, ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 812, 713, 500, 60, ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 812, 768, 800, 70, ALPHANUMERIC + ","));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 962, 848, 500, 90, DATE));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(cropped_bmp, 82, 1178, 700, 180, ALPHABET));
		        adap.notifyDataSetChanged();*/
		        
		        /*scanResultList.add(scanRegion(bmp, 1080, 549, 500, 110, ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 1080, 825, 800, 95, ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 1080, 910, 800, 90, ALPHABET));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 980, 995, 500, 60, ALPHANUMERIC));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 980, 1050, 800, 70, ALPHANUMERIC + ","));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 1130, 1130, 500, 90, DATE));
		        adap.notifyDataSetChanged();
		        scanResultList.add(scanRegion(bmp, 250, 1460, 700, 180, ALPHABET));
		        adap.notifyDataSetChanged();*/
		        
		        tessApi.end();
	        }
		}
		
		isOrientationChange = false;
        
		setListAdapter(adap);
		
		ImageView iv = (ImageView)findViewById(R.id.full_source_image);
        iv.setImageBitmap(cropped_bmp);
		
        TextView tv = (TextView)findViewById(R.id.error_text);
        tv.setText(resultText);
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		isOrientationChange = true;
		return null;		
	}
	
	protected static ScanResult scanRegion(Bitmap bmp, int left, int top, int width, int height, String whitelist) {
		ScanResult result = new ScanResult();
		Bitmap bmpRegion;
		String resultText;
		
		Pix pix=null;
		
		bmpRegion = Bitmap.createBitmap(bmp, left, top, width, height);
        bmpRegion = bmpRegion.copy(Bitmap.Config.ARGB_8888, true);
		
		pix = ReadFile.readBitmap(bmpRegion);
		//pix = Enhance.unsharpMasking(pix, 1, 0.2f);
		
		/*try {
			pix = AdaptiveMap.backgroundNormMorph(pix, 16, 8, 200);
		} catch (Exception e) {
			String msg = "Error: " + e;
			System.out.println(msg);
		}*/
		
		
		try {
			pix = Binarize.otsuAdaptiveThreshold(pix, 600, 600, 0, 0, 0.01f);
		} catch (Exception e) {
			String msg = "Error: " + e;
			System.out.println(msg);
		}
		
        bmpRegion = WriteFile.writeBitmap(pix);
        
        tessApi.setImage(bmpRegion);
        tessApi.setVariable("tessedit_char_whitelist", whitelist);
        
        resultText = tessApi.getUTF8Text();
        
        int confidence = tessApi.meanConfidence();
        resultText = resultText + ", " + String.valueOf(confidence);
        
        result.sourceImage = bmpRegion;
        result.resultText = resultText;
        
        tessApi.clear();
        pix.recycle();
        pix = null;
		
		return result;
	}
	
	public class ScannerAdapter extends ArrayAdapter<ScanResult> {
		private LayoutInflater mInflater;
		//private Context context;
		private int layout;
		
		private ArrayList<ScanResult> arrayScannerList;
		
		public ScannerAdapter(Context context, int layout, ArrayList<ScanResult> arrayScannerList) {
			super(context, layout, arrayScannerList);
			
			// Cache the LayoutInflater to avoid asking for a new one each time
			mInflater = LayoutInflater.from(context);
			this.arrayScannerList = arrayScannerList;
			this.layout = layout;
			//this.context = context;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			
			if (convertView == null) {
				convertView = mInflater.inflate(layout, null);
				
				viewHolder = new ViewHolder();
				viewHolder.imageLine = (ImageView)convertView.findViewById(R.id.source_image);
				viewHolder.textLine = (TextView)convertView.findViewById(R.id.result_text);
				
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			if(viewHolder != null) {
				ScanResult result = arrayScannerList.get(position);
				
				viewHolder.imageLine.setImageBitmap(result.sourceImage);
				viewHolder.textLine.setText(result.resultText);	
			}
			
			return convertView;
		}
		
		class ViewHolder {
			ImageView imageLine;
			TextView textLine;
		}
		
		@Override
		public Filter getFilter() {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public int getCount() {
			return arrayScannerList.size();
		}
		
		@Override
		public ScanResult getItem(int position) {
			return arrayScannerList.get(position);
		}
	}
	
	static class ScanResult {
		Bitmap sourceImage;
		String resultText;
	}
}
