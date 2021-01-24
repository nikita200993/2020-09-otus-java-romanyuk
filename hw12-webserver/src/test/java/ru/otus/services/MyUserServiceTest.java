package ru.otus.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.dao.MyUserDao;
import ru.otus.model.MyUser;
import ru.otus.sessionmanager.SessionManager;

@ExtendWith(MockitoExtension.class)
public class MyUserServiceTest {

    @Mock
    private MyUserDao userDao;
    @Mock
    private SessionManager sessionManager;
    @InjectMocks
    private MyUserServiceImpl myUserService;

    @BeforeEach
    void setCommonStubs() {
        Mockito.when(userDao.getSessionManager()).thenReturn(sessionManager);
    }

    @Test
    void testThatRollbacksIfThrows() {
        Mockito.when(userDao.insert(Mockito.any())).thenThrow(RuntimeException.class);
        Assertions.assertThatThrownBy(() -> myUserService.insert(new MyUser("ab", "ab", MyUser.Role.USER)));
        Mockito.verify(sessionManager).rollbackSession();
        Mockito.verify(sessionManager).close();
    }
}
