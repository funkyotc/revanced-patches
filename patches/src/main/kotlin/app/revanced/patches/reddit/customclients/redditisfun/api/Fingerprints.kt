package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.basicAuthorizationMethodMatch by composingFirstMethod {
    instructions(
        "yyOCBp.RHJhDKd"(),
        "fJOxVwBUyo*=f:<OoejWs:AqmIJ"(), // Encrypted basic authorization string.
    )
}

internal val BytecodePatchContext.buildAuthorizationStringMethodMatch by composingFirstMethod {
    instructions(
        "yyOCBp.RHJhDKd"(),
        "client_id"(),
    )
}

internal val BytecodePatchContext.getUserAgentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    opcodes(
        Opcode.NEW_ARRAY,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
    )
}


// TODO: These obfuscated names are bad, but the app is abandoned so these will not be changing.

internal val BytecodePatchContext.oAuth2ActivityD0MethodMatch by composingFirstMethod {
    name("d0")
    definingClass($$"OAuth2Activity$b;")
    instructions("redditisfun://auth"())
}

internal val BytecodePatchContext.oAuth2ActivityShouldOverrideUrlLoadingMethodMatch by composingFirstMethod {
    name("shouldOverrideUrlLoading")
    definingClass($$"OAuth2Activity$a;")
    instructions("redditisfun://auth"())
}

internal val BytecodePatchContext.cActivityJMethodMatch by composingFirstMethod {
    name("j")
    definingClass { endsWith("c;") }
    instructions("redditisfun://auth"())
}


internal val BytecodePatchContext.imgurApiMethod by gettingFirstMethodDeclaratively(
    "https", "api", "imgur", "3", "gallery", "album"
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Landroid/net/Uri;")
    parameterTypes("Ljava/lang/String;", "Z")
}