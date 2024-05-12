package is322_HW_3;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class test {
    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_pharaohs").get();
        Elements p = document.select("p");
        System.out.println(p.text() + "\n\n");
        
        // Elements linksOnPage = document.select("a[href]");
        // System.out.println(linksOnPage.text());

        // for(int i = 0; i < linksOnPage.size(); i++) {
        //     System.out.println(linksOnPage.get(i).attr("abs:href"));
        // }

        // for (int i = 0; i < p.size(); i++) {
        //     System.out.println(p.get(i).text());
        // }
    }
}
