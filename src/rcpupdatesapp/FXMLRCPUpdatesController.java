package rcpupdatesapp;

import java.io.File;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Window;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

// This program calculates RCP averages and scans for various poll updates.

public class FXMLRCPUpdatesController implements Initializable
{
    // Final Fields
    final static private String     RCP_OA_URL = "http://www.realclearpolitics.com/epolls/other/president_obama_job_approval-1044.html";
    final static private String    RCP_DOC_URL = "http://www.realclearpolitics.com/epolls/other/direction_of_country-902.html";
    final static private String  RASMUSSEN_URL = "http://www.rasmussenreports.com/public_content/politics/obama_administration/obama_approval_index_history";
    final static private String     GALLUP_URL = "http://www.gallup.com/poll/113980/gallup-daily-obama-job-approval.aspx";
    final static private String      IPSOS_URL = "http://www.ipsos-na.com/news-polls/reuters-polls/";
    final static private String     YOUGOV_URL = "http://today.yougov.com/publicopinion/archive/";
    final static private String        PPP_URL = "http://www.publicpolicypolling.com/main/polls/";
    final static private String         AP_URL = "http://ap-gfkpoll.com/poll-archives";
    final static private String QUINNIPIAC_URL = "https://poll.qu.edu/";
    final static private String     MARIST_URL = "http://maristpoll.marist.edu/politics-section/national/";
    final static private String        PEW_URL = "http://www.people-press.org/";
    final static private String         HM_URL = "http://heartlandmonitor.com/category/poll-results/";
    final static private String      ZOGBY_URL = "https://zogbyanalytics.com/news";
    
    // Setting Fields
    final private int  POLLING_TAB_WIDTH = 629,  POLLING_TAB_HEIGHT = 601;
    final private int SETTINGS_TAB_WIDTH = 629, SETTINGS_TAB_HEIGHT = 554;
    final private int RCP_PING_TIME = 15, UPDATE_PING_TIME = 45, PING_RANDOM_SIZE = 15;
            
    // Regular Fields
    Thread mainThread;
    String selectedPoll = "OA";
    private double rcpAverage;
    private int trumpTweetCount = -1;
    TwitterAPI twitterAPI = new TwitterAPI();
    
    // FXML Fields
    @FXML private TabPane mainTabPane;
    @FXML private Label rcpPollNameLabel, rcpAverageLabel, rcpProjectionLabel, rcpPingTimeLabel, trumpTweetsLabel;
    @FXML private TextField userPollName, userPollApproval, userEmailTextField, userMinusTweetsTextField;
    @FXML private CheckBox rcpRangeProjectionsCheckBox, emailUpdatesCheckBox, soundAlertsCheckBox;
    @FXML private TextArea infoTextArea;
    // Main Poll Table
    @FXML private TableView<ListObjectRCPPoll> rcpPollTable;
    @FXML private ObservableList<ListObjectRCPPoll> rcpPollObservableList;
    // Projection Poll Table
    @FXML private TableView<ListObjectRCPPoll> rcpProjectionPollTable;
    @FXML private ObservableList<ListObjectRCPPoll> rcpProjectionPollObservableList;
    // Projection Range Table
    @FXML private TableView<ListObjectRCPProjection> rcpProjectionRangeTable;
    @FXML private ObservableList<ListObjectRCPProjection> rcpProjectionRangeObservableList;
    // Poll Update Table
    @FXML private TableView<ListObjectPollUpdate> pollUpdateTable;
    @FXML private ObservableList<ListObjectPollUpdate> pollUpdateObservableList;
    
    // Init Functions
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        initPollsTab();
        initListeners();
        initUpdatesTab();
        initThread();
        
