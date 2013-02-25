package net.osmand.osm;

import java.util.Collection;
import java.util.List;

import net.osmand.data.Amenity;
import net.osmand.data.AmenityType;
import net.osmand.data.Building;
import net.osmand.data.City;
import net.osmand.data.City.CityType;
import net.osmand.data.LatLon;
import net.osmand.data.MapObject;
import net.osmand.data.Street;
import net.osmand.data.TransportStop;
import net.osmand.osm.OSMSettings.OSMTagKey;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

public class EntityParser {
	
	public static void parseMapObject(MapObject mo, Entity e) {
		mo.setId(e.getId());
		if (mo.getName().length() == 0) {
			mo.setName(e.getTag(OSMTagKey.NAME));
		}
		if (mo.getEnName().length() == 0) {
			mo.setEnName(e.getTag(OSMTagKey.NAME_EN));
			if (mo.getName().length() == 0) {
				mo.setName(mo.getEnName());
			}
		}
		if (mo.getLocation() == null) {
			LatLon l = MapUtils.getCenter(e);
			if (l != null) {
				mo.setLocation(l.getLatitude(), l.getLongitude());
			}
		}
		if (mo.getName().length() == 0) {
			setNameFromOperator(mo, e);
		}
		if (mo.getName().length() == 0) {
			setNameFromRef(mo, e);
		}
	}

	private static void setNameFromRef(MapObject mo, Entity e) {
		String ref = e.getTag(OSMTagKey.REF);
		if(ref != null){
			mo.setName(ref);
		}
	}

	private static void setNameFromOperator(MapObject mo,Entity e) {
		String op = e.getTag(OSMTagKey.OPERATOR);
		if (op == null)
			return;
		String ref = e.getTag(OSMTagKey.REF);
		if (ref != null)
			op += " [" + ref + "]";
		mo.setName(op);
	}
	
	public static void parseAmenity(Amenity am, Entity entity) {
		parseMapObject(am, entity);
		am.setId((entity.getId() << 1) + ((entity instanceof Node) ? 0 : 1));
	}

	public static Amenity parseAmenity(Entity entity, AmenityType type, String subtype) {
		Amenity am = new Amenity();
		parseAmenity(am, entity);
		am.setType(type);
		am.setSubType(subtype);
		am.setOpeningHours(entity.getTag(OSMTagKey.OPENING_HOURS));
		am.setPhone(entity.getTag(OSMTagKey.PHONE));
		if (am.getPhone() == null) {
			am.setPhone(entity.getTag(OSMTagKey.CONTACT_PHONE));
		}
		String siteUrl = entity.getTag(OSMTagKey.WIKIPEDIA);
		if (siteUrl != null) {
			if (!siteUrl.startsWith("http://")) { //$NON-NLS-1$
				int i = siteUrl.indexOf(':');
				if (i == -1) {
					siteUrl = "http://en.wikipedia.org/wiki/" + siteUrl; //$NON-NLS-1$
				} else {
					siteUrl = "http://" + siteUrl.substring(0, i) + ".wikipedia.org/wiki/" + siteUrl.substring(i + 1); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} else {
			siteUrl = entity.getTag(OSMTagKey.WEBSITE);
			if (siteUrl == null) {
				siteUrl = entity.getTag(OSMTagKey.URL);
				if (siteUrl == null) {
					siteUrl = entity.getTag(OSMTagKey.CONTACT_WEBSITE);
				}
			}
			if (siteUrl != null && !siteUrl.startsWith("http://")) {
				siteUrl = "http://" + siteUrl;
			}
		}
		am.setSite(siteUrl);
		am.setDescription(entity.getTag(OSMTagKey.DESCRIPTION));
		return am;
	}
	
	public static List<Amenity> parseAmenities(MapRenderingTypes renderingTypes,
			Entity entity, List<Amenity> amenitiesList){
		if(entity instanceof Relation){
			// it could be collection of amenities
			return amenitiesList;
		}
				
		Collection<String> keySet = entity.getTagKeySet();
		if (!keySet.isEmpty()) {
			int shift = 0;
			for (String t : keySet) {
				AmenityType type = renderingTypes.getAmenityType(t, entity.getTag(t));
				if (type != null) {
					String subtype = renderingTypes.getAmenitySubtype(t, entity.getTag(t));
					Amenity a = parseAmenity(entity, type, subtype);
					if(checkAmenitiesToAdd(a, amenitiesList) && !"no".equals(subtype)){
						amenitiesList.add(shift, a);
						shift++;
					}
				} else {
					type = renderingTypes.getAmenityType(t, null);
					if (type != null) {
						String subtype = renderingTypes.getAmenitySubtype(t, entity.getTag(t));
						Amenity a = parseAmenity(entity, type, subtype);
						if(checkAmenitiesToAdd(a, amenitiesList) && !"no".equals(subtype)){
							// 	add amenity to the end
							amenitiesList.add(a);
						}
					}
					
				}
			}
		}
		return amenitiesList;
	}
	
	private static boolean checkAmenitiesToAdd(Amenity a, List<Amenity> amenitiesList){
		// check amenity for duplication
		for(Amenity b : amenitiesList){
			if(b.getType() == a.getType() && Algorithms.objectEquals(a.getSubType(), b.getSubType())){
				return false;
			}
		}
		return true;
		
	}
	
	public static Building parseBuilding(Entity e){
		Building b = new Building();
		parseMapObject(b, e);
		// try to extract postcode
		String p = e.getTag(OSMTagKey.ADDR_POSTCODE);
		if(p == null) {
			p = e.getTag(OSMTagKey.POSTAL_CODE);
		}
		b.setPostcode(p);
		return b;
	}
	
	public static City parseCity(Node el) {
		return parseCity(el, CityType.valueFromString(el.getTag(OSMTagKey.PLACE)));
	}
	
	public static City parseCity(Entity el, CityType t) {
		City c = new City(t);
		parseMapObject(c, el);
		String isin = el.getTag(OSMTagKey.IS_IN);
		isin = isin != null ? isin.toLowerCase() : null;
		c.setIsin(isin);
		return c;
	}
	
	
	public static Building registerBuilding(Street s, Entity e){
		return registerBuilding(s, e, e.getTag(OSMTagKey.ADDR_HOUSE_NUMBER));
	}
	
	public static  Building registerBuilding(Street s, Entity e, String ref){
		if(ref == null){
			return null;
		}
		Building building = parseBuilding(e);
		building.setName(ref);
		s.registerBuilding(building);
		return building;
	}
	
	public static Building registerBuilding(City c, Entity e) {
		String number = e.getTag(OSMTagKey.ADDR_HOUSE_NUMBER);
		String street = e.getTag(OSMTagKey.ADDR_STREET);
		if (street != null && number != null) {
			Street s = c.registerStreet(street);
			registerBuilding(s, e);
		}
		return null;
	}
	
	public static OsmTransportRoute parserRoute(Relation r, String ref){
		OsmTransportRoute rt = new OsmTransportRoute();
		parseMapObject(rt, r);
		rt.setRef(ref);
		return rt;
	}
	
	public static TransportStop parseTransportStop(Entity e){
		TransportStop st = new TransportStop();
		parseMapObject(st, e);
		return st;
	}

}