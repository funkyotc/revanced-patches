package app.revanced.patches.reddit.layout.community

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideRecommendedCommunitiesShelfPatch = bytecodePatch(
    name = "Hide recommended communities shelf",
    description = "Hides the recommended communities shelves in subreddits."
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        communityRecommendationSectionMethod.returnEarly()
    }
}
