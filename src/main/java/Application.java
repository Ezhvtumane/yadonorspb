import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://yadonor.ru/reserve/yadonorspb").get();
        final Elements elements = doc.getElementsByClass("spk-lights__item");
        //think about one method to get Blood from page, not map it by pieces
        // find all spk-lights__group-item--min, spk-lights__group-item--middle, spk-lights__group-item--max 
        // and than find out what bloodType is it and Rh is it.
        final List<Element> bloodMin = elements.stream()
                                        .map(el -> el.getElementsByClass("spk-lights__group-item--min"))
                                        .flatMap(els -> els.stream())
                                        .collect(Collectors.toList());
//find out parent and spk-lights__head inside it -> blood Type ->bloodMin.get(0).parent().parent().getElementsByClass("spk-lights__head").text()
//find out text in spk-lights__group-item--$ -> Rh ->  bloodMin.get(0).text()               

        final List<Element> bloodMiddle = elements.stream()
                                        .map(el -> el.getElementsByClass("spk-lights__group-item--middle"))
                                        .flatMap(els -> els.stream())
                                        .collect(Collectors.toList());
        final List<Element> bloodMax = elements.stream()
                                        .map(el -> el.getElementsByClass("spk-lights__group-item--max"))
                                        .flatMap(els -> els.stream())
                                        .collect(Collectors.toList());

        final List<String> bloodGroups = parsePageForBloodTypes(elements);

        if (bloodGroups.size() > 4) {
            throw new RuntimeException("Too much blood types was parsed!");
        }

        final List<String> bloodsRh = parsePageForBloodsRh(elements);

        if (bloodsRh.size() > 8) {
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
                final String needsWithRhForAType = bloodsRh.remove(0);
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
                .map(el -> el.getElementsByClass("spk-lights__head"))
                .flatMap(els -> els.stream())
                .map(el -> el.text())
                .collect(Collectors.toList());
    }

    static List<String> parsePageForBloodsRh(Elements elements) {
        return elements.stream()
                .map(el -> el.getElementsByClass("spk-lights__group"))
                .flatMap(els -> els.stream())
                .map(el -> el.getElementsByClass("spk-lights__group-item"))
                .flatMap(els -> els.stream())
                .map(el -> el.text())
                .collect(Collectors.toList());
                //receive 4 pairs of Rh+ Rh- values
    }

    static List<String> parsePageForBloodNeedsWithRh(Elements elements) {
        return elements.stream().map(el -> el.child(0))// only one child should be
                .filter(el -> el.tag() == Tag.valueOf("img")).map(el -> el.attr("alt")).collect(Collectors.toList());

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
