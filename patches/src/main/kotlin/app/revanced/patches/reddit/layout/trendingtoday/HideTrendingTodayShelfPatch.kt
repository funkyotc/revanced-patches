package app.revanced.patches.reddit.layout.trendingtoday

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideTrendingTodayShelfPatch = bytecodePatch(
    name = "Hide 'Trending Today' shelf",
    description = "Hides the 'Trending Today' shelf from search suggestions."
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        searchTypeaheadListDefaultPresentationConstructorMethod
            .addInstructions(1, "const-string p1, \"\"")

        trendingTodayItemMethod.returnEarly()
    }
}
