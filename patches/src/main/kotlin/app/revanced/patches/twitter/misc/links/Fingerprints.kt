package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.sanitizeSharingLinksMethod by gettingFirstMethod(
    "<this>",
    "shareParam",
    "sessionToken",
) { returnType == "Ljava/lang/String;" }

internal val BytecodePatchContext.linkSharingDomainHelperMethod by gettingFirstMethodDeclaratively {
    name("getShareDomain")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}

internal val BytecodePatchContext.returnUsernameHelperMethod by gettingFirstMethodDeclaratively {
    name("isReturnUsernameEnabled")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}

internal val BytecodePatchContext.linkInternalShareSheetMethodMatch by composingFirstMethod {
    instructions(
        "tweet-"(),
        "https://x.com/i/status/"(),
        field { type == "Lcom/x/models/ContextualPost;" }
    )
}

internal val BytecodePatchContext.linkExternalShareSheetMethodMatch by composingFirstMethod {
    instructions(
        Opcode.IF_EQZ(),
        "https://x.com/i/lists/"(),
        "https://x.com/i/trending/"(),
        "https://x.com/i/status/"(),
    )
}