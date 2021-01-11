import java.sql.*;
import com.vdurmont.emoji.*;
import twitter4j.*;

import java.util.Date;

public class bdTwitter {
    public static void main(String[] args) throws Exception {

        System.out.println("Connecting to database...");
        try (Connection connection = DriverManager.getConnection(sqlConn.url, sqlConn.username, sqlConn.password)) {
            System.out.println("Successfully connected to database!");

            Twitter twitter = TwitterFactory.getSingleton();
            Query query = new Query("#panathinaikos"); // Search input - where it will retrieve the tweets from.
            query.setCount(50); // Amount of results to return per page.
            QueryResult result = twitter.search(query);

            Statement st = connection.createStatement();

            int counter = 0;
            for (Status status : result.getTweets()) {
                long TweetID = status.getId();
                String displayName = status.getUser().getName().toString();
                String userName = status.getUser().getScreenName();
                String content = status.getText();
                Date createdAt = status.getCreatedAt();

                content = content.replace("'", "\\"); // In case a tweet contains -> ' <-, which breaks the SQL query. Removes it completely.
                displayName = displayName.replace("'", "\\"); // In case the display name of the tweet author contains -> ' <-, which breaks the SQL query. Removes it completely.
                String contentNoEmojis = EmojiParser.removeAllEmojis(content); // Removes all emojis from the content of a tweet.
                String displayNameNoEmojis = EmojiParser.removeAllEmojis(displayName); // Removes all emojis from the display name of the author of the tweet.

                String insertQuery = "INSERT INTO Tweets (TweetID, DisplayName, userName, Content, CreatedAt) VALUES ('" + TweetID + "', '" + displayNameNoEmojis + "', '" + userName + "', '" + contentNoEmojis + "', '" + createdAt + "')";

                try {
                    st.executeUpdate(insertQuery);
                    System.out.println("A new entry has been successfully inserted!");
                    counter++;
                } catch (SQLException e) {
                    System.out.println("Something went wrong! (" + TweetID + " by " + displayName + ":" + userName + ") " + e);
                }

//                if (result.hasNext()) {
//                    query = result.nextQuery();
//                    result = twitter.search(query);
//                }
            }

            System.out.printf("Inserted %d results to the database.", counter);

        } catch (SQLException e) {
            throw new IllegalStateException("Something went wrong! -> ", e);
        }
    }
}
