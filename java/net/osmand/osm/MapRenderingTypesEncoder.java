package net.osmand.osm;

import gnu.trove.list.array.TIntArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.osmand.osm.edit.Entity;
import net.osmand.osm.edit.Relation;

public class MapRenderingTypesEncoder extends MapRenderingTypes {
	
	public MapRenderingTypesEncoder(String fileName) {
		super(fileName);
	}
	
	private static MapRenderingTypesEncoder DEFAULT_INSTANCE = null;
	
	public static MapRenderingTypesEncoder getDefault() {
		if(DEFAULT_INSTANCE == null){
			DEFAULT_INSTANCE = new MapRenderingTypesEncoder(null);
		}
		return DEFAULT_INSTANCE;
	}

	private MapRulType getRelationalTagValue(String tag, String val) {
		MapRulType rType = getRuleType(tag, val);
		if(rType != null && rType.relation) {
			return rType;
		}
		return null;
	}
	
	public Map<MapRulType, String> getRelationPropogatedTags(Relation relation) {
		Map<MapRulType, String> propogated = new LinkedHashMap<MapRulType, String>();
		Map<String, String> ts = relation.getTags();
		Iterator<Entry<String, String>> its = ts.entrySet().iterator();
		while(its.hasNext()) {
			Entry<String, String> ev = its.next();
			MapRulType rule = getRelationalTagValue(ev.getKey(), ev.getValue());
			if(rule != null) {
				String value = ev.getValue();
				if(rule.targetTagValue != null) {
					rule = rule.targetTagValue;
					if(rule.getValue() != null) {
						value = rule.getValue();
					}
				}
				if (rule.names != null) {
					for (int i = 0; i < rule.names.length; i++) {
						String tag = rule.names[i].tag.substring(rule.namePrefix.length());
						if(ts.containsKey(tag)) {
							propogated.put(rule.names[i], ts.get(tag));
						}
					}
				}
				propogated.put(rule, value);
			}
			addParsedSpecialTags(propogated, ev);
		}
		return propogated;
	}
	
	private MapRulType getRuleType(String tag, String val) {
		Map<String, MapRulType> types = getEncodingRuleTypes();
		MapRulType rType = types.get(constructRuleKey(tag, val));
		if (rType == null) {
			rType = types.get(constructRuleKey(tag, null));
		}
		return rType;
	}
	
	

	public boolean encodeEntityWithType(Entity e, int zoom, TIntArrayList outTypes, 
			TIntArrayList outaddTypes, Map<MapRulType, String> namesToEncode, List<MapRulType> tempList) {
		outTypes.clear();
		outaddTypes.clear();
		namesToEncode.clear();
		tempList.clear();
		tempList.add(getNameRuleType());

		boolean area = "yes".equals(e.getTag("area")) || "true".equals(e.getTag("area"));

		Collection<String> tagKeySet = e.getTagKeySet();
		for (String tag : tagKeySet) {
			String val = e.getTag(tag);
			MapRulType rType = getRuleType(tag, val);
			if (rType != null) {
				if (rType.minzoom > zoom) {
					continue;
				}
				if(rType.targetTagValue != null) {
					rType = rType.targetTagValue;
				}
				rType.updateFreq();
				if (rType.names != null) {
					for (int i = 0; i < rType.names.length; i++) {
						tempList.add(rType.names[i]);
					}
				}

				if (!rType.onlyNameRef) {
					if (rType.additional) {
						outaddTypes.add(rType.id);
					} else {
						outTypes.add(rType.id);
					}
				}
			}
		}
		for(MapRulType mt : tempList){
			String val = e.getTag(mt.tag);
			if(val != null && val.length() > 0){
				namesToEncode.put(mt, val);
			}
		}
		return area;
	}
	
	public void addParsedSpecialTags(Map<MapRulType,String> propogated, Entry<String,String> ev) {
		if ("osmc:symbol".equals(ev.getKey())) {
			String[] tokens = ev.getValue().split(":", 6);
			if (tokens.length > 0) {
				String symbol_name = "osmc_symbol_" + tokens[0];
				MapRulType rt = getRuleType(symbol_name, "");
				if(rt != null) {
					propogated.put(rt, "");
					if (tokens.length > 2 && rt.names != null) {
						String symbol = "osmc_symbol_" + tokens[1] + "_" + tokens[2];
						String name = "\u00A0";
						if (tokens.length > 3 && tokens[3].trim().length() > 0) {
							name = tokens[3];
						}
						for(int k = 0; k < rt.names.length; k++) {
							if(rt.names[k].tag.equals(symbol)) {
								propogated.put(rt.names[k], name);
							}
						}
					}
				}
			}
		}
	}
}
