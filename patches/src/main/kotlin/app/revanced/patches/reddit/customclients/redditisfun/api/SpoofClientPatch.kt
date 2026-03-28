package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.stringReference
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith(
        "com.andrewshu.android.reddit",
        "com.andrewshu.android.redditdonation",
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

        buildAuthorizationStringMethodMatch.patch(clientId!!) { first() + 4 }
        basicAuthorizationMethodMatch.patch("$clientId:") { last() + 7 }

        if (redirectUri != null) {
            listOf(
                oAuth2ActivityD0MethodMatch,
                oAuth2ActivityShouldOverrideUrlLoadingMethodMatch,
                cActivityJMethodMatch
            ).forEach { match ->
                match.patch(redirectUri!!) { first() }
            }
        }

        if (userAgent != null)
            getUserAgentMethod.returnEarly(userAgent!!)


        // Reddit messed up and does not append a redirect uri to the authorization url to old.reddit.com/login.
        // Replace old.reddit.com with www.reddit.com to fix this.
        buildAuthorizationStringMethodMatch.method.apply {
            val index = indexOfFirstInstructionOrThrow {
                stringReference?.contains("old.reddit.com") == true
            }

            val targetRegister = getInstruction<OneRegisterInstruction>(index).registerA
            replaceInstruction(
                index,
                "const-string v$targetRegister, " +
                        "\"https://www.reddit.com/api/v1/authorize.compact\"",
            )
        }

        imgurApiMethod.addInstructionsWithLabels(
            0,
            """
                const-string v0, "https://api.imgur.com/3/gallery/album/"
                if-nez p1, :isGallery
                const-string v0, "https://api.imgur.com/3/album/"
             
                :isGallery
                invoke-static { v0, p0 }, Ljava/lang/String;->concat(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                move-result-object v0
                
                invoke-static { v0 }, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
                move-result-object v0

                return-object v0
            """
        )
    }
}
