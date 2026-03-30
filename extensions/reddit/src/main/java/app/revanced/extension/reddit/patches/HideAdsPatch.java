package app.revanced.extension.reddit.patches;

import com.reddit.domain.model.ILink;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class HideAdsPatch {
    /**
     * Injection point.
     */
    public static List<?> hideOldPostAds(List<?> list) {
        List<Object> filteredList = new ArrayList<>();

        for (var item : list)
            if (!(item instanceof ILink iLink) || !iLink.getPromoted())
                filteredList.add(item);

        return filteredList;
    }
}
