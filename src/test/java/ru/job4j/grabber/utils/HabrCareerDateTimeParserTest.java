package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerDateTimeParserTest {

    @Test
    public void whenDateParse() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThat(parser.parse("2024-08-01T18:27:14+03:00").toString()).isEqualTo("2024-08-01T18:27:14");
    }

    @Test
    public void whenDateParseWithDifferentTimezone() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThat(parser.parse("2024-08-01T18:27:14-05:00").toString()).isEqualTo("2024-08-01T18:27:14");
    }

}