package com.detwiler.hackernews.debug;

import com.detwiler.hackernews.AuthenticationException;
import com.detwiler.hackernews.model.HnComment;
import com.detwiler.hackernews.HnPostCategory;
import com.detwiler.hackernews.HnScraper;
import com.detwiler.hackernews.model.HnSubmission;
import com.detwiler.hackernews.server.HnPostDocument;
import com.detwiler.hackernews.server.HnPostListDocument;
import com.detwiler.hackernews.server.HnSessionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HnScraperDebug implements Runnable, HnSessionManager.CredentialDelegate {
    private Map<String, String> mCredentials;

    public HnScraperDebug() {
        mCredentials = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("HnScraperDebug/src/main/resources/accounts.cfg")));
            String line = reader.readLine();
            while (line != null) {
                String[] userpass = line.split(",");
                mCredentials.put(userpass[0].trim(), userpass[1].trim());
                line = reader.readLine();
            }
            reader.close();
        } catch (final Exception e) {
            System.out.println("Exception while loading credentials: " + e);
            System.exit(1);
        }
    }

    private void dumpDocument(final HnPostListDocument doc) {
        for (HnSubmission post : doc.getPosts()) {
            System.out.println(post);
        }
        System.out.println("More: " + doc.getNextPageHref());
    }

    private void readPostLists(final HnScraper scraper) {
        HnPostListDocument doc;
        try {
            HnPostCategory[] categories = HnPostCategory.values();
            for (final HnPostCategory category : categories) {
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

    private static void readPost(final HnScraper scraper, final String id) {
        HnPostDocument doc;
        try {
            doc = scraper.getPost(id);
            for (final HnComment comment : doc.getComments()) {
                int depth = 0;
                HnComment parent = comment.getParent();
                while (parent != null) {
                    depth++;
                    parent = parent.getParent();
                }
                String indent = "";
                for (int i=0; i<depth; ++i) {
                    indent += "\t";
                }
                System.out.println(indent + "Comment '" + comment.getPostId() + "' by " + comment.getUsername() + ":");
                System.out.println(indent + comment.getText());
            }
        } catch (IOException e) {
            System.out.println("ERROR: Unable to connect to HN server");
            System.exit(-1);
            return;
        }
    }

    @Override
    public void run() {
        HnScraper scraper = new HnScraper();
        scraper.setCredentialDelegate(this);
        try {
            scraper.setActiveUser(mCredentials.keySet().iterator().next());
        } catch (final AuthenticationException e) {
            System.out.println("Unable to authenticate user: " + e);
            System.exit(1);
        }
        // readPost(scraper, "6706065");
        readPostLists(scraper);
    }

    public static void main(String[] args) {
        new HnScraperDebug().run();
    }

    @Override
    public String getPasswordForUser(String username) {
        return mCredentials.get(username);
    }
}
