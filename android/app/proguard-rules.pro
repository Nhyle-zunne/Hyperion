# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/proguard-android-optimize.txt

# Preserve WebKit interfaces and JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepclassmembers class * extends android.webkit.WebViewClient {
   public void *(android.webkit.WebView, java.lang.String);
   public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
   public boolean *(android.webkit.WebView, java.lang.String);
}

-keepclassmembers class * extends android.webkit.WebChromeClient {
   public void *(android.webkit.WebView, java.lang.String);
}
