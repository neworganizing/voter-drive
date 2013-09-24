package com.tanderegg.voterdrive;

import java.io.File;

import com.tanderegg.voterdrive.R;

import android.app.Activity;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.Intent;

public class VoterDriveActivity extends Activity {
	private Button fileButton;
	private Button cameraButton;
	private String path = "/sdcard/DCIM/Camera/voteridcapture.jpg";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        fileButton = (Button)findViewById(R.id.file_button);
        
        fileButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Intent fileIntent = new Intent(v.getContext(), FileActivity.class);
        		startActivityForResult(fileIntent, 0);
        	}
        });
        
        cameraButton = (Button)findViewById(R.id.camera_button);
        
        cameraButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		File file = new File(path);
        		Uri outputFileUri = Uri.fromFile(file);
        		
        		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        		startActivityForResult(cameraIntent, 0);
        	}
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
		switch(resultCode) 
		{
			case 0:
				break;
			case -1:
				Intent fileIntent = new Intent(this, CropActivity.class);
				fileIntent.putExtra("file_path", path);
        		startActivityForResult(fileIntent, 0);
				break;
		}
    }
}