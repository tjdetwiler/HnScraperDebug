package com.detwiler.hackernews.debug;

import com.detwiler.hackernews.*;

import java.io.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class HnScraperDebug implements Runnable, HnCredentialDelegate {
    private static final String ACCOUNT_RESOURCE_FILE = "/accounts.cfg";
    private Map<String, String> mCredentials;

    public static void main(final String[] args) {
        new HnScraperDebug().run();
    }

    public HnScraperDebug() {
        mCredentials = new HashMap<>();
        try {
            final InputStream is = getClass().getResourceAsStream(ACCOUNT_RESOURCE_FILE);
            if (is == null) {
                throw new FileNotFoundException("Missing resource file: " + ACCOUNT_RESOURCE_FILE);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                String[] userpass = line.split(",");
                mCredentials.put(userpass[0].trim(), userpass[1].trim());
                line = reader.readLine();
            }
            reader.close();
        } catch (final Exception ex) {
            unexpectedException("loading credentials", ex);
        }
    }

    @Override
    public String getPasswordForUser(final String username) {
        return mCredentials.get(username);
    }

    @Override
    public void run() {
        String action = "";
        final HnScraper scraper = new HnScraper();
        scraper.setCredentialDelegate(this);
        try {
            action = "authenticating user";
            scraper.setActiveUser(mCredentials.keySet().iterator().next());
        } catch (final HnAuthenticationException e) {
            unexpectedException(action, e);
        }

        try {
            final HnPostDocument doc;
            final Map<HnPostCategory, HnPostListDocument> postLists = new EnumMap<>(HnPostCategory.class);
            HnPostListDocument postListDoc;
            for (HnPostCategory category : HnPostCategory.values()) {
                action = "reading post category " + category;
                postListDoc = scraper.getPostsForCategory(HnPostCategory.TOP);
                postLists.put(category, postListDoc);
                System.out.println("\n\n\n" + category + ": ");
                for (final HnSubmission post : postListDoc.getPosts()) {
                    System.out.println(post.getTitle());
                }
            }
            System.out.println("\n\n\nComments on Top Post: ");
            final HnPost topPost = postLists.get(HnPostCategory.TOP).getPosts().get(0);
            action = "reading post " + topPost.getPostId();
            doc = scraper.getPost(topPost);
            for (final HnComment comment : doc.getComments()) {
                printComment(comment);
            }
        } catch (final IOException e) {
            unexpectedException(action ,e);
        }
    }

    private void printComment(final HnComment comment) {
        printComment(comment, 0);
    }

    private void printComment(final HnComment comment, final int depth) {
        String indent = "";
        for (int i=0; i<depth; ++i) {
            indent += "\t";
        }
        System.out.println(indent + "Comment '" + comment.getPostId() + "' by " + comment.getUsername() + " (" + comment.getReplies().size() + " replies) :");
        System.out.println(indent + comment.getText() + "\n");
        for (final HnComment response : comment.getReplies()) {
            printComment(response, depth + 1);
        }
    }

    private void unexpectedException(final String action, final Exception ex) {
        System.out.println("Exception while " + action + ": " + ex);
        for (StackTraceElement e : ex.getStackTrace()) {
            System.out.printf("\t");
            System.out.println(e);
        }
        System.exit(1);
    }
}
