package org.example.webtable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebPage {
    public String html;
    public List<String> outLinks;
    public String language;


    public WebPage(){
        this.outLinks = new ArrayList<>();
    }

    public WebPage(String html, List<String> outLinks, String language){

        this.html = html;
        this.outLinks = outLinks;
        this.language = language;
    }

    public WebPage(String url) throws IOException {

        Document doc = Jsoup.connect(url).get();
        this.html = doc.html();
        this.outLinks = new ArrayList<>();
        Elements links = doc.select("a[href]");
        for (var link : links) {
            String href = link.attr("abs:href");
            if (!href.isEmpty()) outLinks.add(href);
        }
        this.language = doc.select("html").attr("lang");
        if (this.language.isEmpty()) this.language = "en";
    }

    public void setHtml(String html) {this.html = html;}
    public void setOutLinks(List<String> outLinks) {this.outLinks = outLinks;}
    public void setLanguage(String language) {this.language = language;}
    public void addOutLink(String outLink){
        this.outLinks.add(outLink);
    }

    @Override
    public String toString() {
        return "HTML: " + this.html + "\n" +
                "Lang: " + this.language + "\n" +
                "OutLinks: " + this.outLinks + "\n";
    }

    public String getHtml() {return this.html;}
    public List<String> getOutLinks() {return this.outLinks;}
    public String getLanguage() {return this.language;}
    public String getOutLink(int index){
        return outLinks.get(index);
    }
}
