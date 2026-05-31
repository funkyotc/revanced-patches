package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.*
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import kotlin.properties.ReadOnlyProperty

internal val BytecodePatchContext.jsonHookPatchMethodMatch by ReadOnlyProperty { context, _ ->
    context.firstImmutableClassDef(JSON_HOOK_PATCH_CLASS_DESCRIPTOR).firstMethodComposite {
        name("<clinit>")
        opcodes(
            Opcode.SGET_OBJECT, // Get DummyHook object.
            Opcode.INVOKE_INTERFACE, // Add hook to the hooks list.
        )
    }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getJsonInputStreamMethod() = firstMethodDeclaratively {
    custom {
        if (parameterTypes.isEmpty()) {
            false
        } else {
            parameterTypes.first() == "Ljava/io/InputStream;"
        }
    }
}

internal val BytecodePatchContext.loganSquareClassDef by gettingFirstImmutableClassDef {
    type.endsWith("LoganSquare;")
}
