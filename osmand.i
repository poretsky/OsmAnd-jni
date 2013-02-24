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
#include "Inspector.h"

void printUsage(std::string warning)
{
    if(!warning.empty())
        std::cout << warning << std::endl;
    std::cout << "Inspector is console utility for working with binary indexes of OsmAnd." << std::endl;
    std::cout << "It allows print info about file, extract parts and merge indexes." << std::endl;
    std::cout << "\nUsage for print info : inspector [-vaddress] [-vstreetgroups] [-vstreets] [-vbuildings] [-vintersections] [-vmap] [-vpoi] [-vtransport] [-zoom=Zoom] [-bbox=LeftLon,TopLat,RightLon,BottomLan] [file]" << std::endl;
    std::cout << "  Prints information about [file] binary index of OsmAnd." << std::endl;
    std::cout << "  -v.. more verbose output (like all cities and their streets or all map objects with tags/values and coordinates)" << std::endl;
    std::cout << "\nUsage for combining indexes : inspector -c file_to_create (file_from_extract ((+|-)parts_to_extract)? )*" << std::endl;
    std::cout << "\tCreate new file of extracted parts from input file. [parts_to_extract] could be parts to include or exclude." << std::endl;
    std::cout << "  Example : inspector -c output_file input_file +1,2,3\n\tExtracts 1, 2, 3 parts (could be find in print info)" << std::endl;
    std::cout << "  Example : inspector -c output_file input_file -2,3\n\tExtracts all parts excluding 2, 3" << std::endl;
    std::cout << "  Example : inspector -c output_file input_file1 input_file2 input_file3\n\tSimply combine 3 files" << std::endl;
    std::cout << "  Example : inspector -c output_file input_file1 input_file2 -4\n\tCombine all parts of 1st file and all parts excluding 4th part of 2nd file" << std::endl;
}

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
    	    printUsage(error.toStdString());
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

