package ru.otus.config;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.services.*;


import java.util.Scanner;

@AppComponentsContainerConfig(order = 1)
public class AppConfigOne {

    @AppComponent(order = 1, name = "playerService")
    public PlayerService playerService(IOService ioService) {
        return new PlayerServiceImpl(ioService);
    }

    @AppComponent(order = 0, name = "ioService")
    public IOService ioService() {
        return new IOServiceConsole(System.out, System.in);
    }
}
