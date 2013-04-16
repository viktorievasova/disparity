package com.vasova.bachelorproject;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {

	public boolean sum_of_absolute_differences;
	public boolean mutual_information;
	
		
	private CheckBox checkbox_soad,  checkbox_mi;
	private CheckBox[] checkboxArray = new CheckBox[2];  
	private Button confirmButton;
	 
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		this.setTitle("Settings");
		
		checkbox_soad = (CheckBox) findViewById(R.id.checkbox_sum_of_absolute_differences);
		checkbox_mi = (CheckBox) findViewById(R.id.checkbox_mutual_information);
		
		checkbox_soad.setChecked(GalleryActivity.registration.isSetSumOfAbsoluteDiff());
		checkbox_mi.setChecked(GalleryActivity.registration.isSetMutualInformation());
		
		checkboxArray[0] = checkbox_soad;
		checkboxArray[1] = checkbox_mi;
		
		addListenerOnCheckBox();
		
		confirmButton = (Button) findViewById(R.id.confirmButton);
		addListenerOnButton();
	  }
	 
	  public void addListenerOnCheckBox() {
		  OnClickListener clickListener= new OnClickListener() {
			  @Override
			  public void onClick(View v) {
				  int checkedId = v.getId();
			      for (int i = 0; i < checkboxArray.length; i++) {
			            CheckBox currentCheckbox = checkboxArray[i];
			            if (currentCheckbox.getId() == checkedId) {
			                 currentCheckbox.setChecked(true);
			                 if(currentCheckbox == checkbox_soad){
			                	 sum_of_absolute_differences = true;
			                	 mutual_information = false;
			                 }else{
			                	 sum_of_absolute_differences = false;
			                	 mutual_information = true;
			                 }
			            } else {
			                 currentCheckbox.setChecked(false);
			            }
			       }   
		   
			  }
		  };
		  checkbox_soad.setOnClickListener(clickListener);
		  checkbox_mi.setOnClickListener(clickListener);
	 
	  }
	

		
	  public void addListenerOnButton() {
		  confirmButton.setOnClickListener(new OnClickListener() {
		      //Run when button is clicked
			  @Override
			  public void onClick(View v) {
				  
				  if (sum_of_absolute_differences){
					  GalleryActivity.registration.setRegistrationParametr(Registration.soad_string);
				  }else if (mutual_information){
					  GalleryActivity.registration.setRegistrationParametr(Registration.mi_string);
				  }
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
		
