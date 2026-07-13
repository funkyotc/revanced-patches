package app.revanced.patches.zwanoo.speedtest.misc

import app.revanced.patcher.accessFlags
import app.revanced.patcher.allOf
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.reference
import app.revanced.patcher.returnType
import app.revanced.util.findFieldFromToString
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.subscriptionToStringMethod by gettingFirstImmutableMethodDeclaratively {
    name("toString")
    returnType("Ljava/lang/String;")
    instructions(
        "StUserSubscription(subscriptionTypeName="(),
        ", expiredDate="(),
    )
}

internal val BytecodePatchContext.subscriptionExpiryMethod by getting {
    val expiredDateField = contextOf<BytecodePatchContext>().subscriptionToStringMethod
        .findFieldFromToString(", expiredDate=")

    firstMethodComposite {
        returnType("Ljava/lang/String;")
        accessFlags(AccessFlags.PUBLIC)
        parameterTypes()
        name { !equals(expiredDateField.name) }
        instructions(
            allOf(Opcode.IGET_OBJECT(), field { equals(expiredDateField) }),
        )
    }
} using { subscriptionToStringMethod }

internal val BytecodePatchContext.showUpgradeDialogMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/ookla/speedtest/app/userprompt/")
    returnType("V")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes()
    instructions(
        allOf(Opcode.INVOKE_INTERFACE(), reference("speedtest/app/userprompt")),
    )
}
