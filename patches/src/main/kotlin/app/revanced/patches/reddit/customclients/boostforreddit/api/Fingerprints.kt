package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMethodDeclaratively {
    name("getClientId")
    definingClass("Credentials;")
}

internal val BytecodePatchContext.loginActivityOnCreateMethodMatch by composingFirstMethod {
    name("onCreate")
    definingClass("LoginActivity;")
    instructions("http://rubenmayayo.com"())
}

internal val BytecodePatchContext.loginActivityAShouldOverrideUrlLoadingMethodMatch by composingFirstMethod {
    name("shouldOverrideUrlLoading")
    definingClass($$"LoginActivity$a;")
    instructions("http://rubenmayayo.com"())
}

internal val BytecodePatchContext.buildUserAgentMethodMatch by composingFirstMethod {
    instructions("%s:%s:%s (by /u/%s)"())
}
