package net.osmand.osm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


import net.osmand.PlatformUtil;
import net.osmand.data.AmenityType;

import org.apache.commons.logging.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * reference : http://wiki.openstreetmap.org/wiki/Map_Features
 * Describing types of polygons :
 * 1. Last 2 bits define type of element : polygon, polyline, point 
 */
public class MapRenderingTypes {

	private static final Log log = PlatformUtil.getLog(MapRenderingTypes.class);
	
	
	private static char TAG_DELIMETER = '/'; //$NON-NLS-1$
	
	private String resourceName = null;
	public final static byte RESTRICTION_NO_RIGHT_TURN = 1;
	public final static byte RESTRICTION_NO_LEFT_TURN = 2;
	public final static byte RESTRICTION_NO_U_TURN = 3;
	public final static byte RESTRICTION_NO_STRAIGHT_ON = 4;
	public final static byte RESTRICTION_ONLY_RIGHT_TURN = 5;
	public final static byte RESTRICTION_ONLY_LEFT_TURN = 6;
	public final static byte RESTRICTION_ONLY_STRAIGHT_ON = 7;
	

	// stored information to convert from osm tags to int type
	private Map<String, MapRulType> types = null;
	private List<MapRulType> typeList = new ArrayList<MapRenderingTypes.MapRulType>();
	private List<MapRouteTag> routeTags = new ArrayList<MapRenderingTypes.MapRouteTag>();
	
	private Map<AmenityType, Map<String, String>> amenityTypeNameToTagVal = null;
	private Map<String, AmenityType> amenityNameToType = null;
	

	private MapRulType nameRuleType;
	private MapRulType coastlineRuleType;
	
	public MapRenderingTypes(String fileName){
		this.resourceName = fileName;
	}
	
	private static MapRenderingTypes DEFAULT_INSTANCE = null;
	
	public static MapRenderingTypes getDefault() {
		if(DEFAULT_INSTANCE == null){
			DEFAULT_INSTANCE = new MapRenderingTypes(null);
		}
		return DEFAULT_INSTANCE;
	}

	public Map<String, MapRulType> getEncodingRuleTypes(){
		checkIfInitNeeded();
		return types;
	}

	private void checkIfInitNeeded() {
		if (types == null) {
			types = new LinkedHashMap<String, MapRulType>();
			typeList.clear();
			nameRuleType = new MapRulType();
			nameRuleType.tag = "name";
			nameRuleType.onlyNameRef = true;
			nameRuleType.additional = false; 
			registerRuleType("name", null, nameRuleType);
			init();
		}
	}
	
	public MapRulType getTypeByInternalId(int id) {
		return typeList.get(id);
	}
	
	private MapRulType registerRuleType(String tag, String val, MapRulType rt){
		String keyVal = constructRuleKey(tag, val);
		if("natural".equals(tag) && "coastline".equals(val)) {
			coastlineRuleType = rt;
		}
		if(types.containsKey(keyVal)){
			if(types.get(keyVal).onlyNameRef ) {
				rt.id = types.get(keyVal).id;
				types.put(keyVal, rt);
				typeList.set(rt.id, rt);
				return rt;
			} else {
				throw new RuntimeException("Duplicate " + keyVal);
			}
		} else {
			rt.id = types.size();
			types.put(keyVal, rt);
			typeList.add(rt);
			return rt;
		}
	}
	
	public MapRulType getNameRuleType() {
		getEncodingRuleTypes();
		return nameRuleType;
	}
	
	public MapRulType getCoastlineRuleType() {
		getEncodingRuleTypes();
		return coastlineRuleType;
	}
	
	
	public Map<AmenityType, Map<String, String>> getAmenityTypeNameToTagVal() {
		if (amenityTypeNameToTagVal == null) {
			Map<String, MapRulType> types = getEncodingRuleTypes();
			amenityTypeNameToTagVal = new LinkedHashMap<AmenityType, Map<String, String>>();
			for(MapRulType type : types.values()){
				if(type.poiCategory != null && type.targetTagValue == null) {
					if(!amenityTypeNameToTagVal.containsKey(type.poiCategory)) {
						amenityTypeNameToTagVal.put(type.poiCategory, new TreeMap<String, String>());
					}
					String name = type.value;
					if (name != null) {
						if (type.poiPrefix != null) {
							name = type.poiPrefix + name;
							amenityTypeNameToTagVal.get(type.poiCategory).put(name, type.tag + " " + type.value);
						} else {
							amenityTypeNameToTagVal.get(type.poiCategory).put(name, type.tag);
						}
					}
				}
			}
		}
		return amenityTypeNameToTagVal;
	}
	
	public Map<String, AmenityType> getAmenityNameToType(){
		if(amenityNameToType == null){
			amenityNameToType = new LinkedHashMap<String, AmenityType>();
			Map<AmenityType, Map<String, String>> map = getAmenityTypeNameToTagVal();
			Iterator<Entry<AmenityType, Map<String, String>>> iter = map.entrySet().iterator();
			while(iter.hasNext()){
				Entry<AmenityType, Map<String, String>> e = iter.next();
				for(String t : e.getValue().keySet()){
					if (t != null) {
						if (amenityNameToType.containsKey(t)) {
							System.err.println("Conflict " + t + " " + amenityNameToType.get(t) + " <> " + e.getKey());
						}
						amenityNameToType.put(t, e.getKey());
					}
				}
			}
		}
		return amenityNameToType; 
	}
	
