package com.detwiler.hackernews.debug;

import com.detwiler.hackernews.HnPost;
import com.detwiler.hackernews.HnPostCategory;
import com.detwiler.hackernews.HnScraper;
import com.detwiler.hackernews.server.HnPostListDocument;

import java.io.IOException;

public class HnScraperDebug {

    private static void dumpDocument(final HnPostListDocument doc) {
        for (HnPost post : doc.getPosts()) {
            System.out.println(post);
        }
        System.out.println("More: " + doc.getNextPageHref());
    }

    public static void main(String[] args) {
        HnScraper scraper = new HnScraper();
        HnPostListDocument doc;
        try {
            for (final HnPostCategory category : HnPostCategory.values()) {
                System.out.println("Reading category: " + category);
                doc = scraper.getPostsForCategory(category);
                for (int i=0; i<1; ++i) {
                    dumpDocument(doc);
                    if (!doc.hasMore()) {
                        break;
                    }
                    doc = doc.more();
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Unable to connect to HN server");
            System.exit(-1);
            return;
        }
    }
}
