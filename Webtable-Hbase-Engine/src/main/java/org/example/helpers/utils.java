package org.example.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class utils {
    public static String reverseUrl(String url) {
        String cleanedUrl = url.replace("http://", "").replace("https://", "").replace("/", "");
        String[] parts = cleanedUrl.split("\\.");
        StringBuilder reversed = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            reversed.append(parts[i]);
            if (i > 0) reversed.append(".");
        }
        return reversed.toString();
    }

    public static List<String> readUrls(String filePath) throws IOException {
        List<String> urls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    urls.add(line.trim());
                }
            }
        }
        return urls;
    }

    public static void writeSep(){
        System.out.println("#############################################");
        System.out.println("#############################################");
        System.out.println("#############################################");
        System.out.println("#############################################");
        System.out.println();
    }
}
