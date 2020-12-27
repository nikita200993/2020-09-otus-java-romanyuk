package ru.otus.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @SequenceGenerator(name = "accountId", sequenceName = "account_id")
    @GeneratedValue(generator = "accountId", strategy = GenerationType.SEQUENCE)
    private long id;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }
}
