# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes Signature
#add to adapt getClass().getName().equals(clsname) logic
-dontwarn com.silionmodule.**
-keep class com.silionmodule.**{*;}

-dontwarn com.thingmagic.**
-keep class com.thingmagic.**{*;}

-dontwarn com.bth.api.cls.**
-keep class com.bth.api.cls.**{*;}

-dontwarn com.communication.**
-keep class com.communication.**{*;}

-dontwarn com.thingmagic.**
-keep class com.thingmagic.**{*;}

-dontwarn com.tool.**
-keep class com.tool.**{*;}

-keepclassmembers class com.honeywell.rfidservice.rfid.RfidReader {
    private void doSetFreqHopTable(java.util.List);
    private void doSetRegion(com.honeywell.rfidservice.rfid.Region);
    private void setSilionReader(com.silionmodule.Reader);
    private void startCarrierTest(int, int);
    private void stopCarrierTest();
}

-keepclassmembers class com.honeywell.rfidservice.RfidManager {
    private void setDebugMode(boolean);
}
