package ru.otus.core.model;

import ru.otus.utils.Contracts;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "addresses")
public class AddressDataSet {

    @Nullable
    @Id
    @SequenceGenerator(name = "addressId", sequenceName = "address_id")
    @GeneratedValue(generator = "addressId", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "street")
    private String street;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    public AddressDataSet() {

    }

    public AddressDataSet(final String street) {
        Contracts.requireNonNullArgument(street);

        this.street = street;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        Contracts.requireNonNullArgument(id);

        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public AddressDataSet setStreet(final String street) {
        Contracts.requireNonNullArgument(street);

        this.street = street;
        return this;
    }

    public AddressDataSet setClient(final Client client) {
        Contracts.requireNonNullArgument(client);

        this.client = client;
        return this;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "AddressDataSet{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", client=" + client +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressDataSet that = (AddressDataSet) o;
        return Objects.equals(street, that.street);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street);
    }
}
