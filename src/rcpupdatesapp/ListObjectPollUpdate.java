package rcpupdatesapp;

public class ListObjectPollUpdate
{
    String name,
           label,
           date,
           enabledPing,
           enabledEmail,
           currentApproval;
    int secondsToPing;

    public ListObjectPollUpdate(final String NAME, final String LABEL, final String DATE, final String CURRENT_APPROVAL) {
        name = NAME;
        label = LABEL;
        date = DATE;
        enabledPing = " ";
        currentApproval = CURRENT_APPROVAL;
        secondsToPing = -1;
    }

    public String getName(){
        return name;
    }

    public void setName(final String NAME){
        name = NAME;
    }

    public String getLabel(){
        return label;
    }
    
    public void setLabel(final String LABEL){
        label = LABEL;
    }
    
    public String getDate(){
        return date;
    }
    
    public void setDate(final String DATE){
        date = DATE;
    }
    
    public String getEnabledPing(){
        return enabledPing;
    }

    public void setEnabledPing(final boolean ENABLED){
        if(ENABLED)
            enabledPing = "*";
        else
            enabledPing = " ";
    }
    
    public String getCurrentApproval(){
        return currentApproval;
    }

    public void setCurrentApproval(final String CURRENT_APPROVAL){
        currentApproval = CURRENT_APPROVAL;
    }
    
    public int getSecondsToPing() {
        return secondsToPing;
    }

    public void setSecondsToPing(final int SECONDS_TO_PING) {
        secondsToPing = SECONDS_TO_PING;
    }
    
    public void tick()
    {
        secondsToPing--;
    }
    
    public boolean equals(final ListObjectPollUpdate COPY)
    {
        return name.equals(COPY.name) &&
               label.equals(COPY.label) &&
               date.equals(COPY.date) &&
               currentApproval.equals(COPY.currentApproval);
    }
}
