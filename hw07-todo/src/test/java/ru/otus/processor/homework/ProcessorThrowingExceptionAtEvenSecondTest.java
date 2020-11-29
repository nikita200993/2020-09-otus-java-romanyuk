package ru.otus.processor.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.Message;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProcessorThrowingExceptionAtEvenSecondTest {

    @Mock
    private Clock clock;
    @Mock
    private Message message;

    @BeforeEach
    void stub() {
        Mockito.when(clock.currentTimeInSeconds()).thenReturn(2L);
    }

    @Test
    void testProcess() {
        final var processor = new ProcessorThrowingExceptionAtEvenSecond(clock);
        Assertions.assertThrows(RuntimeException.class, () -> processor.process(message));
    }
}