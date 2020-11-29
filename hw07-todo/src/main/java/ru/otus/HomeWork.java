package ru.otus;

import ru.otus.handler.ComplexProcessor;
import ru.otus.listener.ListenerPrinter;
import ru.otus.listener.homework.MessageHistoryHolder;
import ru.otus.processor.Processor;
import ru.otus.processor.homework.Clock;
import ru.otus.processor.homework.FieldSwapper;
import ru.otus.processor.homework.ProcessorThrowingExceptionAtEvenSecond;

import java.util.List;

public class HomeWork {

    /*
     Реализовать to do:
       1. Добавить поля field11 - field13 (для field13 используйте класс ObjectForMessage)
       2. Сделать процессор, который поменяет местами значения field11 и field12
       3. Сделать процессор, который будет выбрасывать исключение в четную секунду (сделайте тест с гарантированным результатом)
       4. Сделать Listener для ведения истории: старое сообщение - новое (подумайте, как сделать, чтобы сообщения не портились)
     */

    public static void main(String[] args) {
        /*
           по аналогии с Demo.class
           из элеменов "to do" создать new ComplexProcessor и обработать сообщение
         */
        final List<Processor> processors = List.of(
                new ProcessorThrowingExceptionAtEvenSecond(new SystemClock()),
                new FieldSwapper()
        );
        final var complexProcessor = new ComplexProcessor(processors, Exception::printStackTrace);
        final var historyListener = new MessageHistoryHolder();
        complexProcessor.addListener(historyListener);
        complexProcessor.addListener(new ListenerPrinter());
        final ObjectForMessage objectForMessage = new ObjectForMessage();
        objectForMessage.setData(List.of("hey", "ay"));
        final var message = new Message.Builder(1)
                .field1("a")
                .field2("b")
                .field3("c")
                .field4("d")
                .field5("e")
                .field6("f")
                .field7("g")
                .field8("h")
                .field9("i")
                .field10("j")
                .field11("k")
                .field12("l")
                .field13(objectForMessage)
                .build();
        complexProcessor.handle(message);
        System.out.println(historyListener.getIdToHistory().get(message.getId()));
    }

    private static class SystemClock implements Clock {

        @Override
        public long currentTimeInMillis() {
            return System.currentTimeMillis();
        }
    }
}
