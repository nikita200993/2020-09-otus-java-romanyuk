package ru.otus.app.messagesystem;

import ru.otus.app.dto.UserDto;
import ru.otus.messagesystem.client.ResultDataType;
import ru.otus.utils.Contracts;

import javax.annotation.Nullable;
import java.util.List;


public class UserRequestAndResponse extends ResultDataType {

    private final RequestType requestType;
    @Nullable
    private final UserDto userToSave;
    @Nullable
    private final List<UserDto> responseForGetAllRequest;
    private final boolean completedSuccessfully;

    private UserRequestAndResponse(
            final RequestType requestType,
            @Nullable final UserDto userToSave,
            @Nullable final List<UserDto> responseForGetAllRequest,
            final boolean completedSuccessfully) {
        this.requestType = requestType;
        this.userToSave = userToSave;
        this.responseForGetAllRequest = responseForGetAllRequest;
        this.completedSuccessfully = completedSuccessfully;
    }

    public static UserRequestAndResponse newCreationRequest(final UserDto userDto) {
        Contracts.requireNonNullArgument(userDto);
        return new UserRequestAndResponse(RequestType.CREATE, userDto, null, false);
    }

    public static UserRequestAndResponse newGetALlUsersRequest() {
        return new UserRequestAndResponse(RequestType.GET_ALL, null, null, false);
    }

    public UserRequestAndResponse withFetchedUsers(final List<UserDto> users) {
        Contracts.requireThat(requestType == RequestType.GET_ALL);
        return new UserRequestAndResponse(requestType, null, List.copyOf(users), true);
    }

    public UserRequestAndResponse withStatus(final boolean isSuccess) {
        Contracts.requireThat(requestType == RequestType.CREATE);
        return new UserRequestAndResponse(requestType, userToSave, null, isSuccess);
    }


    public UserDto getUserToSave() {
        return Contracts.ensureNonNullArgument(userToSave);
    }

    public RequestType getType() {
        return requestType;
    }

    public boolean isCompletedSuccessfully() {
        return completedSuccessfully;
    }

    public List<UserDto> getUsers() {
        return Contracts.ensureNonNullArgument(responseForGetAllRequest);
    }

}