        infoTextArea.appendText(getTimeStamp() + " RCP Tools Initialized.");
    }
    
    public void initPollsTab()
    {
        // ListObjectRCPPoll Table
        rcpPollTable.getColumns().get(0).setResizable(false);
        rcpPollTable.getColumns().get(1).setResizable(false);
        rcpPollTable.getColumns().get(2).setResizable(false);
        rcpPollTable.getColumns().get(3).setResizable(false);
        
        rcpPollTable.getColumns().get(1).setStyle("-fx-alignment: CENTER;");
        rcpPollTable.getColumns().get(2).setStyle("-fx-alignment: CENTER;");
        rcpPollTable.getColumns().get(3).setStyle("-fx-alignment: CENTER;");
        
        rcpPollTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        rcpPollTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        rcpPollTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("approvalRating"));
        rcpPollTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("dropped"));
        
        rcpPollObservableList = FXCollections.observableArrayList();
        rcpPollTable.setItems(rcpPollObservableList);
        
        // ListObjectRCPProjectionPoll Table
        rcpProjectionPollTable.getColumns().get(0).setResizable(false);
        rcpProjectionPollTable.getColumns().get(1).setResizable(false);
        rcpProjectionPollTable.getColumns().get(2).setResizable(false);
        
        rcpProjectionPollTable.getColumns().get(1).setStyle("-fx-alignment: CENTER;");
        rcpProjectionPollTable.getColumns().get(2).setStyle("-fx-alignment: CENTER;");
        
        rcpProjectionPollTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        rcpProjectionPollTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("approvalRating"));
        rcpProjectionPollTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("dropped"));
        
        rcpProjectionPollObservableList = FXCollections.observableArrayList();
        rcpProjectionPollTable.setItems(rcpProjectionPollObservableList);
        
        // ListObjectRCPProjection Table
        rcpProjectionRangeTable.getColumns().get(0).setResizable(false);
        rcpProjectionRangeTable.getColumns().get(1).setResizable(false);
        
        rcpProjectionRangeTable.getColumns().get(0).setStyle("-fx-alignment: CENTER;");
        rcpProjectionRangeTable.getColumns().get(1).setStyle("-fx-alignment: CENTER;");
        
        rcpProjectionRangeTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("label"));
        rcpProjectionRangeTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("average"));
        
        rcpProjectionRangeObservableList = FXCollections.observableArrayList();
        rcpProjectionRangeTable.setItems(rcpProjectionRangeObservableList);
    }
    
    public void initListeners()
    {
        // Tab Pane
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>(){
            @Override
            public void changed(ObservableValue<? extends Tab> ov, Tab previousTab, Tab selectedTab){
                resizeWindow(selectedTab.getText());
        }});
        
        // Polling Tab
        // Main Poll Table List Selection
        MultipleSelectionModel<ListObjectRCPPoll> pollTableModel = rcpPollTable.getSelectionModel();
        pollTableModel.setSelectionMode(SelectionMode.SINGLE);
        pollTableModel.selectedItemProperty().addListener(new ChangeListener<ListObjectRCPPoll>(){
            @Override
            public void changed(ObservableValue<? extends ListObjectRCPPoll> observable, ListObjectRCPPoll oldValue, ListObjectRCPPoll newValue){
                if(newValue != null){
                    
                    if(!rcpProjectionPollTable.getSelectionModel().isEmpty())
                        rcpProjectionPollTable.getSelectionModel().clearSelection();
                    
                }
            }
        });
        
        // Projection Poll Table List Selection
        MultipleSelectionModel<ListObjectRCPPoll> projectionTableModel = rcpProjectionPollTable.getSelectionModel();
        projectionTableModel.setSelectionMode(SelectionMode.SINGLE);
        projectionTableModel.selectedItemProperty().addListener(new ChangeListener<ListObjectRCPPoll>(){
            @Override
            public void changed(ObservableValue<? extends ListObjectRCPPoll> observable, ListObjectRCPPoll oldValue, ListObjectRCPPoll newValue){
                if(newValue != null){
                    
                    if(!rcpPollTable.getSelectionModel().isEmpty())
                        rcpPollTable.getSelectionModel().clearSelection();
                    
                }
            }
        });
        
        // Projection Range Table List Selection
        MultipleSelectionModel<ListObjectRCPProjection> projectionRangeTableModel = rcpProjectionRangeTable.getSelectionModel();
        projectionRangeTableModel.setSelectionMode(SelectionMode.SINGLE);
        projectionRangeTableModel.selectedItemProperty().addListener(new ChangeListener<ListObjectRCPProjection>(){
            @Override
            public void changed(ObservableValue<? extends ListObjectRCPProjection> observable, ListObjectRCPProjection oldValue, ListObjectRCPProjection newValue){
                if(newValue != null){
                    
                }
            }
        });
    
        // Projection Range Check Box Selection
        rcpRangeProjectionsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                updateProjectionRangeTable();
            }
        });
        
        // Info Text Area Text Added
        infoTextArea.textProperty().addListener(new ChangeListener<Object>(){
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue){
                infoTextArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        
        // Updates Tab
        // User Minus Number Input Changed
        userMinusTweetsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTrumpTweetsLabel();
        });
    }
    
    public void initUpdatesTab()
    {
        // Table Settings
        pollUpdateTable.getColumns().get(0).setResizable(false);
        pollUpdateTable.getColumns().get(1).setResizable(false);
        pollUpdateTable.getColumns().get(2).setResizable(false);
        
        pollUpdateTable.getColumns().get(1).setStyle("-fx-alignment: CENTER;");
        pollUpdateTable.getColumns().get(2).setStyle("-fx-alignment: CENTER;");
        
        pollUpdateTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        pollUpdateTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("enabledPing"));
        pollUpdateTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("secondsToPing"));
        
        pollUpdateObservableList = FXCollections.observableArrayList();
        pollUpdateTable.setItems(pollUpdateObservableList);
    }
    
    public void initThread()
    {
        Task<Void> task = new Task<Void>()
        {    
            // Regular Fields
            private boolean run = true;
            Random randomSeed = new Random();
            private int rcpSeconds = 9999, galSeconds = 9999, rasSeconds = 9999, ygSeconds = 9999, ipsosSeconds = 9999, pppSeconds = 9999, quinSeconds = 9999, maristSeconds = 9999, apSeconds = 9999, pewSeconds = 9999, hmSeconds = 9999, zogbySeconds = 9999, ttSeconds = 9999;
            private int rcpRandom, galRandom, rasRandom, ygRandom, ipsosRandom, pppRandom, quinRandom, maristRandom, apRandom, pewRandom, hmRandom, zogbyRandom;
            
            @Override
            protected Void call() throws Exception
            {
                rcpRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                galRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                rasRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                ygRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                ipsosRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                pppRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                quinRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                maristRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                apRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                pewRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                hmRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                zogbyRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                
                while(run)
                {
                    // RCP Ping
                    if(rcpSeconds >= RCP_PING_TIME + rcpRandom){
                        Platform.runLater(() -> updateRCPPollTable());
                        Platform.runLater(() -> updateProjectionRangeTable());
                        rcpRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        rcpSeconds = 0;
                    }
                    Platform.runLater(() -> rcpPingTimeLabel.setText("RCP Ping: " + ((RCP_PING_TIME + rcpRandom) - rcpSeconds)) );

                    // Rasmussen Ping
                    if(rasSeconds >= UPDATE_PING_TIME + rasRandom){
                        Platform.runLater(() -> getRasmussenData());
                        rasRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        rasSeconds = 0;
                    }
                    
                    // Gallup Ping
                    if(galSeconds >= UPDATE_PING_TIME + galRandom){
                        Platform.runLater(() -> getGallupData());
                        galRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        galSeconds = 0;
                    }
                    
                    // YouGov Ping
                    if(ygSeconds >= UPDATE_PING_TIME + ygRandom){
                        Platform.runLater(() -> getYouGovData());
                        ygRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        ygSeconds = 0;
                    }
                    
                    // Ipsos Ping
                    if(ipsosSeconds >= UPDATE_PING_TIME + ipsosRandom){
                        Platform.runLater(() -> getIpsosData());
                        ipsosRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        ipsosSeconds = 0;
                    }
                    
                    // PPP Ping
                    if(pppSeconds >= UPDATE_PING_TIME + pppRandom){
                        Platform.runLater(() -> getPPPData());
                        pppRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        pppSeconds = 0;
                    }
                    
                    // Quinnipiac Ping
                    if(quinSeconds >= UPDATE_PING_TIME + quinRandom){
                        Platform.runLater(() -> getQuinnipiacData());
                        quinRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        quinSeconds = 0;
                    }
                    
                    // Marist Ping
                    if(maristSeconds >= UPDATE_PING_TIME + maristRandom){
                        Platform.runLater(() -> getMaristData());
                        maristRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        maristSeconds = 0;
                    }
                    
                    // Associated Press Ping
                    if(apSeconds >= UPDATE_PING_TIME + apRandom){
                        Platform.runLater(() -> getAPData());
                        apRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        apSeconds = 0;
                    }
                    
                    // Pew Ping
                    if(pewSeconds >= UPDATE_PING_TIME + pewRandom){
                        Platform.runLater(() -> getPewData());
                        pewRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        pewSeconds = 0;
                    }
                    
                    // Heartland Monitor (The Atlantic) Ping
                    if(hmSeconds >= UPDATE_PING_TIME + hmRandom){
                        Platform.runLater(() -> getHMData());
                        hmRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        hmSeconds = 0;
                    }
                    
                    // Zogby Analytics Ping
                    if(zogbySeconds >= UPDATE_PING_TIME + zogbyRandom){
                        Platform.runLater(() -> getZogbyData());
                        zogbyRandom = randomSeed.nextInt(PING_RANDOM_SIZE);
                        zogbySeconds = 0;
                    }
                    
                    // Trump Tweets
                    if(ttSeconds >= 5){
                        Platform.runLater(() -> getTrumpTweetsData());
                        ttSeconds = 0;
                    }
                    
                    // Tick Seconds
                    rcpSeconds++;
                    if(pollPingEnabledCheck("Gallup")) galSeconds++;
                    if(pollPingEnabledCheck("Rasmussen Reports")) rasSeconds++;
                    if(pollPingEnabledCheck("YouGov")) ygSeconds++;
                    if(pollPingEnabledCheck("Ipsos")) ipsosSeconds++;
                    if(pollPingEnabledCheck("PPP")) pppSeconds++;
                    if(pollPingEnabledCheck("Quinnipiac")) quinSeconds++;
                    if(pollPingEnabledCheck("Marist")) maristSeconds++;
                    if(pollPingEnabledCheck("Associated Press")) apSeconds++;
                    if(pollPingEnabledCheck("Pew")) pewSeconds++;
                    if(pollPingEnabledCheck("Heartland Monitor (The Atlantic)")) hmSeconds++;
                    if(pollPingEnabledCheck("Zogby Analytics")) zogbySeconds++;
                    ttSeconds++;
                    
                    Platform.runLater(() -> tickPollUpdateTimer("Rasmussen Reports", ((UPDATE_PING_TIME + rasRandom) - rasSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Gallup", ((UPDATE_PING_TIME + galRandom) - galSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("YouGov", ((UPDATE_PING_TIME + ygRandom) - ygSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Ipsos", ((UPDATE_PING_TIME + ipsosRandom) - ipsosSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("PPP", ((UPDATE_PING_TIME + pppRandom) - pppSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Quinnipiac", ((UPDATE_PING_TIME + quinRandom) - quinSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Marist", ((UPDATE_PING_TIME + maristRandom) - maristSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Associated Press", ((UPDATE_PING_TIME + apRandom) - apSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Pew", ((UPDATE_PING_TIME + pewRandom) - pewSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Heartland Monitor (The Atlantic)", ((UPDATE_PING_TIME + hmRandom) - hmSeconds)) );
                    Platform.runLater(() -> tickPollUpdateTimer("Zogby Analytics", ((UPDATE_PING_TIME + zogbyRandom) - zogbySeconds)) );
                    
                    Thread.sleep(1000);
                }

                return null;
            }
        };
        
        Thread mainThread = new Thread(task);
        mainThread.setDaemon(true);
        mainThread.start();
    }
    
    public void tickPollUpdateTimer(final String POLL_UPDATE_NAME, final int SECONDS_TO_PING)
    {
        for(ListObjectPollUpdate pollUpdate : pollUpdateObservableList){
            if(pollUpdate.getName().equals(POLL_UPDATE_NAME)){
                pollUpdate.setSecondsToPing(SECONDS_TO_PING);
                pollUpdateObservableList.set(pollUpdateObservableList.indexOf(pollUpdate), pollUpdate);
                break;
            }
        }
    }
    
    // GUI Setting Functions
    public void resizeWindow(final String SELECTED_TAB)
    {   
        Window mainWindow = mainTabPane.getParent().getScene().getWindow();
        
        if(SELECTED_TAB.equals("Polling")){
            infoTextArea.setLayoutY(408);
            mainWindow.setWidth(POLLING_TAB_WIDTH);
            mainWindow.setHeight(POLLING_TAB_HEIGHT);
            mainTabPane.setPrefSize(POLLING_TAB_WIDTH, POLLING_TAB_HEIGHT);
        }

        else if(SELECTED_TAB.equals("Updates")){
            infoTextArea.setLayoutY(361);
            mainWindow.setWidth(SETTINGS_TAB_WIDTH);
            mainWindow.setHeight(SETTINGS_TAB_HEIGHT);
            mainTabPane.setPrefSize(SETTINGS_TAB_WIDTH, SETTINGS_TAB_HEIGHT);
        }
    }
    
    // -Polling- Tab Functions
    public String rcpUpdateCheck(ObservableList<ListObjectRCPPoll> rcpPollObservableList, ObservableList<ListObjectRCPPoll> rcpPollObservableListCopy)
    {
        StringBuilder updateInfo = new StringBuilder();
        
        for(ListObjectRCPPoll updatePoll : rcpPollObservableListCopy)
        {
            boolean updatePollFound = false;
            for(ListObjectRCPPoll originalPoll : rcpPollObservableList)
            {
                // Get Target Poll From Original List -By Name-
                if(originalPoll.getName().equals(updatePoll.getName()))
                {
                    updatePollFound = true;
                    
                    // -RCP Poll Update- If The Poll Date Has Changed
                    if(!originalPoll.getDate().equals(updatePoll.getDate())){
                        updateInfo.append("\nPoll Updated - " + updatePoll.getName() +
                                          " - New Approval: " + updatePoll.getApprovalRating() +
                                          " - Previous Approval: " + originalPoll.getApprovalRating());
                    }
                }
            }
            
            // -RCP Poll Add- If The New Poll Was Not Found In The Original List
            if(!updatePollFound){
                updateInfo.append("\nPoll Added - " + updatePoll.getName() +
                                  " - Approval: " + updatePoll.getApprovalRating());
            }
        }
        
        // -RCP Drop Poll- If An Old Poll Was Not Found In The Updated List
        for(ListObjectRCPPoll originalPoll : rcpPollObservableList)
        {
            boolean originalPollFound = false;
            for(ListObjectRCPPoll updatePoll : rcpPollObservableListCopy){
                if(originalPoll.getName().equals(updatePoll.getName()))
                    originalPollFound = true;
            }
            
            if(!originalPollFound){
                updateInfo.append("\nPoll Dropped - " + originalPoll.getName());
            }
        }
        
        return updateInfo.toString();
    }
    
    void updateRCPPollTable()
    {
        Document htmlData = loadDocument(getSelectedPollURL());

        if(htmlData != null)
        {
            // Load Average Data
            int index = 0;
            double previousAverage = rcpAverage;
            for(Element element : htmlData.getElementById("polling-data-rcp").getElementsByClass("rcpAvg").select("td")){
                if(index == 3)
                    rcpAverage = Double.parseDouble(element.text());
                index++;
            }

            // Load ListObjectRCPPoll Data
            ObservableList<ListObjectRCPPoll> rcpPollObservableListCopy = FXCollections.observableArrayList();
            String tempName = "", tempDate = "", tempAverage = "";
            int subIndex;
            
            index = 0;
            rcpPollObservableListCopy.clear();
            for(Element element : htmlData.getElementById("polling-data-rcp").getElementsByClass("isInRcpAvg")){
                subIndex = 0;
                for(Element subElement : element.getAllElements()){
                    switch(subIndex){
                        case 2:
                            tempName = subElement.text();
                            break;
                        case 4:
                            tempDate = subElement.text();
                            break;
                        case 6:
                            tempAverage = subElement.text();
                            break;
                    }

                    if(subIndex == 6)
                        rcpPollObservableListCopy.add(new ListObjectRCPPoll(tempName, tempDate, tempAverage));
                    subIndex++;
                }
            }

            String updateInfo = rcpUpdateCheck(rcpPollObservableList, rcpPollObservableListCopy);
            
            // Initial Load
            if(rcpPollObservableList.isEmpty()){
                for(ListObjectRCPPoll poll : rcpPollObservableListCopy)
                    rcpPollObservableList.add(poll);
                
                rcpPollNameLabel.setText("Current Poll: " + selectedPoll);
                rcpAverageLabel.setText("Current Average: " + rcpAverage);
            }
            
            // Or Check For Update
            else if(!updateInfo.equals("")){    
                rcpPollObservableList.clear();
                for(ListObjectRCPPoll poll : rcpPollObservableListCopy)
                    rcpPollObservableList.add(poll);
                
                rcpAverageLabel.setText("Current Average: " + rcpAverage);
                updateProjectionRangeTable();
                
                if(!infoTextArea.getText().equals("")) infoTextArea.appendText("\n\n");
                infoTextArea.appendText(getTimeStamp() + "RCP Update!");
                infoTextArea.appendText(updateInfo);
                infoTextArea.appendText("\nNew Average: " + rcpAverage + "\nPrevious Average: " + previousAverage);
                
                // Emailer
                if(emailUpdatesCheckBox.isSelected()){
                    updateInfo = getTimeStamp() + "RCP Update\n" +
                                                  updateInfo +
                                                  "New Average: " + rcpAverage + "\n" +
                                                  "Previous Average: " + previousAverage;
                    if(Emailer.sendMail("RCP Update - New Average: " + rcpAverage, updateInfo, userEmailTextField.getText()))
                        infoTextArea.appendText("\nEmail sent.");
                }
                
                playAlert();
            }
            
        }
    }
    
    public void updateProjectionRangeTable()
    {
        // Clear Table
        boolean initCheck = false;
        if(rcpProjectionRangeObservableList.isEmpty())
            initCheck = true;
        rcpProjectionRangeObservableList.clear();
        
        // Get The Total Average Of All The Polls
        int pollTotalSum = 0, pollCount = 0;
        for(ListObjectRCPPoll tempPoll : rcpPollObservableList){
            if(tempPoll.getDropped().equals(" ") || !rcpRangeProjectionsCheckBox.isSelected()){
                pollTotalSum += tempPoll.getApprovalRatingAsInt();
                pollCount++;
            }
        }
        if(rcpRangeProjectionsCheckBox.isSelected()){
            for(ListObjectRCPPoll tempPoll : rcpProjectionPollObservableList){
                if(tempPoll.getDropped().equals(" ")){
                    pollTotalSum += tempPoll.getApprovalRatingAsInt();
                    pollCount++;
                }
            }
        }
        
        // Calculate Projections Based On The Sum
        String rowLabel;
        double tempProjectedAverage;
        DecimalFormat decimalFormat = new DecimalFormat("00.0");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        for(int i = 15; i >= -15; i--)
        {
            rowLabel = String.valueOf(i);
            if(i > 0) rowLabel = "+" + rowLabel;
            else if(i == 0) rowLabel = "-";
            tempProjectedAverage = (pollTotalSum + i) / (pollCount + 0.0);
            rcpProjectionRangeObservableList.add(new ListObjectRCPProjection(rowLabel, decimalFormat.format(tempProjectedAverage)));
        }
        
        // Set Table Scroll If Initializing
        if(initCheck)
            rcpProjectionRangeTable.scrollTo(15);
        
        // Update Projected Average Label
        updateProjectedAverage();
    }
    
    public void updateProjectedAverage()
    {
        double projectedAverage = 0.0;
        int pollCount = 0;
        
        for(ListObjectRCPPoll rcpPoll : rcpPollTable.getItems()){
            if(rcpPoll.getDropped().equals(" ")){
                projectedAverage += rcpPoll.getApprovalRatingAsInt();
                pollCount++;
            }
        }
        
        for(ListObjectRCPPoll rcpPoll : rcpProjectionPollTable.getItems()){
            if(rcpPoll.getDropped().equals(" ")){
                projectedAverage += rcpPoll.getApprovalRatingAsInt();
                pollCount++;
            }
        }
        
        projectedAverage /= pollCount;
        DecimalFormat decimalFormat = new DecimalFormat("00.0");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        rcpProjectionLabel.setText("Projected Average: " + decimalFormat.format(projectedAverage));
    }
    
    // -Updates- Tab Functions
    public void updateCheckDaily(final String POLL_NAME, final String POLL_DATE, final String POLL_APPROVAL)
    {
        // List Check
        ListObjectPollUpdate targetPollUpdate = null;
        for(ListObjectPollUpdate pollUpdate : pollUpdateObservableList){
            if(pollUpdate.getName().equals(POLL_NAME)){
                targetPollUpdate = pollUpdate;
                break;
            }
        }
        
        ListObjectPollUpdate newPollUpdate = new ListObjectPollUpdate(POLL_NAME, "Not Available", POLL_DATE, POLL_APPROVAL);
        
        // Create Poll If It's Not In The List
        if(targetPollUpdate == null)
            pollUpdateObservableList.add(newPollUpdate);

        // Or If It Already Exists, Compare Data To Check For An Update
        else if(!newPollUpdate.equals(targetPollUpdate))
        {
            newPollUpdate.setEnabledPing( targetPollUpdate.getEnabledPing().equals("*") );

            pollUpdateObservableList.set(pollUpdateObservableList.indexOf(targetPollUpdate), newPollUpdate);
            if(!infoTextArea.getText().equals("")) infoTextArea.appendText("\n\n");
            infoTextArea.appendText(getTimeStamp() + POLL_NAME + " Update" +
                                    "\nNew Approval: " + newPollUpdate.getCurrentApproval() +
                                    " Previous Approval: " + targetPollUpdate.getCurrentApproval());

            // Emailer
            if(emailUpdatesCheckBox.isSelected()){
                String updateInfo = getTimeStamp() + POLL_NAME + " Update" +
                                                     "\nNew Average: " + newPollUpdate.getCurrentApproval() +
                                                     "\nPrevious Average: " + targetPollUpdate.getCurrentApproval();
                if(Emailer.sendMail(POLL_NAME + " Update - New Average: " + newPollUpdate.getCurrentApproval(), updateInfo, userEmailTextField.getText()))
                    infoTextArea.appendText("\nEmail sent.");
            }

            playAlert();
        }

    }
    
    public void updateCheckOther(final String POLL_NAME, final String POLL_LABEL, final String POLL_DATE, final String URL)
    {
        // List Check
        ListObjectPollUpdate targetPollUpdate = null;
        for(ListObjectPollUpdate pollUpdate : pollUpdateObservableList){
            if(pollUpdate.getName().equals(POLL_NAME)){
                targetPollUpdate = pollUpdate;
                break;
            }
        }

        ListObjectPollUpdate newPollUpdate = new ListObjectPollUpdate(POLL_NAME, POLL_LABEL, POLL_DATE, "Not Available");

        // Create Poll If It's Not In The List
        if(targetPollUpdate == null)
            pollUpdateObservableList.add(newPollUpdate);

        // Or If It Already Exists, Compare Data To Check For An Update
        else if(!newPollUpdate.equals(targetPollUpdate))
        {
            newPollUpdate.setEnabledPing( targetPollUpdate.getEnabledPing().equals("*") );

            pollUpdateObservableList.set(pollUpdateObservableList.indexOf(targetPollUpdate), newPollUpdate);
            if(!infoTextArea.getText().equals("")) infoTextArea.appendText("\n\n");
            infoTextArea.appendText(getTimeStamp() + POLL_NAME + " Update" +
                                    "\nNew Poll: " + newPollUpdate.getLabel() +
                                    "\nCheck the website for more data." +
                                    "\n" + URL);

            // Emailer
            if(emailUpdatesCheckBox.isSelected()){
                String updateInfo = getTimeStamp() + POLL_NAME + " Update" +
                                                     "\nCheck the website for more data." +
                                                     "\n" + URL;
                if(Emailer.sendMail(POLL_NAME + " Update", updateInfo, userEmailTextField.getText()))
                    infoTextArea.appendText("\nEmail sent.");
            }

            playAlert();
        }
    }
    
    public void getRasmussenData()
    {
        Document htmlData = loadDocument(RASMUSSEN_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.select("tr").get(1).getAllElements().select("p").get(0).text();
            String approvalPercent = htmlData.select("tr").get(1).getAllElements().select("p").get(4).text().substring(0, 2);
            
            updateCheckDaily("Rasmussen Reports", date, approvalPercent);
        }
    }
    
    public void getGallupData()
    {
        Document htmlData = loadDocument(GALLUP_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementsByClass("figure-table").select("tr").get(1).getAllElements().get(1).text();
            String approvalPercent = htmlData.getElementsByClass("figure-table").select("tr").get(1).getAllElements().get(2).text();
            
            updateCheckDaily("Gallup", date, approvalPercent);
        }
    }
    
    public void getYouGovData()
    {
        Document htmlData = loadDocument(YOUGOV_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String label = htmlData.getElementsByClass("archive-table table").select("tr").get(1).getAllElements().get(1).text();
            String date = htmlData.getElementsByClass("archive-table table").select("tr").get(1).getAllElements().get(10).text();
            
            updateCheckOther("YouGov", label, date, YOUGOV_URL);
        }
    }
    
    public void getIpsosData()
    {
        Document htmlData = loadDocument(IPSOS_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String label = htmlData.getElementsByClass("panel").select("h2").get(0).text();
            String date = htmlData.getElementsByClass("panel").select("p").get(0).text();
            
            updateCheckOther("Ipsos", label, date, IPSOS_URL);
        }
    }
    
    public void getPPPData()
    {
        Document htmlData = loadDocument(PPP_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementsByClass("date-header").select("h2").get(0).text();
            String label = htmlData.getElementsByClass("entry-header").select("h3").get(0).text();
            
            updateCheckOther("PPP", label, date, PPP_URL);
            
        }
    }
    
    public void getAPData()
    {
        Document htmlData = loadDocument(AP_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementsByClass("postdate").get(0).text();
            String label = htmlData.getElementsByClass("featuredtitle").get(0).text();
            
            updateCheckOther("Associated Press", label, date, AP_URL);
        }
    }
    
    public void getQuinnipiacData()
    {
        Document htmlData = loadDocument(QUINNIPIAC_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String label = htmlData.getElementsByClass("mainColumn resizeContent").select("p").get(0).text();
            
            updateCheckOther("Quinnipiac", label, "Not Available", QUINNIPIAC_URL);
        }
    }
    
    public void getMaristData()
    {
        Document htmlData = loadDocument(MARIST_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementsByClass("time").get(0).text();
            String label = htmlData.getElementsByClass("postarea").select("h1").get(0).text();
            
            updateCheckOther("Marist", label, date, MARIST_URL);
        }
    }
    
    public void getPewData()
    {
        Document htmlData = loadDocument(PEW_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementById("homepage-stories-left").getElementsByClass("post").get(0).getElementsByClass("date").text();
            String label = htmlData.getElementById("homepage-stories-left").getElementsByClass("post").get(0).select("h3").text();
            
            updateCheckOther("Pew", label, date, PEW_URL);
        }
    }
    
    public void getHMData()
    {
        Document htmlData = loadDocument(HM_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String date = htmlData.getElementsByClass("detail-column").select("h3").get(0).text();
            String label = htmlData.getElementsByClass("detail-column").select("h1").get(0).text();
            
            updateCheckOther("Heartland Monitor (The Atlantic)", label, date, HM_URL);
        }
    }
    
    public void getZogbyData()
    {
        Document htmlData = loadDocument(ZOGBY_URL);
        
        if(htmlData != null)
        {
            // Scrape Data
            String label = htmlData.getElementsByClass("cat-list-row0").get(0).getElementsByClass("list-title").text();
            String date = htmlData.getElementsByClass("cat-list-row0").get(0).getElementsByClass("list-date").text();
            
            updateCheckOther("Zogby Analytics", label, date, ZOGBY_URL);
        }
    }
    
    public void getTrumpTweetsData()
    {
        // Initialize Check
        if(twitterAPI.twitter == null)
            twitterAPI.loadAPI();
        
        int updateTweetCount = twitterAPI.getTrumpTweetCount();
        if(updateTweetCount != -1)
        {
            // Empty Label Check
            if(trumpTweetsLabel.getText().equals("")){
                trumpTweetCount = updateTweetCount;
                updateTrumpTweetsLabel();
            }

            // Update Check
            else if(trumpTweetCount != updateTweetCount){   
                int labelTweetCount = updateTweetCount;
                if(!userMinusTweetsTextField.getText().equals("")){
                    try{
                        labelTweetCount -= Integer.parseInt(userMinusTweetsTextField.getText());
                    }
                    catch(Exception e){
                        System.out.println("User Input Error: " + e);
                    }
                }

                if(!infoTextArea.getText().equals("")) infoTextArea.appendText("\n\n");
                infoTextArea.appendText(getTimeStamp() + "New Trump Tweets Count: " + labelTweetCount);
                trumpTweetCount = updateTweetCount;
                updateTrumpTweetsLabel();
                
                playTrumpAlert(labelTweetCount);

                // Emailer
                if(emailUpdatesCheckBox.isSelected()){
                    String updateInfo = getTimeStamp() + "Trump Tweets Update" +
                                                         "\nNew Tweet Count: " + labelTweetCount;
                    if(Emailer.sendMail("Trump Tweets Update", updateInfo, userEmailTextField.getText()))
                        infoTextArea.appendText("\nEmail sent.");
                }
            }
        }
        
    }
    
    public void updateTrumpTweetsLabel()
    {
        int labelTweetCount = trumpTweetCount;
        try{
            int userMinusNum = 0;
            if(!userMinusTweetsTextField.getText().equals(""))
                userMinusNum = Integer.parseInt(userMinusTweetsTextField.getText());
            labelTweetCount -= userMinusNum;
            trumpTweetsLabel.setText("Trump Tweets: " + labelTweetCount);
        }
        catch(Exception e){
            System.out.println("User Input Error: " + e);
        }
    }
    
    public boolean pollPingEnabledCheck(String pollName)
    {
        String galPing = "", rasPing = "", ygPing = "", ipsosPing = "", pppPing = "", quinPing = "", maristPing = "", apPing = "", pewPing = "", hmPing = "", zogbyPing = "";
        
        for(ListObjectPollUpdate poll : pollUpdateObservableList){
            if(poll.getName().equals("Gallup")) galPing = poll.getEnabledPing();
            else if(poll.getName().equals("Rasmussen Reports")) rasPing = poll.getEnabledPing();
            else if(poll.getName().equals("YouGov")) ygPing = poll.getEnabledPing();
            else if(poll.getName().equals("Ipsos")) ipsosPing = poll.getEnabledPing();
            else if(poll.getName().equals("PPP")) pppPing = poll.getEnabledPing();
            else if(poll.getName().equals("Quinnipiac")) quinPing = poll.getEnabledPing();
            else if(poll.getName().equals("Marist")) maristPing = poll.getEnabledPing();
            else if(poll.getName().equals("Associated Press")) apPing = poll.getEnabledPing();
            else if(poll.getName().equals("Pew")) pewPing = poll.getEnabledPing();
            else if(poll.getName().equals("Heartland Monitor (The Atlantic)")) hmPing = poll.getEnabledPing();
            else if(poll.getName().equals("Zogby Analytics")) zogbyPing = poll.getEnabledPing();
        }
        
        if(pollName.equals("Gallup")) return galPing.equals("*");
        else if(pollName.equals("Rasmussen Reports")) return rasPing.equals("*");
        else if(pollName.equals("YouGov")) return ygPing.equals("*");
        else if(pollName.equals("Ipsos")) return ipsosPing.equals("*");
        else if(pollName.equals("PPP")) return pppPing.equals("*");
        else if(pollName.equals("Quinnipiac")) return quinPing.equals("*");
        else if(pollName.equals("Marist")) return maristPing.equals("*");
        else if(pollName.equals("Associated Press")) return apPing.equals("*");
        else if(pollName.equals("Pew")) return pewPing.equals("*");
        else if(pollName.equals("Heartland Monitor (The Atlantic)")) return hmPing.equals("*");
        else if(pollName.equals("Zogby Analytics")) return zogbyPing.equals("*");
        
        return false;
    }
    
    // Event Functions
    @FXML private void handleEventLoadNextPollButton(ActionEvent event)
    {
        if(selectedPoll.toLowerCase().equals("oa")) selectedPoll = "DoC";
        else if(selectedPoll.toLowerCase().equals("doc")) selectedPoll = "OA";
        
        rcpPollNameLabel.setText("Current Poll: " + selectedPoll);
        rcpPollObservableList.clear();
        rcpProjectionPollObservableList.clear();
        updateRCPPollTable();
        updateProjectionRangeTable();
        updateProjectedAverage();
    }
    
    @FXML private void handleEventAddButton(ActionEvent event)
    {
        if(!userPollApproval.getText().isEmpty())
        {
            try{
                String pollName = userPollName.getText();
                String pollApproval = userPollApproval.getText();
                rcpProjectionPollObservableList.add(new ListObjectRCPPoll(pollName, "", pollApproval));
                
                userPollName.clear();
                userPollApproval.clear();
                
                updateProjectionRangeTable();
            }
            catch(Exception e){
                System.out.println("User Input Error: " + e);
            }
        }
    }
    
    @FXML private void handleEventDropButton(ActionEvent event)
    {
        if(!rcpPollTable.getSelectionModel().isEmpty() || !rcpProjectionPollTable.getSelectionModel().isEmpty())
        {
            ListObjectRCPPoll targetPoll = null, editedPoll = null;
            ObservableList targetList = null;
            String listTarget = "";
            
            // Get The Data
            if(!rcpPollTable.getSelectionModel().isEmpty())
            {
                targetPoll = rcpPollTable.getSelectionModel().getSelectedItem();
                editedPoll = targetPoll.copy();
                targetList = rcpPollObservableList;
                listTarget = "Main";
            }
                
            else if(!rcpProjectionPollTable.getSelectionModel().isEmpty())
            {
                targetPoll = rcpProjectionPollTable.getSelectionModel().getSelectedItem();
                editedPoll = targetPoll.copy();
                targetList = rcpProjectionPollObservableList;
                listTarget = "Projected";
            }
            
            if(targetPoll.getDropped().equals(" "))
                editedPoll.setDropped(true);
            else
                editedPoll.setDropped(false);
            
            // Update The Poll In The List
            targetList.set(targetList.indexOf(targetPoll), editedPoll);
            if(listTarget.equals("Main"))
                rcpPollTable.getSelectionModel().select(editedPoll);
            else if(listTarget.equals("Projected"))
                rcpProjectionPollTable.getSelectionModel().select(editedPoll);
            
            // Update Projected Average Label
            updateProjectionRangeTable();
        }
    }
    
    @FXML private void handleEventDeleteButton(ActionEvent event)
    {
        if(!rcpProjectionPollTable.getSelectionModel().isEmpty())
        {
            ListObjectRCPPoll selectedPoll = rcpProjectionPollTable.getSelectionModel().getSelectedItem();
            rcpProjectionPollTable.getSelectionModel().clearSelection();
            rcpProjectionPollObservableList.remove(selectedPoll);
            
            updateProjectionRangeTable();
        }
    }
    
    @FXML private void handleEventClearButton(ActionEvent event)
    {
        rcpProjectionPollObservableList.clear();
        updateProjectionRangeTable();
    }
    
    @FXML private void handleEventClearInfoButton(ActionEvent event)
    {
        infoTextArea.clear();
    }
    
    @FXML private void handleEventEnablePollUpdateButton(ActionEvent event)
    {
        if(!pollUpdateTable.getSelectionModel().isEmpty())
        {
            ListObjectPollUpdate pollUpdate = pollUpdateTable.getSelectionModel().getSelectedItem();
            
            if(pollUpdate.getEnabledPing().equals(" "))
                pollUpdate.setEnabledPing(true);
            else
                pollUpdate.setEnabledPing(false);
            
            // Update The Poll In The List
            pollUpdateObservableList.set(pollUpdateObservableList.indexOf(pollUpdate), pollUpdate);
        }
    }
    
    // Other Functions
    public String getTimeStamp()
    {
        Calendar c = Calendar.getInstance();
        return "[" + c.getTime().toString() + "] ";
    }
    
    public String getSelectedPollURL()
    {
        if(selectedPoll.toLowerCase().equals("oa"))
            return RCP_OA_URL;
        else if(selectedPoll.toLowerCase().equals("doc"))
            return RCP_DOC_URL;
        
        return "";
    }
    
    public void playAlert()
    {
        if(soundAlertsCheckBox.isSelected()){
            String musicFile = "Alert.wav";
            Media sound = new Media(new File(musicFile).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        }
    }
    
    public void playTrumpAlert(final int TWEET_COUNT)
    {
        if(soundAlertsCheckBox.isSelected()){
            
            String audioNum = "";
            if(TWEET_COUNT >= 1 && TWEET_COUNT <= 9)
                audioNum = "0" + TWEET_COUNT;
            else if(TWEET_COUNT >= 10 && TWEET_COUNT <= 20)
                audioNum = Integer.toString(TWEET_COUNT);
            
            String audioFilePath = "";
            if(!audioNum.equals(""))
                audioFilePath = audioNum + ".wav";
            else
                audioFilePath = "Alert.wav";
            
            Media sound = new Media(new File(audioFilePath).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        }
    }
    
    Document loadDocument(final String TARGET_URL)
    {
        Document documentSource = null;
        
        try{
            documentSource = Jsoup.connect(TARGET_URL).get();
        }
        catch(Exception e){
            System.out.println("Error Connecting To RCP.");
        }
        
        return documentSource;
    }
}
