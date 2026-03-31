package app.revanced.patches.reddit.layout.dialogs

import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.firstImmutableMethodDeclaratively
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.frequentUpdatesHandlerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    name("invokeSuspend")
    definingClass($$"Lcom/reddit/screens/pager/FrequentUpdatesHandler$handleFrequentUpdates$")
    instructions(
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method { returnType == "Z" && parameterTypes.isEmpty() }
        ),
        after(Opcode.MOVE_RESULT()),
        afterAtMost(3, Opcode.IF_NEZ()),
        afterAtMost(
            3,
            method { toString() == "Lcom/reddit/domain/model/Subreddit;->getUserIsSubscriber()Ljava/lang/Boolean;" }
        ),
    )
}

internal val BytecodePatchContext.nsfwAlertEmitMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    name("emit")
    definingClass("Lcom/reddit/screens/pager/v2/")
    instructions(
        method { toString() == "Lcom/reddit/domain/model/Subreddit;->getOver18()Ljava/lang/Boolean;" },
        method { toString() == "Lcom/reddit/domain/model/Subreddit;->getHasBeenVisited()Z" },
        afterAtMost(3, Opcode.IF_NEZ()),
        "nsfwAlertDelegate"(),
        method { toString() == "Lcom/reddit/session/Session;->isIncognito()Z" },
    )
}

internal val BytecodePatchContext.buildNsfwAlertDialogMethodMatch by getting {
    firstMethodComposite {
        instructions(
            method("setPositiveButton"),
            after(method()),
            after(Opcode.MOVE_RESULT_OBJECT())
        )
    }
} using {
    firstImmutableMethodDeclaratively {
        returnType("V")
        parameterTypes()
        instructions("NsfwAlertDialogScreenDelegate"())
    }
}

internal val BytecodePatchContext.showNsfwAlertDialogMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("Lkotlin/jvm/functions/Function0;")
    definingClass("Lcom/reddit/screen/nsfw/")
    instructions("NSFW_POSITIVE_BUTTON_TEXT_ARG"())
}
