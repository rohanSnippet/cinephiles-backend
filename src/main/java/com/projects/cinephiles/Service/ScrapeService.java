package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.ProfilesUrl;
import com.projects.cinephiles.models.CrewMember;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScrapeService {

public static String newImgUrl(String name) throws IOException {
    String newval = name+"_(actor)";
    String nextUrl = "https://en.wikipedia.org/wiki/"+newval;
    Document doc1 = Jsoup.connect(nextUrl).get();
   String imgUrl = doc1.select("td.infobox-image img").attr("src");
    System.out.println(newval+"----->"+nextUrl);
    return imgUrl;
}

    public static ProfilesUrl scrapeActor(Map<String, String> cast) {
        ProfilesUrl res = new ProfilesUrl();
        Map<String, String> urls = new HashMap<>();

        try {
            for (String name : cast.keySet()) {
                String val = name.replaceAll(" ", "_");
                System.out.println("Fetching data for: " + val);

                String url = "https://en.wikipedia.org/wiki/" + val;

                try {
                    Document doc = Jsoup.connect(url).get();
                    String imgUrl = doc.select("td.infobox-image img").attr("src");

                    if (imgUrl == null || imgUrl.isEmpty()) {
                        imgUrl = newImgUrl(name);
                        if (imgUrl == null) imgUrl = "No Image";
                    }
                    urls.put(name, imgUrl);

                } catch (HttpStatusException e) {
                    // Handle 404 or other HTTP errors for a specific page
                    System.out.println("No page found for " + name + " (Error: " + e.getStatusCode() + ")");
                    urls.put(name, "No Image");

                } catch (Exception innerEx) {
                    // Handle other unexpected errors for this document
                    System.out.println("Error retrieving image for " + name + ": " + innerEx.getMessage());
                    urls.put(name, "No Image");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ProfilesUrl();  // Return empty ProfilesUrl if top-level error occurs
        }

        res.setUrls(urls);
        return res;
    }
    public static String newCrewUrl(String name) throws IOException {
        String newval = name+"_(company)";
        String nextUrl = "https://en.wikipedia.org/wiki/"+newval;
        Document doc1 = Jsoup.connect(nextUrl).get();
        String imgUrl = doc1.select("td.infobox-image img").attr("src");
        System.out.println(newval+"----->"+nextUrl);
        return imgUrl;
    }
    public static ProfilesUrl scrapeCrew(List<CrewMember> crews) {
        ProfilesUrl res = new ProfilesUrl();
        Map<String, String> urls = new HashMap<>();
        try {
            // Iterate through each crew member in the list
            for (CrewMember member : crews) {
                String name = member.getName().replaceAll(" ", "_");
                String url = "https://en.wikipedia.org/wiki/" + name;

                try {
                    Document doc = Jsoup.connect(url).get();
                    String imgUrl = doc.select("td.infobox-image img").attr("src");
                    if (imgUrl == null || imgUrl.isEmpty()) {
                        imgUrl = newCrewUrl(name);
                        if(imgUrl.isEmpty()) imgUrl = "No Image";
                    }
                    urls.put(member.getName(), imgUrl);
                } catch (HttpStatusException e) {
                    System.out.println("No page found for " + member.getName() + " (Error: " + e.getStatusCode() + ")");
                    urls.put(member.getName(), "No Image");
                } catch (Exception innerEx) {
                    System.out.println("Error retrieving image for " + member.getName() + ": " + innerEx.getMessage());
                    urls.put(member.getName(), "No Image");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ProfilesUrl();  // Return empty ProfilesUrl if a top-level error occurs
        }
        res.setUrls(urls);
        return res;
    }
}
