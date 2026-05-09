package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.after
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.exploreResponseJsonParserMethod by gettingFirstMethodDeclaratively(
    "clusters",
    "next_max_id",
    "interests",
) {
    name("unsafeParseFromJson")
}

internal val BytecodePatchContext.exploreResponseJsonParserMethodMatch by composingFirstMethod("clusters") {
    name("unsafeParseFromJson")
    instructions(
        "clusters"(),
        allOf(Opcode.INVOKE_STATIC(), method { returnType == "Ljava/lang/String;" }),
        after(Opcode.MOVE_RESULT_OBJECT()),
        "next_max_id"(),
        "interests"(),
    )
}
