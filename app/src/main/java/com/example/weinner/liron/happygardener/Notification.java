package com.example.weinner.liron.happygardener;

/**
 * Created by Liron weinner on 22/12/2017.
 */

public class Notification {
    private int id;
    private int customer_id;
    private String customer_name , notification_date , repeat;

    // Constructors

    public Notification(int customer_id, String notification_date, String repeat) {
        this.customer_id = customer_id;
        this.notification_date = notification_date;
        this.repeat = repeat;
    }

    public Notification(int customer_id, String notification_date, String repeat , String customer_name) {
        this.customer_id = customer_id;
        this.customer_name = customer_name;
        this.notification_date = notification_date;
        this.repeat = repeat;
    }


    // Empty c'tor
    public Notification(){}

    // End of constructors


    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getNotification_date() {
        return notification_date;
    }

    public void setNotification_date(String notification_date) {
        this.notification_date = notification_date;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    // End of getters and setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;

        Notification that = (Notification) o;

        if (notification_date != null ? !notification_date.equals(that.notification_date) : that.notification_date != null)
            return false;
        return repeat != null ? repeat.equals(that.repeat) : that.repeat == null;
    }


}
