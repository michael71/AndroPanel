/*  (c) 2011, Michael Blank
 * 
 *  This file is part of SRCP Client.

    SRCP Client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SRCP Client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SRCPclient.  If not, see <http://www.gnu.org/licenses/>.

 */

package de.blankedv.andropanel;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;


import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.locolist;
import static de.blankedv.andropanel.AndroPanelApplication.selectedLoco;


public class AddLocoActivity extends Activity{
	
	// this activity is used for adding a new loco to the database 
	// or
	// for editing an existing loco
	// (depending on whether a loco in currentLocos is selected (edit) or not (add)

	

	private Button addButton, cancelButton, selIconButton;

	private EditText name, adr;
	private ImageView locoIconView; 
	private AndroPanelApplication app;

		private Loco myLoco;
	private boolean iconSelected;
	private TextView title;
	
	private LinearLayout lay_speedsteps;
	private LinearLayout lay_nfunc;

	private ArrayList<Loco> locos;

	private boolean editFlag = false; // ADD or EDIT activity ?

	// for loco icon
	private LocoIcon locoIcon;
	private Bitmap newLocoIconBM = null;
	private static final int TAKE_PICTURE = 1;
	private static final int SELECT_IMAGE = 2;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addloco);
		setUpViews();
	}


	private void setUpViews() {


		addButton = (Button)findViewById(R.id.add_button);
		cancelButton = (Button)findViewById(R.id.cancel_button);
		selIconButton = (Button)findViewById(R.id.selIcon_button);

		title = (TextView)findViewById(R.id.loco_title);
		
		iconSelected=false; //no loco icon initially selected


		app = ((AndroPanelApplication)getApplicationContext());

		editFlag = false;
		name = (EditText) findViewById(R.id.add_loco_name);
		adr = (EditText) findViewById(R.id.add_loco_adr);
		locoIconView = (ImageView)findViewById(R.id.loco_icon);


		// fill values with defaults if "EDIT" was selected (i.e. at least one Loco isSelected.)


		for (Loco l : locolist) {
			if (l.equals(selectedLoco)) {
				editFlag = true;

				addButton.setText(R.string.store_changes);
				myLoco = l;
				iconSelected = true; // because loco to edit already has an icon
			}
		}

		if (editFlag) {
			title.setText(R.string.edit);
			name.setText(myLoco.getName());
			adr.setText(""+myLoco.getAdr());
			Bitmap lbm = BitmapFactory.decodeResource(getResources(), R.drawable.genloco);

			locoIconView.setImageBitmap(lbm); // TODO myLoco.getIcon());

		}
		else {
			title.setText(R.string.add_new_loco);
			locoIconView.setImageResource(R.drawable.genloco);
		}

		selIconButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivityForResult(new Intent(Intent.ACTION_PICK, 
						MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
			}
		});

		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// getText() nicht vergessen!! (EditText) ist KEIN STRING !!!!
				String lname = name.getText().toString().trim();

				if(lname.length() == 0) {
					Toast.makeText(getBaseContext(), 
							R.string.error_enter_name_first, 
							Toast.LENGTH_LONG).show();
					return;
				}

				String ladr = adr.getText().toString().trim();
				if(ladr.length() == 0) {
					Toast.makeText(getBaseContext(), 
							R.string.error_enter_address_first, 
							Toast.LENGTH_LONG).show();
					return;
				}
				if(iconSelected == false) {
					// this is the default loco icon.
					Bitmap lbm = BitmapFactory.decodeResource(getResources(), R.drawable.genloco);
					newLocoIconBM = lbm;
				}

				int loco_adr = Integer.parseInt(ladr);


				
				if (editFlag)  {
					// update fields of myLoco
					myLoco.name = lname;
					myLoco.adr = loco_adr;

					//// TODO app.saveLoco(myLoco);  // update in database
					if (DEBUG) Log.d(TAG, "TODO updated loco a="+myLoco.getAdr()+" in DB.");
				}
				else
				{  // ADD

						myLoco = new Loco(lname, 3, loco_adr, newLocoIconBM );  // mass = 3
					locolist.add(myLoco);
					if (DEBUG) Log.d(TAG, "added loco a="+myLoco.getAdr()+" in DB.");
				}
				finish();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		}); 

	}

	private void getThumbnailPicture() {
		// actual use of this routine was disabled because too many possible formats with different phone hardware.
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	@Override
	protected void onActivityResult(int requestCode, 
			int resultCode, Intent data) {

		switch (requestCode)  {
		case SELECT_IMAGE :
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				if (selectedImage != null ) {
					if (DEBUG) Log.d(TAG, "image selected:" + selectedImage.toString());
				} else {
				    if (DEBUG) Log.d(TAG,"data.getData() is null (in selectedI)");
				}
				try {
					// if image larger than needed, sample down first.
					// 1. Decode ONLY image size
			        BitmapFactory.Options o = new BitmapFactory.Options();
			        o.inJustDecodeBounds = true;
			        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),null, o);
			        int scale = LocoIcon.calc_scale(o.outWidth, o.outHeight);
			        if (DEBUG) Log.d(TAG, "scaled foto by "+scale);
			        
			        //2. Decode with inSampleSize - much faster than scaling a large image.
			        BitmapFactory.Options o2 = new BitmapFactory.Options();
			        o2.inSampleSize = scale;
					locoIcon = new LocoIcon(BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),null,o2));  
					newLocoIconBM = locoIcon.getBitmap();   // we are using newLocoBM later.
					locoIconView.setImageBitmap(newLocoIconBM);
					iconSelected = true;
				} catch (FileNotFoundException e) {
					Log.e(TAG,"bitmap file not found.");
					Log.e(TAG, e.getClass().getName() + " "+ e.getMessage());
				} catch (OutOfMemoryError e) {
					Log.e(TAG,"bitmap file too large for memory");
					Log.e(TAG, e.getClass().getName() + " "+ e.getMessage());
				}
			} 
			break;
		}

	}




}