	private void init(){
		InputStream is;
		try {
			if(resourceName == null){
				is = MapRenderingTypes.class.getResourceAsStream("rendering_types.xml"); //$NON-NLS-1$
			} else {
				is = new FileInputStream(resourceName);
			}
			long time = System.currentTimeMillis();
			XmlPullParser parser = PlatformUtil.newXMLPullParser();
			int tok;
			parser.setInput(is, "UTF-8");
			String poiParentCategory = null;
			String poiParentPrefix  = null;
			while ((tok = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (tok == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if (name.equals("category")) { //$NON-NLS-1$
						poiParentCategory = parser.getAttributeValue("","poi_category");
						poiParentPrefix = parser.getAttributeValue("","poi_prefix");
						String tag = parser.getAttributeValue("","poi_tag");
						if (tag != null) {
							MapRulType rtype = new MapRulType();
							rtype.poiCategory = AmenityType.valueOf(poiParentCategory.toUpperCase());
							rtype.poiSpecified = true;
							rtype.poiPrefix = poiParentPrefix;
							rtype.tag = tag;
							registerRuleType(tag, null, rtype);
						}
					} else if (name.equals("type")) {
						parseTypeFromXML(parser, poiParentCategory, poiParentPrefix);
					} else if (name.equals("routing_type")) {
						parseRouteTagFromXML(parser);
					}
				}
			}
			
			log.info("Time to init " + (System.currentTimeMillis() - time)); //$NON-NLS-1$
			is.close();
		} catch (IOException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw e;
		} catch (XmlPullParserException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void parseRouteTagFromXML(XmlPullParser parser) {
		MapRouteTag rtype = new MapRouteTag();
		String mode = parser.getAttributeValue("", "mode"); //$NON-NLS-1$
		rtype.tag = parser.getAttributeValue("", "tag"); //$NON-NLS-1$
		rtype.value = parser.getAttributeValue("", "value"); //$NON-NLS-1$
		rtype.base = Boolean.parseBoolean(parser.getAttributeValue("", "base"));
		rtype.register = "register".equalsIgnoreCase(mode);
		rtype.amend = "amend".equalsIgnoreCase(mode);
		rtype.text = "text".equalsIgnoreCase(mode);
		rtype.relation = "relation".equalsIgnoreCase(parser.getAttributeValue("", "relation"));
		routeTags.add(rtype);
	}
	
	public List<MapRouteTag> getRouteTags() {
		checkIfInitNeeded();
		return routeTags;
	}

	private void parseTypeFromXML(XmlPullParser parser, String poiParentCategory, String poiParentPrefix) {
		MapRulType rtype = new MapRulType();
		String val = parser.getAttributeValue("", "minzoom"); //$NON-NLS-1$
		rtype.minzoom = 15;
		if (val != null) {
			rtype.minzoom = Integer.parseInt(val);
		}
		rtype.tag = parser.getAttributeValue("", "tag"); //$NON-NLS-1$
		rtype.value = parser.getAttributeValue("", "value"); //$NON-NLS-1$
		if (rtype.value != null && rtype.value.length() == 0) { //$NON-NLS-1$
			rtype.value = null;
		}
		registerRuleType(rtype.tag, rtype.value, rtype);
		rtype.additional = Boolean.parseBoolean(parser.getAttributeValue("", "additional")); //$NON-NLS-1$
		rtype.relation = Boolean.parseBoolean(parser.getAttributeValue("", "relation")); //$NON-NLS-1$
		rtype.namePrefix = parser.getAttributeValue("", "namePrefix"); //$NON-NLS-1$
		rtype.nameCombinator = parser.getAttributeValue("", "nameCombinator"); //$NON-NLS-1$
		if(rtype.namePrefix == null){
			rtype.namePrefix = "";
		}
		
		String v = parser.getAttributeValue("", "nameTags");
		if (v != null) {
			String[] names = v.split(",");
			if (names.length == 0) {
				names = new String[] { "name" };
			}
			rtype.names = new MapRulType[names.length];
			for (int i = 0; i < names.length; i++) {
				String tagName = names[i];
				if(rtype.namePrefix.length() > 0) {
					tagName = rtype.namePrefix + tagName;
				}
				MapRulType mt = types.get(constructRuleKey(tagName, null));
				if (mt == null) {
					mt = new MapRulType();
					mt.tag = tagName;
					mt.onlyNameRef = true;
					mt.additional = false;
					registerRuleType(tagName, null, mt);
				}
				rtype.names[i] = mt;
			}
		}
		String targetTag = parser.getAttributeValue("", "target_tag");
		String targetValue = parser.getAttributeValue("", "target_value");
		if (targetTag != null || targetValue != null) {
			if (targetTag == null) {
				targetTag = rtype.tag;
			}
			if (targetValue == null) {
				targetValue = rtype.value;
			}
			rtype.targetTagValue = types.get(constructRuleKey(targetTag, targetValue));
			if (rtype.targetTagValue == null) {
				throw new RuntimeException("Illegal target tag/value " + targetTag + " " + targetValue);
			}
		}
		if (poiParentCategory != null) {
			rtype.poiCategory = AmenityType.valueOf(poiParentCategory.toUpperCase());
			rtype.poiSpecified = true;
		}
		if (poiParentPrefix != null) {
			rtype.poiPrefix = poiParentPrefix;
		}

		String poiCategory = parser.getAttributeValue("", "poi_category");
		if (poiCategory != null) {
			rtype.poiSpecified = true;
			if (poiCategory.length() == 0) {
				rtype.poiCategory = null;
			} else {
				rtype.poiCategory = AmenityType.valueOf(poiCategory.toUpperCase());
			}
		}
		String poiPrefix = parser.getAttributeValue("", "poi_prefix");
		if (poiPrefix != null) {
			rtype.poiPrefix = poiPrefix;
		}
	}
	
	public static String constructRuleKey(String tag, String val) {
		if(val == null || val.length() == 0){
			return tag;
		}
		return tag + TAG_DELIMETER + val;
	}
	
	protected static String getTagKey(String tagValue) {
		int i = tagValue.indexOf(TAG_DELIMETER);
		if(i >= 0){
			return tagValue.substring(0, i);
		}
		return tagValue;
	}
	
	protected static String getValueKey(String tagValue) {
		int i = tagValue.indexOf(TAG_DELIMETER);
		if(i >= 0){
			return tagValue.substring(i + 1);
		}
		return null;
	}
	
	public String getAmenitySubtypePrefix(String tag, String val){
		Map<String, MapRulType> rules = getEncodingRuleTypes();
		MapRulType rt = rules.get(constructRuleKey(tag, val));
		if(rt != null && rt.poiPrefix != null) {
			return rt.poiPrefix;
		}
		rt = rules.get(constructRuleKey(tag, null));
		if(rt != null && rt.poiPrefix != null) {
			return rt.poiPrefix;
		}
		return null;
	}
	
	public String getAmenitySubtype(String tag, String val){
		String prefix = getAmenitySubtypePrefix(tag, val);
		if(prefix != null){
			return prefix + val;
		}
		return val;
	}
	
	public AmenityType getAmenityType(String tag, String val){
		// register amenity types
		Map<String, MapRulType> rules = getEncodingRuleTypes();
		MapRulType rt = rules.get(constructRuleKey(tag, val));
		if(rt != null && rt.poiSpecified) {
			return rt.poiCategory;
		}
		rt = rules.get(constructRuleKey(tag, null));
		if(rt != null && rt.poiSpecified) {
			return rt.poiCategory;
		}
		return null;
	}
	
	public static class MapRouteTag {
		boolean relation;
		String tag;
		String value;
		boolean register;
		boolean amend;
		boolean base; 
		boolean text;
		
	}
	
	public static class MapRulType {
		protected MapRulType[] names;
		protected String tag;
		protected String value;
		protected int minzoom;
		protected boolean additional;
		protected boolean relation;
		protected MapRulType targetTagValue;
		protected boolean onlyNameRef;
		
		// inner id
		protected int id;
		protected int freq;
		protected int targetId;
		
		protected String poiPrefix;
		protected String namePrefix ="";
		protected String nameCombinator = null;
		protected AmenityType poiCategory;
		protected boolean poiSpecified;
		
		
		public MapRulType(){
		}
		
		public String poiPrefix(){
			return poiPrefix;
		}
		
		public AmenityType getPoiCategory() {
			return poiCategory;
		}
		
		public String getTag() {
			return tag;
		}
		
		public int getTargetId() {
			return targetId;
		}
		
		public int getInternalId() {
			return id;
		}
		
		public void setTargetId(int targetId) {
			this.targetId = targetId;
		}
		
		public MapRulType getTargetTagValue() {
			return targetTagValue;
		}
		
		public String getValue() {
			return value;
		}
		
		public int getMinzoom() {
			return minzoom;
		}
		
		public boolean isAdditional() {
			return additional;
		}
		
		public boolean isOnlyNameRef() {
			return onlyNameRef;
		}
		
		public boolean isRelation() {
			return relation;
		}
		
		public int getFreq() {
			return freq;
		}
		
		public int updateFreq(){
			return ++freq;
		}
		
		@Override
		public String toString() {
			return tag + " " + value;
		}
	}
	
	public Collection<String> getAmenitySubCategories(AmenityType t){
		Map<AmenityType, Map<String, String>> amenityTypeNameToTagVal = getAmenityTypeNameToTagVal();
		if(!amenityTypeNameToTagVal.containsKey(t)){
			return Collections.emptyList(); 
		}
		return amenityTypeNameToTagVal.get(t).keySet();
	}
	
}

