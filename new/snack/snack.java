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
    JResult res = new HtmlFetcher().fetchAndExtract(newurl, 10000, true);
    System.out.println("holmes:" + res.getUrl());
    System.out.println("text:" + res.getText());
    System.out.println("author Name:" + res.getAuthorName());
    System.out.println("  Description:" + res.getAuthorDescription());

    List<List<String>> links = res.getLinks();
    for (List<String> link : links) {
        System.out.println("link:" + link.toString());
    }

    //    res = new HtmlFetcher().fetchAndExtract("http://www.tumblr.com/xeb22gs619", 10000, true);
    //    System.out.println("tumblr:" + res.getUrl());
    //    System.out.println("description:" + res.getDescription());
    //    System.out.println("text:" + res.getText());
  }

}
