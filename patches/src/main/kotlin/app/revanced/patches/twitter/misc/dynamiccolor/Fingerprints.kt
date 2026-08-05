package app.revanced.patches.twitter.misc.dynamiccolor

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.getting
import app.revanced.util.using

internal val BytecodePatchContext.designTokenMethodMatch by getting {
    firstMethodComposite {
        instructions(literal(4280130544L))
    }
} using { firstImmutableMethod("StaticColor") }