package ru.otus.core.model;


import ru.otus.utils.Contracts;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "client")
public class Client {

    @Id
    // initial value is set to 1000, because Long has default cache from -128 to 127
    @SequenceGenerator(name = "clientId", sequenceName = "client_id", initialValue = 1000)
    @GeneratedValue(generator = "clientId", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "name")
    private String name;
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private AddressDataSet address;
    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PhoneDataSet> phones = new HashSet<>();

    public Client() {
    }

    public Client(final String name) {
        Contracts.requireNonNullArgument(name);

        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAddress(final AddressDataSet address) {
        Contracts.requireNonNullArgument(address);

        address.setClient(this);
        this.address = address;
    }

    public void removePhone(final PhoneDataSet phone) {
        Contracts.requireNonNullArgument(phone);
        Contracts.requireNonNull(phones);

        phone.setClient(null);
        phones.remove(phone);
    }

    public void addPhone(final PhoneDataSet phone) {
        Contracts.requireNonNullArgument(phone);
        Contracts.requireNonNull(phones);

        phone.setClient(this);
        phones.add(phone);
    }

    public Set<PhoneDataSet> getPhones() {
        return Collections.unmodifiableSet(phones);
    }

    public void setPhones(final Set<PhoneDataSet> phones) {
        Contracts.requireNonNullArgument(phones);
        this.phones = new HashSet<>(phones);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public AddressDataSet getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id) && Objects.equals(name, client.name) && Objects.equals(address, client.address) && Objects.equals(phones, client.phones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, phones);
    }
}
