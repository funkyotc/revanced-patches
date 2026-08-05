package app.revanced.patches.twitter.misc.hook

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.hook.json.addJsonHook
import app.revanced.patches.twitter.misc.hook.json.jsonHook
import app.revanced.patches.twitter.misc.hook.json.jsonHookPatch

fun hookPatch(
    name: String,
    hookClassDescriptor: String,
) = bytecodePatch(name) {
    dependsOn(jsonHookPatch)

    compatibleWith(
        "com.twitter.android"(
            "11.80.0-release.0",
            "12.8.0-release.0",
            "12.10.0-release.0",
        ),
    )

    apply {
        addJsonHook(jsonHook(hookClassDescriptor))
    }
}
