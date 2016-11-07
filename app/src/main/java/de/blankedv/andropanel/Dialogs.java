package de.blankedv.andropanel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import static de.blankedv.andropanel.AndroPanelApplication.appContext;
import static de.blankedv.andropanel.AndroPanelApplication.configHasChanged;
import static de.blankedv.andropanel.AndroPanelApplication.locolist;

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
        final CheckBox cbInverted = (CheckBox) selSxAddressView.findViewById(R.id.cbInverted);
        final TextView tvInverted = (TextView) selSxAddressView.findViewById(R.id.tvInverted);
        if (el.getInverted() == 1) {
            cbInverted.setChecked(true);
        } else {
            cbInverted.setChecked(false);
        }
        if (el.getType().equalsIgnoreCase("turnout")) {
            // inverted is only used for turnout
            cbInverted.setVisibility(View.VISIBLE);
            tvInverted.setVisibility(View.VISIBLE);
        } else {
            // hidden for all other panel element
            cbInverted.setVisibility(View.INVISIBLE);
            tvInverted.setVisibility(View.INVISIBLE);
        }


		final NumberPicker sxAddress = (NumberPicker) selSxAddressView.findViewById(R.id.picker1);
		sxAddress.setRange(1,103);
		sxAddress.setSpeed(100); // faster change for long press
		final NumberPicker sxBit = (NumberPicker) selSxAddressView.findViewById(R.id.picker2);
		sxBit.setRange(1,8);
		final SXPanelElement e = (SXPanelElement) el;

		sxAddress.setCurrent(e.getSxAdr());
		sxBit.setCurrent(e.getSxBit());

		AlertDialog sxDialog = new AlertDialog.Builder(appContext)
                .setMessage("SX Address ?")
                .setCancelable(false)
		       .setView(selSxAddressView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
		        	   e.setSxAdr(sxAddress.getCurrent());
		        	   e.setSxBit(sxBit.getCurrent());
                       if (e.getType().equalsIgnoreCase("turnout") && cbInverted.isChecked()) {
                           e.setInverted(1);
                       } else {
                           e.setInverted(0);
                       }
                       configHasChanged = true; // flag for saving the configuration later when pausing the activity
                        dialog.dismiss();
		        	   
		           }
		       })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		                dialog.dismiss();
		           }
		       })
		       .create();
		sxDialog.show();
		sxDialog.getWindow().setLayout(350,400);
	}
 
}
