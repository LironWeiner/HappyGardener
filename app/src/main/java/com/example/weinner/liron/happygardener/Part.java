package com.example.weinner.liron.happygardener;

/**
 * Created by Liron weinner
 */

public class Part{
    private int id , part_id;
    private String name , price , quantity;
    private Category category;


    // Constructors
    public Part(int id, int part_id, String name, String price, String quantity, Category category) {
        this.id = id;
        this.part_id = part_id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Part(int part_id, String name, String price) {
        this.part_id = part_id;
        this.name = name;
        this.price = price;
    }

    public Part(int part_id , String quantity)
    {
        this.part_id = part_id ;
        this.quantity = quantity ;
    }

    public Part(int id, int part_id, String name, String price, String quantity){
        this.id = id;
        this.part_id = part_id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Part(){} // Empty c'tor

    public Part(Part part, int part_id){
        this.id = part.id;
        this.name = part.name;
        this.category = part.category;
        this.part_id = part_id;
    }

    public Part(final Part part){
        this.part_id = part.part_id;
        this.name = part.name;
        this.price = part.price;
        this.quantity = part.getQuantity();
        this.category = part.getCategory();
    }
    // End of constructors

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPart_id() {
        return part_id;
    }

    public void setPart_id(int part_id) {
        this.part_id = part_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    // END of Getters and setters

    // This method is for checking if user changed parts info
    public boolean isTheSame(Object o) {
        if (this == o) return true;
        if (!(o instanceof Part)) return false;

        Part part = (Part) o;

        if (name != null ? !name.equals(part.name) : part.name != null) return false;
        if (price != null ? !price.equals(part.price) : part.price != null) return false;
        return quantity != null ? quantity.equals(part.quantity) : part.quantity == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Part part = (Part) o;

        return part_id == part.part_id;
    }

}
