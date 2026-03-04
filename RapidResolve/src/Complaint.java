/**
 * Immutable view model representing a complaint record fetched from the database.
 * Used for printing and in-memory sorting (BST) during dashboards.
 */
public class Complaint {
    private int complaintId;
    private int userId;
    private String area;
    private String type;
    private String description;
    private String status;
    private int officerId;
    private String filedOn;

    /**
     * Construct a complaint data object.
     */
    public Complaint(int complaintId, int userId, String area, String type, String description, String status, int officerId, String filedOn) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.area = area;
        this.type = type;
        this.description = description;
        this.status = status;
        this.officerId = officerId;
        this.filedOn = filedOn;
    }

    /**
     * @return complaint primary key identifier
     */
    public int getComplaintId()
    {
        return complaintId;
    }

    @Override
    public String toString() {
        return "Complaint ID: " + complaintId +
                "\nArea      : " + area +
                "\nType      : " + type +
                "\nDescription: " + description +
                "\nStatus    : " + status +
                "\nOfficer ID: " + officerId +
                "\nFiled On  : " + filedOn +
                "\n-----------------------------";
    }

    /**
     * Print a concise, readable one-complaint card to console.
     */

}
