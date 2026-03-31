package app.revanced.patches.reddit.ad

import app.revanced.patcher.accessFlags
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.firstImmutableMethodDeclaratively
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.findFieldFromToString
import app.revanced.util.getting
import app.revanced.util.using
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.listingMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    definingClass("Lcom/reddit/domain/model/listing/Listing;")

    var methodDefiningClass = ""
    custom {
        methodDefiningClass = definingClass
        true
    }

    instructions(
        "children"(),
        "uxExperiences"(),
        Opcode.INVOKE_DIRECT(),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { definingClass == methodDefiningClass && name == "children" }),
    )
}

internal val BytecodePatchContext.submittedListingMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    definingClass("Lcom/reddit/domain/model/listing/SubmittedListing;")
    instructions(
        "children"(),
        "videoUploads"(),
        Opcode.INVOKE_DIRECT(),
        Opcode.IPUT_OBJECT(),
    )
}
internal val BytecodePatchContext.adPostSectionConstructorMethodMatch by getting {
    firstMethodComposite {
        returnType("V")
        name("<init>")
        instructions(
            "sections"(),
        )
    }
} using {
    firstImmutableMethodDeclaratively {
        accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
        returnType("Ljava/lang/String;")
        name("toString")
        parameterTypes()
        instructions("AdPostSection(linkId="(),)
    }
}

internal val BytecodePatchContext.commentsAdStateToStringMethod by gettingFirstImmutableMethodDeclaratively {
    name("toString")
    returnType("Ljava/lang/String;")
    instructions(
        "CommentsAdState(conversationAdViewState="(),
        ", adsLoadCompleted="(),
    )
}

val BytecodePatchContext.commentsAdStateConstructorMethodMatch by getting {
    val adsLoadCompletedField = contextOf<BytecodePatchContext>().commentsAdStateToStringMethod
        .findFieldFromToString(", adsLoadCompleted=")

    firstMethodComposite {
        name("<init>")
        returnType("V")
        instructions(
            allOf(
                Opcode.IPUT_BOOLEAN(), field { this == adsLoadCompletedField }
            ),
        )
    }
} using { commentsAdStateToStringMethod }

internal val BytecodePatchContext.commentsViewModelAdLoaderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    definingClass("Lcom/reddit/comments/presentation/CommentsViewModel;")
    parameterTypes("Z", "L", "I")
    instructions(
        allOf(
            Opcode.INVOKE_DIRECT(),
            method { name == "<init>" && returnType == "V" && parameterTypes == listOf("Z", "I") }),
    )
}

internal val BytecodePatchContext.immutableListBuilderMethodMatch by composingFirstMethod {
    returnType("V")
    name("<clinit>")
    parameterTypes()
    instructions(
        allOf(
            Opcode.INVOKE_STATIC(),
            method {
                name == "getEntries" &&
                        definingClass == "Lcom/reddit/accessibility/AutoplayVideoPreviewsOption;"
            }
        ),
        allOf(
            Opcode.INVOKE_STATIC(),
            method { parameterTypes == listOf("Ljava/lang/Iterable;") }),
    )
}