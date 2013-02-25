package net.osmand.data;

import net.osmand.osm.Entity;

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
