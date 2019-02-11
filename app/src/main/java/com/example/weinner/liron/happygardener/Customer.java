package com.example.weinner.liron.happygardener;

public class Customer{
    private int id;
    private String date;
    private String name;
    private String phone;
    private String address;
    private String time_estimate;
    private String price;
    private String payment;
    private String price_per_hour;

    public Customer(int id, String date, String name, String phone, String address, String time_estimate, String price, String payment, String price_per_hour) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.time_estimate = time_estimate;
        this.price = price;
        this.payment = payment;
        this.price_per_hour = price_per_hour;
    }

    public Customer(){} // Empty c'tor
    // End of constructors


    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime_estimate() {
        return time_estimate;
    }

    public void setTime_estimate(String time_estimate) {
        this.time_estimate = time_estimate;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getPrice_per_hour() {
        return price_per_hour;
    }

    public void setPrice_per_hour(String price_per_hour) {
        this.price_per_hour = price_per_hour;
    }

    // END of Getters and setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;

        Customer customer = (Customer) o;

        if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
        if (phone != null ? !phone.equals(customer.phone) : customer.phone != null) return false;
        if (address != null ? !address.equals(customer.address) : customer.address != null)
            return false;
        if (date != null ? !date.equals(customer.date) : customer.date != null) return false;
        if (time_estimate != null ? !time_estimate.equals(customer.time_estimate) : customer.time_estimate != null)
            return false;
        if (price != null ? !price.equals(customer.price) : customer.price != null) return false;
        if (price_per_hour != null ? !price_per_hour.equals(customer.price_per_hour) : customer.price_per_hour != null)
            return false;
        return payment != null ? payment.equals(customer.payment) : customer.payment == null;
    }

}
