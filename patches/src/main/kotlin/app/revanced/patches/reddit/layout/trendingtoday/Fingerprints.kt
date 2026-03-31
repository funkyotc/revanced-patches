package app.revanced.patches.reddit.layout.trendingtoday

import app.revanced.patcher.definingClass
import app.revanced.patcher.firstImmutableMethod
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.searchTypeaheadListDefaultPresentationConstructorMethod by getting {
    firstMethodDeclaratively {
        name("<init>")
        returnType("V")
        parameterTypes("Ljava/lang/String;")
    }
} using {
    firstImmutableMethod("OnSearchTypeaheadListDefaultPresentation(title=")
}

internal val BytecodePatchContext.trendingTodayItemMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/reddit/search/combined/ui/composables")
    returnType("V")
    instructions("search_trending_item"())
}
