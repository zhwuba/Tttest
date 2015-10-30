LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(TYD_THEME_CLUB_SUPPORT), yes)
AUTO_GEN_CLS := src/com/freeme/themeclub/wallpaper/Thumbs.java
$(warning $(LOCAL_PATH)/wallpapers/build/cp_res)
SH_RES := $(shell bash -f $(LOCAL_PATH)/wallpapers/build/cp_res $(LOCAL_PATH) $(LOCAL_PATH)/wallpapers/$(TARGET_PRODUCT) $(AUTO_GEN_CLS))
endif

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 
LOCAL_STATIC_JAVA_LIBRARIES += libadroisdkd
LOCAL_STATIC_JAVA_LIBRARIES += libdroiaccountsdkd

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := FreemeThemeClub

LOCAL_CERTIFICATE := platform
#LOCAL_DEX_PREOPT := false

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

##############################################
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libadroisdkd:libs/adroi.sdk.jar \
            libdroiaccountsdkd:libs/droiaccountsdk.jar
include $(BUILD_MULTI_PREBUILT)

ifeq ($(TYD_THEME_CLUB_SUPPORT), yes)
include $(LOCAL_PATH)/wallpapers/$(TARGET_PRODUCT)/AllWallpapers.mk
endif
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
