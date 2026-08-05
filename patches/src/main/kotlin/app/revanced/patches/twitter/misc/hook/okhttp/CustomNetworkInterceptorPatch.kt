package app.revanced.patches.twitter.misc.hook.okhttp

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch

private const val CUSTOM_NETWORK_INTERCEPTOR_EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/twitter/patches/hook/okhttp/CustomNetworkInterceptorPatch;"

@Suppress("unused")
val customNetworkInterceptor = bytecodePatch {
    compatibleWith(
        "com.twitter.android"(
            "12.8.0-release.0",
            "12.10.0-release.0",
        )
    )

    dependsOn(sharedExtensionPatch)

    apply {
        okhttpBuildMethod.addInstructions(
            0,
            $$"""
                new-instance v0, $$CUSTOM_NETWORK_INTERCEPTOR_EXTENSION_CLASS_DESCRIPTOR
                invoke-direct { v0 }, $$CUSTOM_NETWORK_INTERCEPTOR_EXTENSION_CLASS_DESCRIPTOR-><init>()V
                invoke-virtual { p0, v0 }, Lokhttp3/OkHttpClient$Builder;->addNetworkInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient$Builder;
            """
        )
    }
}