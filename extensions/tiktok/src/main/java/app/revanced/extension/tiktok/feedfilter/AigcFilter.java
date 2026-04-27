package app.revanced.extension.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.AIGCInfo;
import com.ss.android.ugc.aweme.feed.model.Aweme;

import app.revanced.extension.tiktok.settings.Settings;

public class AigcFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_AI_GENERATED.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        final AIGCInfo aigcInfo = item.getAigcInfo();
        if (aigcInfo == null) {
            return false;
        }

        return aigcInfo.getCreateByAI() || aigcInfo.getAIGCLabelType() != 0;
    }
}
