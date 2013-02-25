package net.osmand.data;

import net.osmand.osm.edit.Entity;

public class TransportStop extends MapObject {
	int[] referencesToRoutes = null;
	
	public TransportStop(){
	}
	
	public int[] getReferencesToRoutes() {
		return referencesToRoutes;
	}
	
	public void setReferencesToRoutes(int[] referencesToRoutes) {
		this.referencesToRoutes = referencesToRoutes;
	}

}
