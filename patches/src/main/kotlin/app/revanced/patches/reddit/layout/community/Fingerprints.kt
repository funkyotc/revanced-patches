package app.revanced.patches.reddit.layout.community

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstImmutableMethod
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.communityRecommendationSectionMethod by getting {
    firstMethodDeclaratively {
        accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
        returnType("V")
        instructions("feedContext"())
    }
} using { firstImmutableMethod("community_recomendation_section_") }
