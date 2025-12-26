package android.text;

/**
 * 這是一個用來解決 "Method ... not mocked" 錯誤的 Stub 類別。
 * 它模擬了 Android TextUtils 的行為，讓單元測試可以在 JVM 上執行。
 * 請確保此檔案位於 src/test/java/android/text/TextUtils.java
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
