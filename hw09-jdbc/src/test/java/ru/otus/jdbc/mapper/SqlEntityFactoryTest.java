package ru.otus.jdbc.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.core.annotation.Id;

import java.util.List;
import java.util.Objects;

public class SqlEntityFactoryTest {

    private final SqlEntityFactory sqlEntityFactory = new SqlEntityFactory();
    private final SQLEntity<TestEntity> testEntitySQL = sqlEntityFactory.from(TestEntity.class);

    @Test
    void testConstructionFromRows() {
        final var testEntityActual = testEntitySQL.createFromRow(List.of(1L, "a", 2L));
        final var testEntityExpected = new TestEntity(1L, "a", 2L);
        Assertions.assertEquals(testEntityExpected, testEntityActual);
    }

    @Test
    void testRowFromEntity() {
        final List<Object> actual = testEntitySQL.getRow(new TestEntity(1L, "b", 3L));
        final List<Object> expected = List.of(1L, "b", 3L);
        Assertions.assertIterableEquals(expected, actual);
    }

    private static class TestEntity {
        @Id
        private final long id;
        private final String name;
        private final long companyId;

        TestEntity(final long id, final String name, final long companyId) {
            this.id = id;
            this.name = name;
            this.companyId = companyId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestEntity that = (TestEntity) o;
            return id == that.id && companyId == that.companyId && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, companyId);
        }
    }
}
