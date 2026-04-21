package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class ShopFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_SHOP.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        String url = item.getShareUrl();
        if (url == null) return false;
        return url.contains("placeholder_product_id")
                || url.contains("shop_tab_feed")
                || url.contains("ec_shared_reflux");
    }
}
