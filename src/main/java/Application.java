import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://yadonorspb.ru/svetofor/").get();
        final Elements elements = doc.select("th");

        final List<String> bloodGroups = parsePageForBloodTypes(elements);
        
        if ( bloodGroups.size() > 4) {
            throw new RuntimeException("Too much blood types was parsed!");
        }

        final List<String> bloodNeeds = parsePageForBloodNeeds(elements);
        
        if ( bloodNeeds.size() > 8) {
            throw new RuntimeException("Too much needs was parsed!");
        }
    }

    void kek(List<String> needs) {
        needs.stream().filter(str -> str.contains("плюс"));
    }

    static List<String> parsePageForBloodTypes(Elements elements) {
        return elements.stream()
                        .map(el -> el.child(0))//onlyone child should be
                        .filter(el -> el.tag() == Tag.valueOf("h1"))
                        .map(Element::text)
                        .collect(Collectors.toList());

    }

    static List<String> parsePageForBloodNeeds(Elements elements) {
        return elements.stream()
                        .map(el -> el.child(0))//onlyone child should be
                        .filter(el -> el.tag() == Tag.valueOf("img"))
                        .map(el -> el.attr("alt"))
                        .collect(Collectors.toList());
    }

    class Blood {
        String group;
        String positive;
        String negative;
    }
}
