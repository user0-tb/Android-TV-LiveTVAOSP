LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Include all test java files.
LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-Iaidl-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-annotations \
    androidx.test.runner \
    tv-guava-android-jar \
    mockito-robolectric-prebuilt \
    tv-lib-truth \
    androidx.test.uiautomator_uiautomator \

# Link tv-common as shared library to avoid the problem of initialization of the constants
LOCAL_JAVA_LIBRARIES := tv-common

LOCAL_INSTRUMENTATION_FOR := LiveTv
LOCAL_MODULE := tv-tuner-testing
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_MODULE_TAGS := optional
LOCAL_SDK_VERSION := system_current

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_AIDL_INCLUDES += $(LOCAL_PATH)/src

include $(BUILD_STATIC_JAVA_LIBRARY)
