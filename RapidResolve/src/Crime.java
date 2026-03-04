/**
 * Read-only value object for a crime record.
 */
public class Crime {
    private int crimeId, officerId;
    private String title, description, location, dateReported;

    /**
     * Construct a crime record object.
     */
    public Crime(int crimeId, int officerId, String title, String description, String location, String dateReported) {
        this.crimeId = crimeId;
        this.officerId = officerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateReported = dateReported;
    }

    @Override
    public String toString() {
        return "Crime ID      : " + crimeId +
                "\nOfficer ID   : " + officerId +
                "\nTitle        : " + title +
                "\nDescription  : " + description +
                "\nLocation     : " + location +
                "\nDate Reported: " + dateReported +
                "\n-------------------------------";
    }
}
