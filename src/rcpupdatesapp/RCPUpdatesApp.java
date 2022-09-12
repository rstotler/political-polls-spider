package rcpupdatesapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/* 
   This program monitors RealClearPolitics.com and several other polling websites
   to provide the user with real-time poll updates.
   
   Presidential approval rating data is scraped from RealClearPolitics.com and
   displayed using using dynamic tables in JavaFX. An average approval rating is
   calculated as well as tools to add theoretical polls and drop existing ones
   in order to create new projected averages. If desired the user can also be
   notified by email when a poll is updated.
*/

public class RCPUpdatesApp extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLRCPUpdates.fxml"));
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("RCP Tools");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
    
}
