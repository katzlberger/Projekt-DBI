package ht.leonding.dbi.workload.customer.address;

import ht.leonding.dbi.workload.customer.Customer;
import jakarta.persistence.*;

@Entity
public class Address {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false, length=120)
    private String street;

    @Column(nullable=false, length=80)
    private String city;

    @Column(nullable=false, length=10)
    private String zip;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Customer customer;

    public Address(String street, String city, String zip) {
        this.street = street;
        this.city = city;
        this.zip = zip;
    }

    public Address() {
    }

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getZip() {
        return zip;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
