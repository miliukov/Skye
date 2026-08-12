# --- Gson: DTO и domain-модели, которые сериализуются/десериализуются по рефлексии ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class dev.dmil.skye.data.dto.** { <fields>; }
-keep class dev.dmil.skye.domain.model.** { <fields>; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# --- WorkManager: класс воркера ищется по имени через Class.forName в рантайме ---
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Enum-настройки, сохраняемые в DataStore через .name / .valueOf(...) ---
-keepclassmembers enum dev.dmil.skye.domain.model.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}