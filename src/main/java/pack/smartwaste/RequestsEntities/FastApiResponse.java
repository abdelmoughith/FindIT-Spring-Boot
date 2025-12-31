package pack.smartwaste.RequestsEntities;

import java.util.List;
import java.util.stream.Collectors;

import static pack.smartwaste.Utils.UrlUtils.BASE_URL;

public class FastApiResponse {
    private List<String> urls;

    public void removeIP() {
        if (urls != null) {
            urls = urls.stream()
                    .map(url -> url.replace(BASE_URL, ""))
                    .collect(Collectors.toList());
        }
    }


    // Getter and Setter
    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}

