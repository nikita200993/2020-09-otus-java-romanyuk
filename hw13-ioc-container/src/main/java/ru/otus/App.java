package ru.otus;

import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.implementation.AppComponentsContainerImpl;
import ru.otus.config.AppConfigOne;
import ru.otus.config.AppConfigTwo;
import ru.otus.services.GameProcessor;

/*
В классе AppComponentsContainerImpl реализовать обработку, полученной в конструкторе конфигурации,
основываясь на разметке аннотациями из пакета appcontainer. Так же необходимо реализовать методы getAppComponent.
В итоге должно получиться работающее приложение. Менять можно только класс AppComponentsContainerImpl.

PS Приложение представляет из себя тренажер таблицы умножения)
*/

public class App {

    public static void main(String[] args) {
        // Опциональные варианты
        AppComponentsContainer container = AppComponentsContainerImpl.create(AppConfigOne.class, AppConfigTwo.class);

//      Тут можно использовать библиотеку Reflections (см. зависимости)
//      AppComponentsContainer container = new AppComponentsContainerImpl("ru.otus.config");

//      Приложение должно работать в каждом из указанных ниже вариантов
        GameProcessor gameProcessor = container.getAppComponent(GameProcessor.class);
//      GameProcessor gameProcessor = container.getAppComponent(GameProcessorImpl.class);
//      GameProcessor gameProcessor = container.getAppComponent("gameProcessor");

        gameProcessor.startGame();
    }
}
