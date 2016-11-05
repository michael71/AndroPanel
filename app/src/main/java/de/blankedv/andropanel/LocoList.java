package de.blankedv.andropanel;

import java.util.ArrayList;

public class LocoList {
	public ArrayList<Loco> locos = new ArrayList<Loco>();
	public String name = "";
	public Loco selectedLoco = null;
	
	public int size() {
		return locos.size();
	}
	
	public Loco get(int i) {
		if (i < locos.size()) {
			return locos.get(i);
		} else {
			return null;
		}

	}
}
