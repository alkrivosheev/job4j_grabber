package ru.job4j.grabber;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    private void createTableIfNotExists(Connection connection) throws SQLException {
        String createTableSQL = String.format(
                "CREATE TABLE IF NOT EXISTS post ("
                        + " id serial primary key,"
                        + " name varchar(100),"
                        + " text text,"
                        + " link text,"
                        + " created timestamp,"
                        + " CONSTRAINT link_unique UNIQUE (link)"
                        + ");");
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        }
    }

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
            createTableIfNotExists(connection);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT INTO post (id, name, text, link, created)"
                + " VALUES (?, ?, ?, ?, ?)"
                + " ON CONFLICT(link)"
                + " DO UPDATE SET"
                + " text = EXCLUDED.text,"
                + " created = EXCLUDED.created,"
                + " name = EXCLUDED.name;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, post.getId());
            preparedStatement.setString(2, post.getTitle());
            preparedStatement.setString(3, post.getDescription());
            preparedStatement.setString(4, post.getLink());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Post> getAll() {
        List<Post> res = new ArrayList<>();
        String sql = "SELECT id, name, text, link, created FROM post";
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                Post post = new Post(rs.getInt("id"));
                post.setTitle(rs.getString("name"));
                post.setDescription(rs.getString("text"));
                post.setLink(rs.getString("link"));
                post.setCreated(rs.getTimestamp("created").toLocalDateTime());
                res.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public Post findById(int id) {
        Post res = null;
        String sql = "SELECT id, name, text, link, created FROM post WHERE id = ?";
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setInt(1, id);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                res = new Post(rs.getInt("id"));
                res.setTitle(rs.getString("name"));
                res.setDescription(rs.getString("text"));
                res.setLink(rs.getString("link"));
                res.setCreated(rs.getTimestamp("created").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(input);
            PsqlStore ps = new PsqlStore(config);
            Post post = new Post(0);
            post.setLink("link1");
            post.setTitle("title1");
            post.setDescription("description1");
            LocalDateTime ldt = LocalDateTime.now();
            post.setCreated(ldt);
            ps.save(post);

            Post otherPost = new Post(0);
            otherPost.setLink("link1");
            otherPost.setTitle("title2");
            otherPost.setDescription("description2");
            ldt = LocalDateTime.now();
            otherPost.setCreated(ldt);
            ps.save(otherPost);

            System.out.println(ps.getAll());

            Post pst = ps.findById(0);
            System.out.println("Post_id = " + pst.getId());
            System.out.println("Post_name = " + pst.getTitle());
            System.out.println("Post_text = " + pst.getDescription());
            System.out.println("Post_link = " + pst.getLink());
            System.out.println("Post_date = " + pst.getCreated());

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}