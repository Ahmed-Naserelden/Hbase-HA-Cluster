package org.example;

import org.apache.hadoop.conf.Configuration;
import org.example.hbase.HbaseWebTable;
import org.example.helpers.utils;
import org.example.webtable.WebPage;

import java.io.IOException;
import java.util.List;

public class Main {
    static Configuration config;
    static HbaseWebTable hbaseWebTable;
    static List<String> urls;
    public static void main(String[] args) {
        hbaseWebTable = new HbaseWebTable();
        try {
            urls = utils.readUrls("src/main/resources/urls.txt");

            // unComment the following comment at first time
             createDB();
            String reversedUrl = utils.reverseUrl(urls.get(1)); // "org.apache.www"
            WebPage webPage = hbaseWebTable.getWebPage(reversedUrl);

            utils.writeSep();
            System.out.println(webPage);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createDB() throws IOException {
        hbaseWebTable.createTable();
        hbaseWebTable.showConnectionInfo();
        hbaseWebTable.ingestBulkData(urls);
        hbaseWebTable.flushTable("webtable");
    }
}
