project(OsmAndJNI)

set(target_specific_private_includes "")
if(CMAKE_TARGET_OS STREQUAL "linux" OR CMAKE_TARGET_OS STREQUAL "darwin" OR CMAKE_TARGET_OS STREQUAL "windows")
	set(target_specific_private_includes ${target_specific_private_includes}
		"$ENV{JAVA_HOME}/include"
	)
endif()
if(CMAKE_TARGET_OS STREQUAL "windows")
	set(target_specific_private_includes ${target_specific_private_includes}
		"$ENV{JAVA_HOME}/include/win32"
	)
elseif(CMAKE_TARGET_OS STREQUAL "darwin")
	set(target_specific_private_includes ${target_specific_private_includes}
		"$ENV{JAVA_HOME}/include/darwin"
	)
elseif(CMAKE_TARGET_OS STREQUAL "linux")
	set(target_specific_private_includes ${target_specific_private_includes}
		"$ENV{JAVA_HOME}/include/linux"
	)
endif()

include_directories(AFTER SYSTEM
	"${OSMAND_ROOT}/core/externals/protobuf/upstream.patched/src"
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

if(CMAKE_SHARED_LIBS_ALLOWED_ON_TARGET)
	add_library(OsmAndJNI SHARED
		${sources}
		${pd_sources}
	)

	if(CMAKE_STATIC_LIBS_ALLOWED_ON_TARGET)
		add_dependencies(OsmAndJNI
			OsmAndCore_static
			OsmAndCoreUtils_static
		)
		target_link_libraries(OsmAndJNI
			LINK_PUBLIC
				OsmAndCore_static
				OsmAndCoreUtils_static
		)
	else()
		add_dependencies(OsmAndJNI
			OsmAndCore_shared
			OsmAndCoreUtils_shared
		)
		target_link_libraries(OsmAndJNI
			LINK_PUBLIC
				OsmAndCore_shared
				OsmAndCoreUtils_shared
		)
	endif()
endif()