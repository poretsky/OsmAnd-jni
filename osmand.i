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

/*
namespace OsmAnd {

class ObfSection {
};

class ObfReader {
public :
	int getVersion();
    std::list< OsmAnd::ObfSection* > getSections();
};

}


int OsmAnd::Utilities::get31TileNumberX(double longitude);
int OsmAnd::Utilities::get31TileNumberY( double latitude);
double OsmAnd::Utilities::get31LongitudeX(int x);
double OsmAnd::Utilities::get31LatitudeY(int y);
double OsmAnd::Utilities::getTileNumberX(float zoom, double longitude);
double OsmAnd::Utilities::getTileNumberY(float zoom,  double latitude);
double OsmAnd::Utilities::checkLatitude(double latitude);
double OsmAnd::Utilities::checkLongitude(double longitude);
double OsmAnd::Utilities::getPowZoom(float zoom);
double OsmAnd::Utilities::getLongitudeFromTile(float zoom, double x);
double OsmAnd::Utilities::getLatitudeFromTile(float zoom, double y);
*/