package net.osmand.router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


import net.osmand.binary.BinaryMapRouteReaderAdapter.RouteRegion;
import net.osmand.binary.BinaryMapRouteReaderAdapter.RouteTypeRule;
import net.osmand.binary.RouteDataObject;
import net.osmand.router.BinaryRoutePlanner.RouteSegment;
import net.osmand.util.MapUtils;

public class NewGeneralRouter extends VehicleRouter {
	
	public Map<String, RouteEvaluationRule> highwaySpeed ;
	public Map<String, RouteEvaluationRule> highwayPriorities ;
	public Map<String, RouteEvaluationRule> avoid ;
	public Map<String, RouteEvaluationRule> obstacles;
	public Map<String, RouteEvaluationRule> routingObstacles;
	public Map<String, RouteEvaluationRule> oneway;
	public Map<String, String> attributes;
	
	public Map<RoutingParameter, Object> parameters = new LinkedHashMap<RoutingParameter, Object>(); 
	
	
	public static class RoutingParameter {
		public String id;
		public String name;
		public String description;
		public RoutingParameterType type;
		public String[] possibleValues;
		public String[] possibleValueDescriptions;
	}
	
	public enum RoutingParameterType {
		NUMERIC,
		BOOLEAN,
		SYMBOLIC
	}
	
	private static class RouteEvaluationRule {
		
	}
	
	private GeneralRouterProfile profile;

	// cached values
	private boolean restrictionsAware = true;
	private float leftTurn;
	private float roundaboutTurn;
	private float rightTurn;
	private float minDefaultSpeed = 10;
	private float maxDefaultSpeed = 10;

	public enum GeneralRouterProfile {
		CAR,
		PEDESTRIAN,
		BICYCLE
	}
	
	public NewGeneralRouter(GeneralRouterProfile profile, Map<String, String> attributes) {
		this.attributes = new LinkedHashMap<String, String>();
		this.profile = profile;
		highwaySpeed = new LinkedHashMap<String, RouteEvaluationRule>();
		highwayPriorities = new LinkedHashMap<String, RouteEvaluationRule>();
		avoid = new LinkedHashMap<String, RouteEvaluationRule>();
		obstacles = new LinkedHashMap<String, RouteEvaluationRule>();
		routingObstacles = new LinkedHashMap<String, RouteEvaluationRule>();
		Iterator<Entry<String, String>> e = attributes.entrySet().iterator();
		while(e.hasNext()){
			Entry<String, String> next = e.next();
			addAttribute(next.getKey(), next.getValue());
		}
	}

	public void addAttribute(String k, String v) {
		attributes.put(k, v);
		if(k.equals("restrictionsAware")) {
			restrictionsAware = parseSilentBoolean(v, restrictionsAware);
		} else if(k.equals("leftTurn")) {
			leftTurn = parseSilentFloat(v, leftTurn);
		} else if(k.equals("rightTurn")) {
			rightTurn = parseSilentFloat(v, rightTurn);
		} else if(k.equals("roundaboutTurn")) {
			roundaboutTurn = parseSilentFloat(v, roundaboutTurn);
		} else if(k.equals("minDefaultSpeed")) {
			minDefaultSpeed = parseSilentFloat(v, minDefaultSpeed * 3.6f) / 3.6f;
		} else if(k.equals("maxDefaultSpeed")) {
			maxDefaultSpeed = parseSilentFloat(v, maxDefaultSpeed * 3.6f) / 3.6f;
		}
	}

	@Override
	public boolean acceptLine(RouteDataObject way) {
		int[] types = way.getTypes();
		RouteRegion reg = way.region;
		// TODO
		return true;
	}
	
	@Override
	public boolean restrictionsAware() {
		return restrictionsAware;
	}
	
	@Override
	public float defineObstacle(RouteDataObject road, int point) {
		// TODO
		return 0;
	}
	
	@Override
	public float defineRoutingObstacle(RouteDataObject road, int point) {
		// TODO
		return 0;
	}
	
	@Override
	public int isOneWay(RouteDataObject road) {
		// TODO
		return 0;
	}

	
	private static boolean parseSilentBoolean(String t, boolean v) {
		if (t == null || t.length() == 0) {
			return v;
		}
		return Boolean.parseBoolean(t);
	}

	private static float parseSilentFloat(String t, float v) {
		if (t == null || t.length() == 0) {
			return v;
		}
		return Float.parseFloat(t);
	}

	@Override
	public float defineSpeed(RouteDataObject road) {
		// TODO
		return 0;
	}

	@Override
	public float defineSpeedPriority(RouteDataObject road) {
		return 0;
	}

	@Override
	public float getMinDefaultSpeed() {
		return minDefaultSpeed;
	}

	@Override
	public float getMaxDefaultSpeed() {
		return maxDefaultSpeed ;
	}

	
	public double getLeftTurn() {
		return leftTurn;
	}
	
	public double getRightTurn() {
		return rightTurn;
	}
	public double getRoundaboutTurn() {
		return roundaboutTurn;
	}
	@Override
	public double calculateTurnTime(RouteSegment segment, int segmentEnd, RouteSegment prev, int prevSegmentEnd) {
		int[] pt = prev.getRoad().getPointTypes(prevSegmentEnd);
		if(pt != null) {
			RouteRegion reg = prev.getRoad().region;
			for(int i=0; i<pt.length; i++) {
				RouteTypeRule r = reg.quickGetEncodingRule(pt[i]);
				if("highway".equals(r.getTag()) && "traffic_signals".equals(r.getValue())) {
					// traffic signals don't add turn info 
					return 0;
				}
			}
		}
		double rt = getRoundaboutTurn();
		if(rt > 0 && !prev.getRoad().roundabout() && segment.getRoad().roundabout()) {
			return rt;
		}
		if (getLeftTurn() > 0 || getRightTurn() > 0) {
			double a1 = segment.getRoad().directionRoute(segment.getSegmentStart(), segment.getSegmentStart() < segmentEnd);
			double a2 = prev.getRoad().directionRoute(prevSegmentEnd, prevSegmentEnd < prev.getSegmentStart());
			double diff = Math.abs(MapUtils.alignAngleDifference(a1 - a2 - Math.PI));
			// more like UT
			if (diff > 2 * Math.PI / 3) {
				return getLeftTurn();
			} else if (diff > Math.PI / 2) {
				return getRightTurn();
			}
			return 0;
		}
		return 0;
	}
	

	@Override
	public NewGeneralRouter specifyParameter(String parameter) {
		// TODO
		return this;
	}
	
	@Override
	public boolean containsAttribute(String attribute) {
		return attributes.containsKey(attribute);
	}
	
	@Override
	public String getAttribute(String attribute) {
		return attributes.get(attribute);
	}
	

}

