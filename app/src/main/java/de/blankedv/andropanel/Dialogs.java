package de.blankedv.andropanel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.appContext;
import static de.blankedv.andropanel.AndroPanelApplication.configHasChanged;
import static de.blankedv.andropanel.AndroPanelApplication.locolist;
import static de.blankedv.andropanel.AndroPanelApplication.selectedLoco;

/**
 * Dialog function for selection of a new locomotive
 * and for
 * Setting the SX Address of a turnout or sensor panel element
 *
 * @author mblank
 */
public class Dialogs {

    private static int selLocoIndex;
    private static final int NOTHING = 99999;


    static void selectLocoDialog() {

        final LayoutInflater factory = LayoutInflater.from(appContext);
        final View selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_loco_from_list, null);
        final Spinner selLoco = (Spinner) selSxAddressView.findViewById(R.id.spinner);

        String[] locosToSelect = new String[locolist.size() + 1];

        int index = 0;
        for (Loco l : locolist) {
            locosToSelect[index] = l.name + " (" + l.adr + ")";
            index++;
        }
        locosToSelect[index] = "+ NEUE LOK (3)";
        final int NEW_LOCO = index;

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
                //.setMessage("Lok auswählen - "+locolist.name)
                .setCancelable(false)
                .setView(selSxAddressView)
                .setPositiveButton("Auswählen", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        if (selLocoIndex == NEW_LOCO) {
                            dialog.dismiss();
                            Loco l = new Loco();
                            locolist.add(l);
                            selectedLoco = l;
                            openEditDialog();
                        } else if (selLocoIndex != NOTHING) {
                            //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                            //		+" wurde selektiert", Toast.LENGTH_SHORT)
                            //		.show();
                            selectedLoco = locolist.get(selLocoIndex);
                            selectedLoco.initFromSX();
                            dialog.dismiss();
                        }


                    }
                })
                .setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (selLocoIndex == NEW_LOCO) {
                            Loco l = new Loco();
                            locolist.add(l);
                            selectedLoco = l;
                        } else if (selLocoIndex != NOTHING) {
                            //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                            //		+" wurde selektiert", Toast.LENGTH_SHORT)
                            //		.show();
                            selectedLoco = locolist.get(selLocoIndex);
                         }
                        dialog.dismiss();
                        openEditDialog();
                    }
                })
                .setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();

                    }
                })

                .show();
    }


    static void openEditDialog() {

        final LayoutInflater factory = LayoutInflater.from(appContext);
        final View selSxAddressView = factory.inflate(R.layout.alert_dialog_edit, null);
        final EditText lName = (EditText) selSxAddressView.findViewById(R.id.setname);
        final NumberPicker sxAddress = (NumberPicker) selSxAddressView.findViewById(R.id.picker1);
        sxAddress.setMinValue(1);
        sxAddress.setMaxValue(99);
        sxAddress.setWrapSelectorWheel(false);
        final NumberPicker mass = (NumberPicker) selSxAddressView.findViewById(R.id.picker2);
        mass.setMinValue(1);
        mass.setMaxValue(5);


        sxAddress.setValue(selectedLoco.adr);
        mass.setValue(selectedLoco.mass);

        AlertDialog sxDialog = new AlertDialog.Builder(appContext)
                //.setMessage("")
                .setCancelable(false)
                .setView(selSxAddressView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //e.setSxAdr(sxAddress.getValue());
                        //e.setSxBit(sxBit.getValue());

                        configHasChanged = true; // flag for saving the configuration later when pausing the activity
                        selectedLoco.initFromSX();
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
        sxAddress.setMinValue(1);
        sxAddress.setMaxValue(103);
        sxAddress.setWrapSelectorWheel(false);
        final NumberPicker sxBit = (NumberPicker) selSxAddressView.findViewById(R.id.picker2);
        sxBit.setMinValue(1);
        sxBit.setMaxValue(8);
        final SXPanelElement e = (SXPanelElement) el;

        sxAddress.setValue(e.getSxAdr());
        sxBit.setValue(e.getSxBit());

        AlertDialog sxDialog = new AlertDialog.Builder(appContext)
                //.setMessage("")
                .setCancelable(false)
                .setView(selSxAddressView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        e.setSxAdr(sxAddress.getValue());
                        e.setSxBit(sxBit.getValue());
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
        //sxDialog.getWindow().setLayout(350, 400);
    }

}
