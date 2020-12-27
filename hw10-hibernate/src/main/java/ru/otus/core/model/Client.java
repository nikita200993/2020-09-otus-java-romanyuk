package ru.otus.core.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @SequenceGenerator(name = "clientId", sequenceName = "client_id")
    @GeneratedValue(generator = "clientId", strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(unique = true)
    private String login;

    public Client() {
    }

    public Client(long id, String login) {
        this.id = id;
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + login + '\'' +
                '}';
    }
}
