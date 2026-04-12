package com.example.authservice.dto;

public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private Boolean isDefault;

    public AddressResponse(Long id, String street, String city, String state,
                           String pincode, String country, Boolean isDefault) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.isDefault = isDefault;
    }

    public Long getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getCountry() { return country; }
    public Boolean getIsDefault() { return isDefault; }
}
