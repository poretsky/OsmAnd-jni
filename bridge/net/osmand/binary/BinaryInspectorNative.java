package net.osmand.binary;


import java.io.IOException;

import net.osmand.NativeLibrary;
import net.osmand.bridge.ObfInspector;
import net.osmand.bridge.StringVector;

public class BinaryInspectorNative {
	

	public static final int BUFFER_SIZE = 1 << 20;
	
	public static void main(String[] args) throws IOException {
		// test cases show info
		NativeLibrary.load(null);
		StringVector vector = new StringVector();
		for(int i = 0; i < args.length; i++) {
			vector.add(args[i]);
		}
		ObfInspector.inspector(vector);
		
		
//		inspector(new String[]{"-vmap", /*"-bbox=11.3,49,11.6,47",*/ "/home/victor/projects/OsmAnd/data/osm-gen/Map.obf"});
		// test case extract parts
		// test case 
	}
}
