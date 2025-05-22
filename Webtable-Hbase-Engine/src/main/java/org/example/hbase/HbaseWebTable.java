package org.example.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.example.webtable.WebPage;
import org.example.helpers.utils;

import java.io.IOException;
import java.util.List;

public class HbaseWebTable {
    private Configuration config;

    public HbaseWebTable(){
        this.setConnection();
    }

    private void setConnection() {
        config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "master1,master2,master3");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        config.set("hbase.security.authentication", "simple");
        config.set("hbase.master", "hb-master1:60000");
        config.set("hbase.rootdir", "hdfs://hb-master1:9000/hbase"); // Added based on your setup
    }

    public void createTable() throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(this.config);
             Admin admin = connection.getAdmin()) {
            TableName tableName = TableName.valueOf("webtable");
            if (!admin.tableExists(tableName)) {
                TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(tableName);
                tableBuilder.setColumnFamily(
                        ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("contents"))
                                .setMaxVersions(3)
//                                .setCompressionType(Compression.Algorithm.SNAPPY)
                                .build()
                );
                tableBuilder.setColumnFamily(
                        ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("anchor"))
//                                .setCompressionType(Compression.Algorithm.SNAPPY)
                                .build()
                );
                tableBuilder.setColumnFamily(
                        ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("metadata"))
//                                .setCompressionType(Compression.Algorithm.SNAPPY)
                                .build()
                );
                admin.createTable(tableBuilder.build());
                System.out.println("Table 'webtable' created.");
            } else {
                System.out.println("Table 'webtable' already exists.");
            }
        }
    }

    public void showConnectionInfo() {
        System.out.println("ZooKeeper Quorum: " + config.get("hbase.zookeeper.quorum"));
        System.out.println("HBase Root Dir: " + config.get("hbase.rootdir"));
        System.out.println("HBase Master: " + config.get("hbase.master"));
    }

    public void ingestData(String url) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(this.config);
             Table table = connection.getTable(TableName.valueOf("webtable"))) {
            String reversedUrl = utils.reverseUrl(url);
            WebPage page = new WebPage(url);
            long crawlTimestamp = System.currentTimeMillis();

            Put put = new Put(Bytes.toBytes(reversedUrl));
            put.addColumn(Bytes.toBytes("contents"), Bytes.toBytes("html"), crawlTimestamp,
                    Bytes.toBytes(page.html));

            for (String link : page.outLinks) {
                put.addColumn(Bytes.toBytes("anchor"), Bytes.toBytes("out:" + utils.reverseUrl(link)), crawlTimestamp,
                        Bytes.toBytes(link));
            }

            put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("language"), crawlTimestamp,
                    Bytes.toBytes(page.language));
            table.put(put);
            System.out.println("Ingested data for " + reversedUrl);
        }
    }

    public void ingestBulkData(List<String> urls) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(this.config);
             Table table = connection.getTable(TableName.valueOf("webtable"))) {

            for(String url: urls){
                String reversedUrl = utils.reverseUrl(url);
                WebPage page = new WebPage(url);
                long crawlTimestamp = System.currentTimeMillis();

                Put put = new Put(Bytes.toBytes(reversedUrl));
                put.addColumn(Bytes.toBytes("contents"), Bytes.toBytes("html"), crawlTimestamp,
                        Bytes.toBytes(page.html));

                for (String link : page.outLinks) {
                    put.addColumn(Bytes.toBytes("anchor"), Bytes.toBytes("out:" + utils.reverseUrl(link)), crawlTimestamp,
                            Bytes.toBytes(link));
                }

                put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("language"), crawlTimestamp,
                        Bytes.toBytes(page.language));
                table.put(put);
                System.out.println("Ingested data for " + reversedUrl);
            }
        }
    }

    public  void flushTable(String tableName) {
        try (Connection connection = ConnectionFactory.createConnection(config);
             Admin admin = connection.getAdmin()) {

            System.out.println("Flushing table: " + tableName);
            admin.flush(TableName.valueOf(tableName));
            System.out.println("Flush completed.");
        } catch (Exception e) {
            System.err.println("Error flushing table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void queryData(String reversedUrl) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(this.config);
             Table table = connection.getTable(TableName.valueOf("webtable"))) {
            Get get = new Get(Bytes.toBytes(reversedUrl));
            Result result = table.get(get);

            byte[] html = result.getValue(Bytes.toBytes("contents"), Bytes.toBytes("html"));
            System.out.println("HTML: " + (html != null ? Bytes.toString(html) : "Not found"));

            byte[] language = result.getValue(Bytes.toBytes("metadata"), Bytes.toBytes("language"));
            System.out.println("Language: " + (language != null ? Bytes.toString(language) : "Not found"));

            for (Cell cell : result.rawCells()) {
                String family = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                if ("anchor".equals(family)) {
                    String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    if (qualifier.startsWith("out:")) {
                        String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                        System.out.println("Link: " + qualifier + " -> " + value);
                    }
                }
            }

            System.out.println("\nVersions of contents:html:");
            for (Cell cell : result.getColumnCells(Bytes.toBytes("contents"), Bytes.toBytes("html"))) {
                System.out.println("Timestamp " + cell.getTimestamp() + ": " +
                        Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
        }
    }

    public WebPage getWebPage(String reversedUrl) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(this.config);
             Table table = connection.getTable(TableName.valueOf("webtable"))) {
            WebPage webPage = new WebPage();
            Get get = new Get(Bytes.toBytes(reversedUrl));
            Result result = table.get(get);

            byte[] html = result.getValue(Bytes.toBytes("contents"), Bytes.toBytes("html"));
            webPage.setHtml(Bytes.toString(html));
            byte[] language = result.getValue(Bytes.toBytes("metadata"), Bytes.toBytes("language"));
            webPage.setLanguage(Bytes.toString(language));

            for (Cell cell : result.rawCells()) {
                String family = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                if ("anchor".equals(family)) {
                    String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    if (qualifier.startsWith("out:")) {
                        String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                        webPage.addOutLink(value);
                    }

                }
            }
            return webPage;
        }
    }
}
