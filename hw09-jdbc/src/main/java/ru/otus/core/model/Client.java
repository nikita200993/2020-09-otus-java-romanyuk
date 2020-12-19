package ru.otus.core.model;

import ru.otus.core.annotation.Id;
import ru.otus.utils.Contracts;

import java.util.Objects;

/**
 * @author sergey
 * created on 03.02.19.
 */
public class Client {
    @Id
    private final long id;
    private final String name;
    private final int age;

    public Client(final long id, final String name, final int age) {
        this.id = id;
        this.name = Contracts.ensureNonNullArgument(name);
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id && age == client.age && name.equals(client.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
