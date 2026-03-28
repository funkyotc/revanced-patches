package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith(
        "free.reddit.news",
        "reddit.news",
    )

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        fun CompositeMatch.patch(
            string: String,
            getReplacementIndex: List<Int>.() -> Int,
        ) = method.apply {
            val replacementIndex = indices[0].getReplacementIndex()
            val stringRegister = getInstruction<OneRegisterInstruction>(replacementIndex).registerA

            replaceInstruction(replacementIndex, "const-string v$stringRegister, \"$string\"")
        }

        listOf(
            loginActivityClientIdMethodMatch,
            getLoggedInBearerTokenMethodMatch,
            getLoggedOutBearerTokenMethodMatch,
            getRefreshTokenMethodMatch,
        ).forEach { match ->
            match.patch(clientId!!) { first() }
        }

        if (redirectUri != null) {
            setOf(
                loginActivityRedirectUriMethodMatch,
                shouldOverrideUrlLoadingRedirectUriMethodMatch,
                redditAccountManagerRedirectUriMethodMatch
            ).forEach { match ->
                match.patch(redirectUri!!) { last() }
            }
        }

        if (userAgent != null) {
            networkModuleUserAgentMethodMatch.let {
                it.method.apply {
                    val index = it[0]
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    val userAgentField = it.classDef.fields.first { field ->
                        field.type == "Ljava/lang/String;"
                    }

                    addInstructions(
                        index,
                        """
                            const-string v$register, "$userAgent"
                            sput-object v$register, $userAgentField
                        """
                    )
                }
            }
        }

        // Do not load remote config which disables OAuth login remotely.
        setRemoteConfigMethod.returnEarly()

        // Prevent OAuth login being disabled remotely.
        redditCheckDisableAPIMethod.apply {
            val checkIsOAuthRequestIndex = indexOfFirstInstructionOrThrow(Opcode.IF_EQZ)
            val returnNextChain =
                getInstruction<BuilderInstruction21t>(checkIsOAuthRequestIndex).target
            replaceInstruction(
                checkIsOAuthRequestIndex,
                BuilderInstruction10t(Opcode.GOTO, returnNextChain)
            )
        }
    }
}
