package app.revanced.patches.reddit.misc.links

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.customReportsMethodMatch by composingFirstMethod {
    returnType("V")
    definingClass("Lcom/reddit/safety/report/dialogs/customreports/")
    instructions(
        "https://www.crisistextline.org/"(),
        method { toString() == "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;" },
        method { returnType == "V" },
        after(Opcode.RETURN_VOID()),
    )
}
