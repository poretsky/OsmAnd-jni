LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OSMAND_PROTOBUF := $(LOCAL_PATH)/../core/externals/protobuf/upstream.patched
ifeq ($(OSMAND_BUILDING_NEON_LIBRARY),true)
	QT := $(LOCAL_PATH)/../core/externals/qtbase-android/upstream.patched.$(TARGET_ARCH_ABI)-neon
else
	QT := $(LOCAL_PATH)/../core/externals/qtbase-android/upstream.patched.$(TARGET_ARCH_ABI)
endif
OSMAND_CORE := $(LOCAL_PATH)/../core
OSMAND_JNI_RELATIVE := .
OSMAND_JNI := $(LOCAL_PATH)/$(OSMAND_JNI_RELATIVE)

LOCAL_C_INCLUDES := \
	$(QT)/include \
	$(QT)/include/QtCore \
	$(LOCAL_PATH)/src \
    $(OSMAND_PROTOBUF)/src \
	$(OSMAND_CORE)/include \
	$(OSMAND_CORE)/protos \
	$(OSMAND_CORE)/utils

LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES := \
	$(OSMAND_JNI_RELATIVE)/native/java_core_wrap.cpp
	
# Name of the local module
ifneq ($(OSMAND_BUILDING_NEON_LIBRARY),true)
	LOCAL_MODULE := OsmAndJNI
else
	LOCAL_MODULE := OsmAndJNI_neon
	LOCAL_ARM_NEON := true
endif

LOCAL_CFLAGS := \
	-DGOOGLE_PROTOBUF_NO_RTTI \
	-DSK_BUILD_FOR_ANDROID \
	-DSK_BUILD_FOR_ANDROID_NDK \
	-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=0 \
	-DSK_RELEASE \
	-DSK_CPU_LENDIAN \
	-DGR_RELEASE=1 \
	-DANDROID_BUILD \
	-fPIC
	
ifeq ($(LOCAL_ARM_NEON),true)
	OSMAND_BINARY_SUFFIX := _neon
else
	OSMAND_BINARY_SUFFIX :=
endif

LOCAL_SHARED_LIBRARIES := \
	OsmAndCore$(OSMAND_BINARY_SUFFIX) \
	Qt5Core$(OSMAND_BINARY_SUFFIX) \
	Qt5Network$(OSMAND_BINARY_SUFFIX) \
	Qt5Xml$(OSMAND_BINARY_SUFFIX) \
	Qt5Sql$(OSMAND_BINARY_SUFFIX) \
	Qt5Concurrent$(OSMAND_BINARY_SUFFIX)

include $(BUILD_SHARED_LIBRARY)
