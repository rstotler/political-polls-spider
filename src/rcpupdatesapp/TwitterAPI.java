package rcpupdatesapp;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

class TwitterAPI
{
    Twitter twitter = null;
    
    public void loadAPI()
    {
        try{
            ConfigurationBuilder config = new ConfigurationBuilder();
            config.setDaemonEnabled(true)
                  .setOAuthConsumerKey("H7cagj7XD1JlbjFd2TqUQICpz")
                  .setOAuthConsumerSecret("xTpJIgBKxYTQkZr2pmNHnK2tywrvBbed7wYQJlEGsB3Uf9bkSh")
                  .setOAuthAccessToken("1950308892-eovMfn8mPI1M2UXTQvwL4c7XiChOnS2WkuRES9q")
                  .setOAuthAccessTokenSecret("KO7m1t7pDgDexLQeeJ0MmCKOjgZpjDFL4Cy9qsecO9DGo");
            TwitterFactory tf = new TwitterFactory(config.build());
            twitter = tf.getInstance();
        }
        catch(Exception e){
            System.out.println("TwitterAPI Error: " + e);
        }
    }

    public int getTrumpTweetCount()
    {
        try{
            String statuses = twitter.getUserTimeline("realdonaldtrump").toString();
            if(statuses.contains("statusesCount=")){
                int beginIndex = statuses.indexOf("statusesCount=") + 14;
                int endIndex = statuses.indexOf(",", beginIndex);
                int totalTweets = Integer.parseInt(statuses.substring(beginIndex, endIndex));
                
                return totalTweets;
            }
        }
        catch(Exception e){
            
        }
        
        return -1;
    }
}
