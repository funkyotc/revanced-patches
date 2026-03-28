package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal fun baseClientIdMethod(string: String) = composingFirstMethod {
    instructions(
        "dj-xCIZQYiLbEg"(),
        string(),
    )
}

internal val BytecodePatchContext.getLoggedInBearerTokenMethodMatch by baseClientIdMethod("authorization_code")

internal val BytecodePatchContext.getLoggedOutBearerTokenMethodMatch by baseClientIdMethod("https://oauth.reddit.com/grants/installed_client")

internal val BytecodePatchContext.getRefreshTokenMethodMatch by baseClientIdMethod("refresh_token")

internal val BytecodePatchContext.loginActivityClientIdMethodMatch by baseClientIdMethod("&duration=permanent")

internal val BytecodePatchContext.redditCheckDisableAPIMethod by gettingFirstMethodDeclaratively("Reddit Disabled") {
    instructions(Opcode.IF_EQZ())
}

internal val BytecodePatchContext.setRemoteConfigMethod by gettingFirstMethodDeclaratively("reddit_oauth_url") {
    parameterTypes("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
}


internal val BytecodePatchContext.loginActivityRedirectUriMethodMatch by composingFirstMethod {
    definingClass("Lreddit/news/oauth/LoginActivity;")
    instructions("dbrady://relay"())
}

internal val BytecodePatchContext.shouldOverrideUrlLoadingRedirectUriMethodMatch by composingFirstMethod {
    instructions("login url: "(), "dbrady://relay"())
}

internal val BytecodePatchContext.redditAccountManagerRedirectUriMethodMatch by composingFirstMethod {
    definingClass("Lreddit/news/oauth/RedditAccountManager;")
    instructions("dbrady://relay"())
}

internal val BytecodePatchContext.networkModuleUserAgentMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.STATIC)
    returnType("Lokhttp3/OkHttpClient;")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    )
}