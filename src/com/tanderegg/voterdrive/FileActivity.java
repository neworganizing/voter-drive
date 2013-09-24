package com.tanderegg.voterdrive;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.tanderegg.voterdrive.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;


public class FileActivity extends ListActivity {
	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.file);
        myPath = (TextView)findViewById(R.id.path);
        getDir(root);
    }
    
    private void getDir(String dirPath) {
    	myPath.setText("Location: " + dirPath);
    	
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
    	
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
    	
    	if(!dirPath.equals(root)) {
    		item.add(root);
    		path.add(root);
    		item.add("../");
    		path.add(f.getParent());
    	}
    	
    	for(int i=0; i < files.length; i++) {
    		File file = files[i];
    		path.add(file.getPath());
    		
    		if(file.isDirectory())
    			item.add(file.getName() + "/");
    		else
    			item.add(file.getName());
    	}
    	
    	ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.file_row, item);
    	setListAdapter(fileList);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final File file = new File(path.get(position));
    	
    	final View view = v;
    	
    	if (file.isDirectory()) {
    		if(file.canRead()) {
    			getDir(path.get(position));
    		}
    		else {
    			new AlertDialog.Builder(this)
    				.setIcon(R.drawable.icon)
    				.setTitle("[" + file.getName() + "] folder can't be read!")
    				.setPositiveButton("OK", 
    					new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
        						// TODO Auto-generated method stub
    						}
    				}).show();
    		}
    	}
    	else {
    		new AlertDialog.Builder(this)
    			.setIcon(R.drawable.icon)
    			.setTitle("[" + file.getName() + "]")
    			.setPositiveButton("OK",
    				new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
							Intent fileIntent = new Intent(view.getContext(), CropActivity.class);
							fileIntent.putExtra("file_path", file.getAbsolutePath());
			        		startActivityForResult(fileIntent, 0);
    					}
    			}).show();
    	}
    }
}
