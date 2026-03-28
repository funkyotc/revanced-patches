package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.firstImmutableMethodDeclaratively
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.authUtilityUserAgentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    opcodes(Opcode.APUT_OBJECT)
    custom { immutableClassDef.sourceFile == "AuthUtility.java" }
}

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { immutableClassDef.sourceFile == "AuthUtility.java" }
}

internal val BytecodePatchContext.oauthHelperConstructorMethodMatch by getting {
    firstMethodComposite {
        accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
        opcodes(Opcode.INVOKE_STATIC)
    }
} using {
    firstImmutableMethodDeclaratively {
        instructions("This method is not appropriate for this authentication method"())
    }
}

internal val BytecodePatchContext.oauthContainsCodeMethodMatch by composingFirstMethod {
    instructions("code="())
    opcodes(
        Opcode.MOVE_RESULT,
        Opcode.RETURN
    )
    custom { immutableClassDef.sourceFile == "LoginActivity.java" }
}