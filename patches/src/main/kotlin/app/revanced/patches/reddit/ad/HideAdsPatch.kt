package app.revanced.patches.reddit.ad

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/reddit/patches/HideAdsPatch;"

@Suppress("unused")
val hideAdsPatch = bytecodePatch("Hide ads") {
    dependsOn(sharedExtensionPatch)

    compatibleWith("com.reddit.frontpage")

    apply {
        // region Filter promoted ads (does not work in popular or latest feed).

        listOf(
            listingMethodMatch,
            submittedListingMethodMatch
        ).forEach { match ->
            match.let {
                it.method.apply {
                    val index = it[-1]
                    val register = getInstruction<TwoRegisterInstruction>(index).registerA

                    addInstructions(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->hideOldPostAds(Ljava/util/List;)Ljava/util/List;
                            move-result-object v$register
                        """
                    )
                }
            }
        }

        val immutableListBuilderReference = immutableListBuilderMethodMatch.let {
            it.method.getInstruction<ReferenceInstruction>(it[-1]).reference
        }

        adPostSectionConstructorMethodMatch.let {
            it.method.apply {
                val sectionIndex = it[0]
                val sectionRegister =
                    getInstruction<FiveRegisterInstruction>(sectionIndex + 1).registerC

                addInstructions(
                    sectionIndex,
                    """
                        new-instance v$sectionRegister, Ljava/util/ArrayList;
                        invoke-direct { v$sectionRegister }, Ljava/util/ArrayList;-><init>()V
                        invoke-static { v$sectionRegister }, $immutableListBuilderReference
                        move-result-object v$sectionRegister
                    """
                )
            }
        }

        // endregion

        // region Filter comment ads.

        commentsViewModelAdLoaderMethod.returnEarly()

        commentsAdStateConstructorMethodMatch.let {
            it.method.apply {
                val index = it[-1]
                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstructions(index, "const/4 v$register, 1")
            }
        }
    }

    // endregion
}
