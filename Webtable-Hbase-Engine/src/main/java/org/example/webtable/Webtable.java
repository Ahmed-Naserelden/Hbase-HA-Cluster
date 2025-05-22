package org.example.webtable;
import org.example.hbase.HbaseWebTable;
import java.io.IOException;
import java.util.List;

public class Webtable {

    public static void main(String[] args) {
        HbaseWebTable hbase = new HbaseWebTable();
        try {

            hbase.createTable();
            hbase.showConnectionInfo();
            List<String> urls;
            urls = List.of(
                "http://www.hbase.org",
                "http://www.apache.org",
                "http://www.example.com",
                "http://www.github.com",
                "http://www.wikipedia.org"
            );

            WebPage webPage = hbase.getWebPage("org.hbase.www");
            System.out.println(webPage.getLanguage());

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}