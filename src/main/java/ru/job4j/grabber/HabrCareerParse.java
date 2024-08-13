package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import java.io.IOException;

public class HabrCareerParse {
    private static final Integer PAGES_TO_PARSE = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all  ";

    private static String retrieveDescription(String link) {
        String res = "";
        String fullLink = "%s%s".formatted(SOURCE_LINK, link);
        Connection connection = Jsoup.connect(fullLink);
        try {
            Document document = connection.get();
            Elements rows = document.select(".vacancy-description__text");
            res = rows.text();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        for (int pageNumber = 1; pageNumber <= PAGES_TO_PARSE; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTimeElement = dateElement.child(0);
                String dateStr = dateTimeElement.attr("datetime");
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyText = retrieveDescription(linkElement.attr("href"));
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", parser.parse(dateStr).toString(), vacancyName, link);
            });
        }
    }
}