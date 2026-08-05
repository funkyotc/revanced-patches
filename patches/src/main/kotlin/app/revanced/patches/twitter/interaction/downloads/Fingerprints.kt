package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.subscriptionsFeaturesMethodMatch by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes()
    returnType("Ljava/lang/String;")
    strings(
        "NotePostFeatures(maxWeightedCharacterLength=",
        ", isRichCompositionEnabled=",
        ", isPostStormEnabled=",
        ")",
    )
}

internal fun BytecodePatchContext.getCanDownloadVideoMethodMatch(subscriptionsFeaturesDefiningClass: String, legacy: Boolean) = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("Z")
    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { definingClass.contains("legacy") == legacy && type == subscriptionsFeaturesDefiningClass },
        ),
        after(
            allOf(
                Opcode.INVOKE_INTERFACE(),
                method { definingClass == subscriptionsFeaturesDefiningClass }
            )
        ),
        after(
            Opcode.MOVE_RESULT(),
        ),
        after(
            Opcode.RETURN(),
        ),
    )
}

internal fun BytecodePatchContext.getMediaGalleryDownloadMethodMatch(subscriptionsFeaturesDefiningClass: String) = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        allOf(
            Opcode.INVOKE_INTERFACE(),
            method { definingClass == subscriptionsFeaturesDefiningClass && returnType == "Z" },
        ),
        "offline_videos_download_fallback"(),
        allOf(
            Opcode.INVOKE_INTERFACE(),
            method { definingClass == subscriptionsFeaturesDefiningClass && returnType == "Z" },
        ),
        "offline_videos_download_fallback"(),
        "video_download"(),
    )
}

internal fun BytecodePatchContext.getPostMediaActionMethodMatch(subscriptionsFeaturesDefiningClass: String) = firstMethodComposite {
    var mediaConfigGifDefiningClass = ""
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    name("invoke")
    instructions(
        "postEvent"(),
        "click"(),
        "video"(),
        "gif"(),
        "photo"(),
        Opcode.INSTANCE_OF(), // MediaContentImage
        Opcode.INSTANCE_OF(), // MediaContentVideo
        // MediaContentGif
        allOf(
            Opcode.INSTANCE_OF(),
            type {
                mediaConfigGifDefiningClass = this
                true
            }
        ),
        // Boolean for creating download button for GIF.
        allOf(
          Opcode.INVOKE_VIRTUAL(),
            method { definingClass == mediaConfigGifDefiningClass }
        ),
        "save"(),
        // Boolean for checking if user can download video
        allOf(
            Opcode.INVOKE_INTERFACE_RANGE(),
            method { definingClass == subscriptionsFeaturesDefiningClass && returnType == "Z" },
        )
    )
}
