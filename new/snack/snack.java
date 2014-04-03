//package snack;

import de.jetwick.snacktory.JResult;
import de.jetwick.snacktory.*;
import java.util.List;
import java.util.ArrayList;

//try to run
class snack {
  /*
  public static void main(String[] args){
    System.out.println("I'm a Simple Program");
  }
  */

  public static void main(String[] args) throws Exception {
      String newurl = "http://in2.holmesreport.com/2014/03/are-pr-engineers-the-next-thing";
      //      String newurl = "http://boss.blogs.nytimes.com/2013/08/13/today-in-small-business-walmart-vs-amazon/?_r=0";
      //      String newurl = "http://upstart.bizjournals.com/companies/rebel-brands/2013/12/16/qvc-90m-startup-plus-amazon-protests.html";
      //      String newurl = "http://www.huffingtonpost.jp/the-new-classic/post_6459_b_4457452.html";
      //      String newurl = "http://www.adverts.ie/fiction/thirty-miles-south-of-dry-county-by-kealan-patrick-burke-novella-signed/1701884";
      JResult res = new HtmlFetcher().fetchAndExtract(newurl, 10000, true);
      System.out.println("url:" + res.getUrl());
      System.out.println("text:" + res.getText());
      System.out.println("-----");
      System.out.println("author Name:" + res.getAuthorName());
      System.out.println("  Description:" + res.getAuthorDescription());
      System.out.println("-----");
      System.out.println("links:");
      List<List<String>> links = res.getLinks();
      for (List<String> link : links) {
          System.out.println("link:" + link.toString());
      }

      System.out.println("-----");
      System.out.println("date:" + res.getDate());

      //    res = new HtmlFetcher().fetchAndExtract("http://www.tumblr.com/xeb22gs619", 10000, true);
      //    System.out.println("tumblr:" + res.getUrl());
      //    System.out.println("description:" + res.getDescription());
      //    System.out.println("text:" + res.getText());
  }

}
