package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements  Parse {
    private static final Integer PAGES_TO_PARSE = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";
    private final DateTimeParser dateTimeParser;
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> res = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGES_TO_PARSE; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = null;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTimeElement = dateElement.child(0);
                String dateStr = dateTimeElement.attr("datetime");
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyText = retrieveDescription(linkElement.attr("href"));
                String vacancyName = titleElement.text();
                String descriptionLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                Post post = new Post();
                post.setTitle(vacancyName);
                post.setLink(descriptionLink);
                post.setDescription(vacancyText);
                post.setCreated(dateTimeParser.parse(dateStr));
                res.add(post);
            });
        }
        return res;
    }

    private  String retrieveDescription(String link) {
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
}