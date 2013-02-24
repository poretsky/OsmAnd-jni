%module CoreOsmAnd
// # swig -c++ -java -package net.osmand.bridge -outdir java/net/osmand/bridge -o native/java_core_wrap.cpp osmand.i; 
%include "typemaps.i"
%include "std_string.i"
%include "std_vector.i"
namespace std {
   %template(IntVector) vector<int>;
   %template(DoubleVector) vector<double>;
   %template(StringVector) vector<string>;
   %template(ConstCharVector) vector<const char*>;
}

%{
#include <string>
#include <iostream>
#include <sstream>
#include <map>
#include <memory>

#include <QFile>
#include <QStringList>
#include <Inspector.h>
#include <ObfReader.h>
#include <ObfSection.h>
#include <Utilities.h>

class ObfInspector {
public:
	static int inspector(std::vector<std::string> argv) 
	{
	    OsmAnd::Inspector::Configuration cfg;
    	QString error;
    	QStringList args;
    	for (int idx = 0; idx < argv.size(); idx++)
        	args.push_back(argv[idx].c_str());
	
    	if(!OsmAnd::Inspector::parseCommandLineArguments(args, cfg, error))
    	{
	        return -1;
    	}
    	OsmAnd::Inspector::dumpToStdOut(cfg);
    	return 0;
	}
};
%}

class ObfInspector {
public:
	static int ObfInspector::inspector(std::vector<std::string> argv) ;
};

namespace OsmAnd {
%nodefaultctor ObfSection;
struct ObfSection {
	QString _name;
    uint32_t _length;
    uint32_t _offset;
};
%nodefaultctor ObfReader;
class ObfReader {
public :
	int getVersion();
    std::list< OsmAnd::ObfSection* > getSections();
};

}

namespace OsmAnd::Utilities {
	int get31TileNumberX(double longitude);
	int get31TileNumberY( double latitude);
	double get31LongitudeX(int x);
	double get31LatitudeY(int y);
	double getTileNumberX(float zoom, double longitude);
	double getTileNumberY(float zoom,  double latitude);
	double checkLatitude(double latitude);
	double checkLongitude(double longitude);
	double getPowZoom(float zoom);
	double getLongitudeFromTile(float zoom, double x);
	double getLatitudeFromTile(float zoom, double y);
}
