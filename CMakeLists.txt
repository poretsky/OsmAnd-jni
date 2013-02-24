project(OsmAndJNI)

include_directories(AFTER SYSTEM
	"${OSMAND_ROOT}/core/include"
	"${OSMAND_ROOT}/core/protos"
	"${OSMAND_ROOT}/core/utils"
	"$ENV{JAVA_HOME}/include"
)

if(CMAKE_TARGET_OS STREQUAL "windows")
	include_directories(AFTER SYSTEM
		"$ENV{JAVA_HOME}/include/win32"
	)
	add_definitions(-DSK_BUILD_FOR_WIN32)
elseif(CMAKE_TARGET_OS STREQUAL "darwin")
	include_directories(AFTER SYSTEM
		"$ENV{JAVA_HOME}/include/darwin"
	)
	add_definitions(-DSK_BUILD_FOR_MAC)
elseif(CMAKE_TARGET_OS STREQUAL "linux")
	include_directories(AFTER SYSTEM
		"$ENV{JAVA_HOME}/include/linux"
	)
	add_definitions(-DSK_BUILD_FOR_UNIX)
endif()
add_definitions(
	-DGOOGLE_PROTOBUF_NO_RTTI
	-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=0
	-DSK_RELEASE
	-DSK_CPU_LENDIAN
	-DGR_RELEASE=1
	-DXML_STATIC
	-DOSMAND_CORE_EXPORTS
)
if(CMAKE_COMPILER_FAMILY STREQUAL "gcc" OR CMAKE_COMPILER_FAMILY STREQUAL "clang")
	set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif()

set(pd_sources "")
if(CMAKE_TARGET_OS STREQUAL "linux")
elseif(CMAKE_TARGET_OS STREQUAL "darwin")
elseif(CMAKE_TARGET_OS STREQUAL "windows")
endif()

file(GLOB_RECURSE sources "native/*")
add_library(OsmAndJNI SHARED
	${sources}
	${pd_sources}
)

target_link_libraries(OsmAndJNI LINK_PUBLIC
	OsmAndCore
	OsmAndCoreUtils
)

find_package(Qt5Core REQUIRED)
qt5_use_modules(OsmAndJNI Core)
