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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "phones")
public class PhoneDataSet {

    @Id
    @SequenceGenerator(name = "phoneId", sequenceName = "phone_id")
    @GeneratedValue(generator = "phoneId", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "phone_number")
    private String phoneNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public PhoneDataSet() {
    }

    public PhoneDataSet(final String phoneNumber) {
        Contracts.requireNonNullArgument(phoneNumber);

        this.phoneNumber = phoneNumber;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        Contracts.requireNonNullArgument(phoneNumber);

        this.phoneNumber = phoneNumber;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        Contracts.requireNonNullArgument(client);

        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneDataSet that = (PhoneDataSet) o;
        return phoneNumber.equals(that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}
