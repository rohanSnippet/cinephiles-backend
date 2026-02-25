package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.ProfilesUrl;
import com.projects.cinephiles.models.CrewMember;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScrapeService {

    // Helper method to properly format names for Wikipedia (Title Case + Underscores)
    // Example: "tom cruise" -> "Tom_Cruise"
    private static String formatWikiName(String name) {
        if (name == null || name.isEmpty()) return "";
        String[] words = name.trim().split("\\s+");
        StringBuilder wikiName = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                wikiName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append("_");
            }
        }
        if (wikiName.length() > 0) wikiName.setLength(wikiName.length() - 1);
        return wikiName.toString();
    }

    // Generic helper to fetch an image from a given Wikipedia URL safely
    private static String fetchImageSafely(String url) {
        try {
            // Adding a user agent helps prevent Wikipedia from blocking the request
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            // Try multiple selectors to ensure we catch the image
            // Note: abs:src automatically prepends https: to //upload.wikimedia.org...
            String imgUrl = doc.select("table.infobox img").attr("abs:src");

            if (imgUrl == null || imgUrl.isEmpty()) {
                imgUrl = doc.select(".infobox-image img").attr("abs:src");
            }
            if (imgUrl == null || imgUrl.isEmpty()) {
                imgUrl = doc.select("div.thumbinner img").attr("abs:src"); // Fallback for pages without infoboxes
            }

            return (imgUrl != null && !imgUrl.isEmpty()) ? imgUrl : null;

        } catch (Exception e) {
            // Silently catch 404s or connection issues so it doesn't break the loop
            return null;
        }
    }

    public ProfilesUrl scrapeActor(Map<String, String> cast) {
        ProfilesUrl res = new ProfilesUrl();
        Map<String, String> urls = new HashMap<>();

        if (cast == null) return res;

        for (String name : cast.keySet()) {
            String formattedName = formatWikiName(name);
            System.out.println("Fetching data for Actor: " + formattedName);

            // Attempt 1: Standard Name
            String url = "https://en.wikipedia.org/wiki/" + formattedName;
            String imgUrl = fetchImageSafely(url);

            // Attempt 2: Add _(actor) if the standard name page doesn't exist or has no image
            if (imgUrl == null) {
                String fallbackUrl = "https://en.wikipedia.org/wiki/" + formattedName + "_(actor)";
                System.out.println("Fallback attempt for: " + formattedName + " -> " + fallbackUrl);
                imgUrl = fetchImageSafely(fallbackUrl);
            }

            // Attempt 3: Add _(Indian_actor) or similar if needed (optional based on your dataset)
            if(imgUrl == null){
                String fallbackUrl2 = "https://en.wikipedia.org/wiki/" + formattedName + "_(Indian_actor)";
                imgUrl = fetchImageSafely(fallbackUrl2);
            }

            // Final check
            if (imgUrl == null) {
                System.out.println("No image found for actor: " + name);
                urls.put(name, "No Image");
            } else {
                urls.put(name, imgUrl);
            }
        }

        res.setUrls(urls);
        return res;
    }

    public ProfilesUrl scrapeCrew(List<CrewMember> crews) {
        ProfilesUrl res = new ProfilesUrl();
        Map<String, String> urls = new HashMap<>();

        if (crews == null) return res;

        for (CrewMember member : crews) {
            String name = member.getName();
            String formattedName = formatWikiName(name);
            System.out.println("Fetching data for Crew: " + formattedName);

            // Attempt 1: Standard Name
            String url = "https://en.wikipedia.org/wiki/" + formattedName;
            String imgUrl = fetchImageSafely(url);

            // Attempt 2: Add _(filmmaker) or _(director) fallback
            if (imgUrl == null) {
                String fallbackUrl = "https://en.wikipedia.org/wiki/" + formattedName + "_(filmmaker)";
                System.out.println("Fallback attempt for: " + formattedName + " -> " + fallbackUrl);
                imgUrl = fetchImageSafely(fallbackUrl);
            }

            if (imgUrl == null) {
                String fallbackUrl2 = "https://en.wikipedia.org/wiki/" + formattedName + "_(director)";
                imgUrl = fetchImageSafely(fallbackUrl2);
            }

            // Final check
            if (imgUrl == null) {
                System.out.println("No image found for crew: " + name);
                urls.put(name, "No Image");
            } else {
                urls.put(name, imgUrl);
            }
        }

        res.setUrls(urls);
        return res;
    }

    @Cacheable(value = "wikiImages", key = "#name.toLowerCase()")
    public Map<String, String> scrapeSinglePerson(String name, boolean isCrew) {
        Map<String, String> result = new HashMap<>();
        String formattedName = formatWikiName(name);
        String imgUrl = fetchImageSafely("https://en.wikipedia.org/wiki/" + formattedName);

        // Fallbacks if the main page fails or has no image
        if (imgUrl == null) {
            if (isCrew) {
                imgUrl = fetchImageSafely("https://en.wikipedia.org/wiki/" + formattedName + "_(filmmaker)");
                if (imgUrl == null) imgUrl = fetchImageSafely("https://en.wikipedia.org/wiki/" + formattedName + "_(director)");
            } else {
                imgUrl = fetchImageSafely("https://en.wikipedia.org/wiki/" + formattedName + "_(actor)");
                if (imgUrl == null) imgUrl = fetchImageSafely("https://en.wikipedia.org/wiki/" + formattedName + "_(Indian_actor)");
            }
        }

        result.put("url", imgUrl != null ? imgUrl : "No Image");
        return result;
    }
}