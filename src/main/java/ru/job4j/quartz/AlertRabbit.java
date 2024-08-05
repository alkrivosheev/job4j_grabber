package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) {
        Properties config = readProperty();
        try (Connection store = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        )) {
            Class.forName(config.getProperty("driver-class-name"));
            createTableIfNotExists(store);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(Long.parseLong(config.getProperty("rabbit.interval")));
            scheduler.shutdown();
            System.out.println(store);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static void createTableIfNotExists(Connection store) throws SQLException {
        String createTableSQL = String.format(
                "CREATE TABLE IF NOT EXISTS rabbit(id serial primary key, created_date timestamp);");
        try (Statement statement = store.createStatement()) {
            statement.execute(createTableSQL);
        }
    }

    private static void addToTable(Connection store) throws SQLException {
        Timestamp timestampFromLDT = Timestamp.valueOf(LocalDateTime.now().withNano(0));
        String sql = "INSERT INTO rabbit(created_date) VALUES (?)";
        try (PreparedStatement preparedStatement = store.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, timestampFromLDT);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readLastFromTable(Connection store) throws SQLException {
        String res = "";
        String sql = "SELECT created_date FROM rabbit ORDER BY ID DESC LIMIT 1";
        try (PreparedStatement preparedStatement = store.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                res = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection store = (Connection) context.getJobDetail().getJobDataMap().get("store");
            try {
                addToTable(store);
                System.out.println(readLastFromTable(store));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Properties readProperty() {
        Properties property = null;
        try (InputStream input = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            property = new Properties();
            property.load(input);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return property;
    }
}