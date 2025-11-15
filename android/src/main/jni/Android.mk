LOCAL_PATH:= $(call my-dir)

PREFIX := $(LOCAL_PATH)/../../../../buildscripts/prefix/$(TARGET_ARCH_ABI)

include $(CLEAR_VARS)
LOCAL_MODULE := libswresample
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libpostproc
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
# only include if library file exists
ifneq (,$(wildcard $(LOCAL_SRC_FILES)))
include $(PREBUILT_SHARED_LIBRARY)
endif

include $(CLEAR_VARS)
LOCAL_MODULE := libavutil
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavcodec
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavformat
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libswscale
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavfilter
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavdevice
LOCAL_SRC_FILES := $(PREFIX)/lib/$(LOCAL_MODULE).so
LOCAL_EXPORT_C_INCLUDES := $(PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libmpv
LOCAL_SRC_FILES := $(PREFIX)/lib/libmpv.so
LOCAL_EXPORT_C_INCLUDES := $(PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := libplayer
LOCAL_CFLAGS    := -Werror
LOCAL_CPPFLAGS  += -std=c++11
LOCAL_SRC_FILES := \
	main.cpp \
	render.cpp \
	log.cpp \
	jni_utils.cpp \
	property.cpp \
	event.cpp \
	thumbnail.cpp
LOCAL_LDLIBS    := -llog -lGLESv3 -lEGL -latomic
LOCAL_SHARED_LIBRARIES := swscale avcodec mpv avfilter avformat avutil swresample avdevice

include $(BUILD_SHARED_LIBRARY)
