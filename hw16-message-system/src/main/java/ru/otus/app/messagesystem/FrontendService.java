package ru.otus.app.messagesystem;

import ru.otus.messagesystem.client.MessageCallback;

public interface FrontendService {

    void serveRequest(UserRequestAndResponse request, MessageCallback<UserRequestAndResponse> onComplete);
}
