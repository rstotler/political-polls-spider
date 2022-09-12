package rcpupdatesapp;

public class ListObjectRCPProjection
{
    private String label,
                   average;

    public ListObjectRCPProjection(final String LABEL, final String AVERAGE)
    {
        label = LABEL;
        average = AVERAGE;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String LABEL) {
        label = LABEL;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(final String AVERAGE) {
        average = AVERAGE;
    }
}
