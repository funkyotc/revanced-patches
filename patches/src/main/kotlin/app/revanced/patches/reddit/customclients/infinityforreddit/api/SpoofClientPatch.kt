package app.revanced.patches.reddit.customclients.infinityforreddit.api

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.toInstructions
import app.revanced.patches.reddit.customclients.spoofClientPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import java.util.logging.Logger

val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith(
        "ml.docilealligator.infinityforreddit",
        "ml.docilealligator.infinityforreddit.plus",
        "ml.docilealligator.infinityforreddit.patreon",
    )

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        apiUtilsMethod.classDef.methods.apply {
            val getClientIdMethod = single { it.name == "getId" }.also(::remove)

            val newGetClientIdMethod = ImmutableMethod(
                getClientIdMethod.definingClass,
                getClientIdMethod.name,
                null,
                getClientIdMethod.returnType,
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                null,
                null,
                ImmutableMethodImplementation(
                    1,
                    """
                        const-string v0, "$clientId"
                        return-object v0
                    """.toInstructions(getClientIdMethod),
                    null,
                    null,
                ),
            ).toMutable()

            add(newGetClientIdMethod)
        }

        if (redirectUri != null || userAgent != null) {
            Logger.getLogger(this::class.java.name).warning(
                "Patching redirect URI and user agent is not supported for Infinity for Reddit. " +
                        "Only the client ID will be patched."
            )
        }
    }
}
