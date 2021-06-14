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

        if (bloodGroups.size() > 4) {
            throw new RuntimeException("Too much blood types was parsed!");
        }

        final List<String> bloodNeedsWithRh = parsePageForBloodNeedsWithRh(elements);

        if (bloodNeedsWithRh.size() > 8) {
            throw new RuntimeException("Too much needs was parsed!");
        }

        final List<Blood> bloods = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (String bl : bloodGroups) {
                final Blood blood = new Blood();
                // blood needs and Rh located in strictly order
                // from 0 (I) and RhPositive to AB (IV) RhPositive, first 4 pieces in list
                // second part is for RhNegative
                // that's why we execute 2 times this piece of code
                // first time for RhPositive and second time for RhNegative
                final String needsWithRhForAType = bloodNeedsWithRh.remove(0);
                final BloodType bloodType = convertStringToBloodType(bl);
                final BloodRh bloodRh = convertStringToBloodRh(needsWithRhForAType);
                final BloodNeeds bloodNeeds = convertStringToBloodNeeds(needsWithRhForAType);

                blood.setBloodRh(bloodRh);
                blood.setBloodNeeds(bloodNeeds);
                blood.setBloodType(bloodType);

                bloods.add(blood);
            }
        }

        System.out.println(bloods.size());
    }

    static List<String> parsePageForBloodTypes(Elements elements) {
        return elements.stream()
                .map(el -> el.child(0))//only one child should be
                .filter(el -> el.tag() == Tag.valueOf("h1"))
                .map(Element::text)
                .collect(Collectors.toList());

    }

    static List<String> parsePageForBloodNeedsWithRh(Elements elements) {
        return elements.stream()
                .map(el -> el.child(0))//only one child should be
                .filter(el -> el.tag() == Tag.valueOf("img"))
                .map(el -> el.attr("alt"))
                .collect(Collectors.toList());
    }

    static BloodType convertStringToBloodType(String blood) {
        if (blood.contains("0 (I)")) {
            return BloodType.I;
        } else if (blood.contains("A (II)")) {
            return BloodType.II;
        } else if (blood.contains("B (III)")) {
            return BloodType.III;
        } else if (blood.contains("AB (IV)")) {
            return BloodType.IV;
        } else {
            throw new RuntimeException("Can't parse blood type!");
        }
    }

    static BloodRh convertStringToBloodRh(String blood) {
        if (blood.contains("плюс")) {
            return BloodRh.POSITIVE;
        } else if (blood.contains("минус")) {
            return BloodRh.NEGATIVE;
        } else {
            throw new RuntimeException("Can't parse blood Rh!");
        }
    }

    static BloodNeeds convertStringToBloodNeeds(String blood) {
        if (blood.contains("Красный")) {
            return BloodNeeds.RED;
        } else if (blood.contains("Жёлтый")) {
            return BloodNeeds.YELLOW;
        } else if (blood.contains("Зелёный")) {
            return BloodNeeds.GREEN;
        } else {
            throw new RuntimeException("Can't parse blood needs!");
        }
    }

}
