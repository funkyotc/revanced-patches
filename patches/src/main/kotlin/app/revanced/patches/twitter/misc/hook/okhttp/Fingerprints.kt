package app.revanced.patches.twitter.misc.hook.okhttp

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.okhttpBuildMethod by gettingFirstMethodDeclaratively {
    definingClass($$"Lokhttp3/OkHttpClient$Builder;")
    name("build")
}