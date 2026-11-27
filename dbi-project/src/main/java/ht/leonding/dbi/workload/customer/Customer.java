package ht.leonding.dbi.workload.customer;

import ht.leonding.dbi.workload.customer.address.Address;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length=100)
    private String firstName;

    @Column(nullable=false,length=100)
    private String lastName;

    @Column(nullable=false, unique=true, length=120)
    private String email;

    @OneToMany(mappedBy="customer", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Address> addresses = new ArrayList<>();

    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Customer() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setCustomer(null);
    }
}