package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.BytecodePatchContextDeclarativePredicateCompositeBuilder
import app.revanced.patcher.accessFlags
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private val shouldShowBannerInstructions: BytecodePatchContextDeclarativePredicateCompositeBuilder =
    {
        var methodDefiningClass = ""
        custom {
            methodDefiningClass = definingClass
            true
        }

        instructions(
            allOf(
                Opcode.IGET_OBJECT(),
                field { definingClass == methodDefiningClass && name == $$"$shouldShowBanner$delegate" }
            ),
            afterAtMost(
                3,
                field { toString() == "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;" }
            ),
            afterAtMost(
                3,
                allOf(Opcode.INVOKE_INTERFACE(), method { name == "setValue" })
            ),
        )
    }

internal val BytecodePatchContext.redditScreenshotTriggerSharingListenerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    name("invokeSuspend")
    definingClass($$"Lcom/reddit/sharing/screenshot/RedditScreenshotTriggerSharingListener$ScreenshotBanner$")
    parameterTypes("Ljava/lang/Object;")
    shouldShowBannerInstructions()
}

internal val BytecodePatchContext.screenshotTakenBannerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    name("invokeSuspend")
    definingClass($$"Lcom/reddit/sharing/screenshot/composables/ScreenshotTakenBannerKt$ScreenshotTakenBanner$")
    parameterTypes("Ljava/lang/Object;")
    shouldShowBannerInstructions()
}
