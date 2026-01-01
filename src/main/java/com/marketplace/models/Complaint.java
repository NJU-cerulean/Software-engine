package com.marketplace.models;

public class Complaint {
    private String complaintId;
    private String userId;
    private String targetId;
    private Enums.ComplaintType type;
    private Enums.ComplaintStatus status;

    public Complaint(String complaintId, String userId, String targetId, Enums.ComplaintType type) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.targetId = targetId;
        this.type = type;
        this.status = Enums.ComplaintStatus.OPEN;
    }

    public String getComplaintId() { return complaintId; }
    public String getUserId() { return userId; }
    public String getTargetId() { return targetId; }
    public Enums.ComplaintType getType() { return type; }
    public Enums.ComplaintStatus getStatus() { return status; }

    public void submitComplaint() { /* persisted by DAO */ }
    public void updateStatus(Enums.ComplaintStatus s) { this.status = s; }
}
