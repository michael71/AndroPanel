package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Dialog function for selection of a new locomotive
 * and for
 * Setting the SX Address of a turnout or sensor panel element
 * 
 * @author mblank
 *
 */
public class Dialogs {
	
	private static int selLocoIndex; 
	private static final int NOTHING = 99999;
	

	static void selectLocoDialog() {

		final LayoutInflater factory = LayoutInflater.from(appContext);
		final View selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_loco_from_list, null);
		final Spinner selLoco = (Spinner) selSxAddressView.findViewById(R.id.spinner);

		String[] locosToSelect = new String[locolist.locos.size()];

		int index = 0;
		for (Loco l : locolist.locos) {
			locosToSelect[index] = l.name+" ("+l.adr+")";
		    index++;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(appContext,
		        android.R.layout.simple_spinner_dropdown_item,
		        locosToSelect);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selLoco.setAdapter(adapter);
		
		selLocoIndex = NOTHING;
		selLoco.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selLocoIndex = arg2;   // save for later use when "SAVE" pressed
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				selLocoIndex = NOTHING;
			}
		});

 
		AlertDialog sxDialog = new AlertDialog.Builder(appContext)    
		//, R.style.Animations_GrowFromBottom ) => does  not work
		.setMessage("Lok auswählen - "+locolist.name)
		.setCancelable(false)
		.setView(selSxAddressView)
		.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				if (selLocoIndex != NOTHING) {
 					//Toast.makeText(appContext,"Loco-index="+selLocoIndex
 					//		+" wurde selektiert", Toast.LENGTH_SHORT)
 					//		.show();
					locolist.selectedLoco = locolist.locos.get(selLocoIndex);
					locolist.selectedLoco.initFromSX();
				}

				dialog.dismiss();
			}
		})
		.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//dialog.cancel();
				dialog.dismiss();
			}
		})
		
		.create();
		sxDialog.getWindow().getAttributes().windowAnimations = R.style.Animations_GrowFromRight;  // must be called before show()
		sxDialog.getWindow().setGravity(Gravity.RIGHT);
		sxDialog.show();
		sxDialog.getWindow().setLayout(350,250);


	}
	
	// selectSXAddressDialog is used when editing the address of panel elements
	// needed for edit mode for sx-controls
 	static void selectSXAddressDialog(PanelElement el) {

    	final LayoutInflater factory = LayoutInflater.from(appContext);
		final View selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_sx_address, null);
		final NumberPicker sxAddress = (NumberPicker) selSxAddressView.findViewById(R.id.picker1);
		sxAddress.setRange(1,103);
		sxAddress.setSpeed(100); // faster change for long press
		final NumberPicker sxBit = (NumberPicker) selSxAddressView.findViewById(R.id.picker2);
		sxBit.setRange(1,8);
		final SXPanelElement e = (SXPanelElement) el;
		sxAddress.setCurrent(e.getSxAdr());
		sxBit.setCurrent(e.getSxBit());
		AlertDialog sxDialog = new AlertDialog.Builder(appContext)
               .setMessage("SX Adresse?")
		       .setCancelable(false)
		       .setView(selSxAddressView)
		       .setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
//		        	   Toast.makeText(appContext,"Adresse "+sxAddress.getCurrent()
//		        			   +"/"+sxBit.getCurrent()+" wurde selektiert", Toast.LENGTH_SHORT)
//		        	   .show();
		        	   e.setSxAdr(sxAddress.getCurrent());
		        	   e.setSxBit(sxBit.getCurrent());
		        	   configHasChanged = true; // flag for saving the configuration later when pausing the activity
		        	   dialog.dismiss();
		        	   
		           }
		       })
		       .setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		                dialog.dismiss();
		           }
		       })
		       .create();
		sxDialog.show();
		sxDialog.getWindow().setLayout(350,400);
	}
 
/*	OLD CODE WITH ADDRESS SELECTION
 * 
 *      static void selectLocoAddressDialog() {

		final int oldAddress = locoAdr;

		final LayoutInflater factory = LayoutInflater.from(appContext);
		final View selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_loco, null);
		final NumberPicker sxAddress = (NumberPicker) selSxAddressView.findViewById(R.id.picker1);
		final Spinner selLoco = (Spinner) selSxAddressView.findViewById(R.id.spinner);


		String[] locosToSelect = new String[locos.size()];

		int index = 0;
		for (Loco l : locos) {
			locosToSelect[index] = l.name;
		    index++;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(appContext,
		        android.R.layout.simple_spinner_dropdown_item,
		        locosToSelect);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selLoco.setAdapter(adapter);
		selLocoIndex = 0;
		selLoco.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selLocoIndex = arg2;   // save for later use when "SAVE" pressed
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				selLocoIndex = 0;
			}
		});

		sxAddress.setRange(1,99);
		sxAddress.setSpeed(100); // faster change for long press
		sxAddress.setCurrent(locoAdr);
 
		AlertDialog sxDialog = new AlertDialog.Builder(appContext)     //, R.style.Animations_GrowFromBottom ) => does also not work
		.setMessage("Lok oder SX-Adresse auswählen")
		.setCancelable(false)
		.setView(selSxAddressView)
		.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
			

			public void onClick(DialogInterface dialog, int id) {
				if (sxAddress.getCurrent() != oldAddress) {
//					Toast.makeText(appContext,"Adresse "+sxAddress.getCurrent()
//							+" wurde selektiert", Toast.LENGTH_SHORT)
//							.show();
					locoAdr = sxAddress.getCurrent();
					// check if this adr is already in locos list.
					boolean contains=false;
					for (int i=0; i<locos.size(); i++) {
						if (locos.get(i).adr == locoAdr) {
							contains=true;
							break;
						}
					}
					if (!contains) locos.add(new Loco(locoAdr,2));

					
				} else if (selLocoIndex > 0) {
//					Toast.makeText(appContext,"Loco-index="+selLocoIndex
//							+" wurde selektiert", Toast.LENGTH_SHORT)
//							.show();
					locoAdr = locos.get(selLocoIndex).adr;
				}

				dialog.dismiss();
			}
		})
		.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//dialog.cancel();
				dialog.dismiss();
			}
		})
		
		//   funktioniert nicht - daher spinner in layout
		//		       .setSingleChoiceItems(adrs, 0, new DialogInterface.OnClickListener() {
		//		           public void onClick(DialogInterface dialog, int id) {
		//		        	   Toast.makeText(appContext,"Item "+sxAddress.getCurrent()
		//		        			   +" wurde selektiert", Toast.LENGTH_SHORT)
		//		        	   .show();
		//		        	   locoAdr = id+50; //sxAddress.getCurrent();
		//
		//		        	   dialog.dismiss();
		//		           }
		//		       })
		.create();
		sxDialog.getWindow().getAttributes().windowAnimations = R.style.Animations_GrowFromRight;  // must be called before show()
		sxDialog.getWindow().setGravity(Gravity.RIGHT);
		sxDialog.show();
		sxDialog.getWindow().setLayout(350,400);


	}
	
*/
}
