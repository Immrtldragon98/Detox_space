# Retrofit reads these annotations and generic signatures at runtime.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# API payloads are deserialized by Gson using field names.
-keep class com.immrtldragon.detoxspace.data.remote.** { <fields>; }
