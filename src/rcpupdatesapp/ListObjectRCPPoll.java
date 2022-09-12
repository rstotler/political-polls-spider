package rcpupdatesapp;

public class ListObjectRCPPoll
{
    private String name,
                   date,
                   dropped,
                   approvalRating;

    ListObjectRCPPoll(final String NAME, final String DATE, final String APPROVAL_RATING)
    {
        name = NAME;
        date = DATE;
        approvalRating = APPROVAL_RATING;
        dropped = " ";
    }
    
    public String getName(){
        return name;
    }
    
    public String getDate(){
        return date;
    }
    
    public String getApprovalRating(){
        return approvalRating;
    }
    
    public int getApprovalRatingAsInt(){
        return Integer.parseInt(approvalRating);
    }
    
    public String getDropped(){
        return dropped;
    }
    
    public void setDropped(final boolean DROPPED){
        if(DROPPED)
            dropped = "*";
        else
            dropped = " ";
    }

    public ListObjectRCPPoll copy()
    {
        ListObjectRCPPoll pollCopy = new ListObjectRCPPoll(name, date, approvalRating);
        
        if(dropped.equals("*"))
            pollCopy.setDropped(true);
        
        return pollCopy;
    }
    
    public boolean equals(final ListObjectRCPPoll COPY)
    {
        return name.equals(COPY.name) &&
               date.equals(COPY.date) &&
               approvalRating.equals(COPY.approvalRating);
    }
}
