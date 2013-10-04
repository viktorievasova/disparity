package com.vasova.bachelorproject;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This activity can be used for setting parameters of the calculation disparity map by the user. 
 * @author viktorievasova
 *
 */
public class SettingsActivity extends Activity {

	private Button confirmButton;
	 
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		this.setTitle("Settings");
		
		confirmButton = (Button) findViewById(R.id.confirmButton);
		addListenerOnConfirmButton();
	  }
	 

	  private void addListenerOnConfirmButton() {
		  confirmButton.setOnClickListener(new OnClickListener() {
			  @Override
			  public void onClick(View v) {
				 finish();
			  }
		  });
	  }
	  
	  @Override
	  public void onPause(){
		  super.onPause();
	  }
	  
	  @Override
	  public void onResume(){
		  super.onResume();
	  }
	  
	  @Override
	  public void onStop(){
		  super.onStop();
	  }
	  
}
		
