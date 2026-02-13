package ru.viktorgezz.coretyping.domain.multiplayer.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.FinishMessage;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.ProgressUpdateMessage;
import ru.viktorgezz.coretyping.domain.multiplayer.service.intrf.ContestWebSocketService;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;

@ExtendWith(MockitoExtension.class)
class ContestWebSocketHandlerTest {

    @Mock
    private ContestWebSocketService contestWebSocketService;

    @InjectMocks
    private ContestWebSocketHandler contestWebSocketHandler;

    private User userAuthenticated;
    private UsernamePasswordAuthenticationToken principalValid;

    private static final Long ID_CONTEST_TEST = 1L;
    private static final Long ID_USER_TEST = 100L;
    private static final String USERNAME_TEST = "TestPlayer";
    private static final String PASSWORD_TEST = "password123";

    @BeforeEach
    void setupAuthentication() {
        userAuthenticated = new User(USERNAME_TEST, PASSWORD_TEST, Role.USER, true, false, false);
        userAuthenticated.setId(ID_USER_TEST);

        principalValid = new UsernamePasswordAuthenticationToken(
                userAuthenticated,
                null,
                userAuthenticated.getAuthorities()
        );
    }

    @Test
    @DisplayName("Обработка прогресса делегирует вызов сервису с корректными параметрами")
    void handleProgress_ShouldDelegateToService_WhenValidMessageReceived() {
        int progressPercent = 50;
        int speed = 200;
        BigDecimal accuracy = new BigDecimal("95.50");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, ID_USER_TEST, messageProgress);
    }

    @Test
    @DisplayName("Обработка прогресса извлекает идентификатор пользователя из Principal")
    void handleProgress_ShouldExtractUserIdFromPrincipal_WhenAuthenticatedUser() {
        int progressPercent = 75;
        int speed = 220;
        BigDecimal accuracy = new BigDecimal("96.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, userAuthenticated.getId(), messageProgress);
    }

    @Test
    @DisplayName("Обработка прогресса выбрасывает исключение при отсутствии аутентификации")
    void handleProgress_ShouldThrowException_WhenPrincipalIsNull() {
        int progressPercent = 25;
        int speed = 150;
        BigDecimal accuracy = new BigDecimal("90.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        assertThatThrownBy(() -> contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, null))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("User is not authenticated");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка финиша делегирует вызов сервису с корректными параметрами")
    void handleFinish_ShouldDelegateToService_WhenValidMessageReceived() {
        FinishMessage messageFinish = new FinishMessage(120L, 200, new BigDecimal("95.50"));

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, ID_USER_TEST, messageFinish);
    }

    @Test
    @DisplayName("Обработка финиша извлекает идентификатор пользователя из Principal")
    void handleFinish_ShouldExtractUserIdFromPrincipal_WhenAuthenticatedUser() {
        FinishMessage messageFinish = new FinishMessage(90L, 180, new BigDecimal("98.00"));

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, userAuthenticated.getId(), messageFinish);
    }

    @Test
    @DisplayName("Обработка финиша выбрасывает исключение при отсутствии аутентификации")
    void handleFinish_ShouldThrowException_WhenPrincipalIsNull() {
        FinishMessage messageFinish = new FinishMessage(60L, 150, new BigDecimal("92.00"));

        assertThatThrownBy(() -> contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, null))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("User is not authenticated");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка готовности делегирует вызов сервису с корректными параметрами")
    void handleReady_ShouldDelegateToService_WhenValidPrincipalProvided() {
        contestWebSocketHandler.handleReady(ID_CONTEST_TEST, principalValid);

        verify(contestWebSocketService).processReady(ID_CONTEST_TEST, ID_USER_TEST);
    }

    @Test
    @DisplayName("Обработка готовности извлекает идентификатор пользователя из Principal")
    void handleReady_ShouldExtractUserIdFromPrincipal_WhenAuthenticatedUser() {
        contestWebSocketHandler.handleReady(ID_CONTEST_TEST, principalValid);

        verify(contestWebSocketService).processReady(ID_CONTEST_TEST, userAuthenticated.getId());
    }

    @Test
    @DisplayName("Обработка готовности выбрасывает исключение при отсутствии аутентификации")
    void handleReady_ShouldThrowException_WhenPrincipalIsNull() {
        assertThatThrownBy(() -> contestWebSocketHandler.handleReady(ID_CONTEST_TEST, null))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("User is not authenticated");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка прогресса выбрасывает исключение при некорректном типе Principal")
    void handleProgress_ShouldThrowException_WhenPrincipalHasInvalidType() {
        int progressPercent = 50;
        int speed = 180;
        BigDecimal accuracy = new BigDecimal("92.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);
        UsernamePasswordAuthenticationToken principalInvalid = new UsernamePasswordAuthenticationToken(
                "stringPrincipal",
                null
        );

        assertThatThrownBy(() -> contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalInvalid))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Cannot extract user from principal");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка финиша выбрасывает исключение при некорректном типе Principal")
    void handleFinish_ShouldThrowException_WhenPrincipalHasInvalidType() {
        FinishMessage messageFinish = new FinishMessage(100L, 170, new BigDecimal("88.00"));
        UsernamePasswordAuthenticationToken principalInvalid = new UsernamePasswordAuthenticationToken(
                "stringPrincipal",
                null
        );

        assertThatThrownBy(() -> contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalInvalid))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Cannot extract user from principal");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка готовности выбрасывает исключение при некорректном типе Principal")
    void handleReady_ShouldThrowException_WhenPrincipalHasInvalidType() {
        UsernamePasswordAuthenticationToken principalInvalid = new UsernamePasswordAuthenticationToken(
                "stringPrincipal",
                null
        );

        assertThatThrownBy(() -> contestWebSocketHandler.handleReady(ID_CONTEST_TEST, principalInvalid))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Cannot extract user from principal");

        verifyNoInteractions(contestWebSocketService);
    }

    @Test
    @DisplayName("Обработка прогресса передаёт идентификатор соревнования без изменений")
    void handleProgress_ShouldPassContestIdUnchanged_WhenDelegatingToService() {
        Long idContestSpecific = 999L;
        int progressPercent = 100;
        int speed = 300;
        BigDecimal accuracy = new BigDecimal("100.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(idContestSpecific, messageProgress, principalValid);

        verify(contestWebSocketService).processProgress(idContestSpecific, ID_USER_TEST, messageProgress);
    }

    @Test
    @DisplayName("Обработка финиша передаёт данные о скорости и точности без изменений")
    void handleFinish_ShouldPassSpeedAndAccuracyUnchanged_WhenDelegatingToService() {
        long durationSecondsExpected = 45L;
        int speedExpected = 300;
        BigDecimal accuracyExpected = new BigDecimal("100.00");
        FinishMessage messageFinish = new FinishMessage(durationSecondsExpected, speedExpected, accuracyExpected);

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, ID_USER_TEST, messageFinish);
    }

    @Test
    @DisplayName("Обработка прогресса с нулевым значением делегирует вызов корректно")
    void handleProgress_ShouldDelegateCorrectly_WhenProgressIsZero() {
        int progressPercent = 0;
        int speed = 0;
        BigDecimal accuracy = new BigDecimal("0.00");
        ProgressUpdateMessage messageProgressZero = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgressZero, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, ID_USER_TEST, messageProgressZero);
    }

    @Test
    @DisplayName("Обработка прогресса со значением 100 делегирует вызов корректно")
    void handleProgress_ShouldDelegateCorrectly_WhenProgressIsHundred() {
        int progressPercent = 100;
        int speed = 300;
        BigDecimal accuracy = new BigDecimal("100.00");
        ProgressUpdateMessage messageProgressComplete = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgressComplete, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, ID_USER_TEST, messageProgressComplete);
    }

    @Test
    @DisplayName("Обработка прогресса передаёт все параметры сообщения без изменений")
    void handleProgress_ShouldPassAllMessageParametersUnchanged_WhenDelegatingToService() {
        int progressPercentExpected = 60;
        int speedExpected = 250;
        BigDecimal accuracyExpected = new BigDecimal("97.50");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercentExpected, speedExpected, accuracyExpected);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, ID_USER_TEST, messageProgress);
    }

    @Test
    @DisplayName("Обработка финиша передаёт идентификатор соревнования без изменений")
    void handleFinish_ShouldPassContestIdUnchanged_WhenDelegatingToService() {
        Long idContestSpecific = 888L;
        FinishMessage messageFinish = new FinishMessage(80L, 190, new BigDecimal("94.00"));

        contestWebSocketHandler.handleFinish(idContestSpecific, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(idContestSpecific, ID_USER_TEST, messageFinish);
    }

    @Test
    @DisplayName("Обработка финиша передаёт все параметры сообщения без изменений")
    void handleFinish_ShouldPassAllMessageParametersUnchanged_WhenDelegatingToService() {
        long durationSecondsExpected = 55L;
        int speedExpected = 280;
        BigDecimal accuracyExpected = new BigDecimal("99.50");
        FinishMessage messageFinish = new FinishMessage(durationSecondsExpected, speedExpected, accuracyExpected);

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, ID_USER_TEST, messageFinish);
    }

    @Test
    @DisplayName("Обработка готовности передаёт идентификатор соревнования без изменений")
    void handleReady_ShouldPassContestIdUnchanged_WhenDelegatingToService() {
        Long idContestSpecific = 777L;

        contestWebSocketHandler.handleReady(idContestSpecific, principalValid);

        verify(contestWebSocketService).processReady(idContestSpecific, ID_USER_TEST);
    }

    @Test
    @DisplayName("Обработка прогресса с максимальными значениями делегирует вызов корректно")
    void handleProgress_ShouldDelegateCorrectly_WhenProgressHasMaximumValues() {
        int progressPercent = 100;
        int speed = 500;
        BigDecimal accuracy = new BigDecimal("100.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalValid);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, ID_USER_TEST, messageProgress);
    }

    @Test
    @DisplayName("Обработка финиша с минимальными значениями делегирует вызов корректно")
    void handleFinish_ShouldDelegateCorrectly_WhenFinishHasMinimumValues() {
        long durationSeconds = 1L;
        int speed = 0;
        BigDecimal accuracy = new BigDecimal("0.00");
        FinishMessage messageFinish = new FinishMessage(durationSeconds, speed, accuracy);

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalValid);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, ID_USER_TEST, messageFinish);
    }

    @Test
    @DisplayName("Обработка прогресса с различными пользователями извлекает корректные идентификаторы")
    void handleProgress_ShouldExtractCorrectUserId_WhenDifferentUsersProcessProgress() {
        Long idUserOther = 200L;
        User userOther = new User("OtherPlayer", PASSWORD_TEST, Role.USER, true, false, false);
        userOther.setId(idUserOther);
        UsernamePasswordAuthenticationToken principalOther = new UsernamePasswordAuthenticationToken(
                userOther,
                null,
                userOther.getAuthorities()
        );
        int progressPercent = 40;
        int speed = 180;
        BigDecimal accuracy = new BigDecimal("93.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercent, speed, accuracy);

        contestWebSocketHandler.handleProgress(ID_CONTEST_TEST, messageProgress, principalOther);

        verify(contestWebSocketService).processProgress(ID_CONTEST_TEST, idUserOther, messageProgress);
    }

    @Test
    @DisplayName("Обработка финиша с различными пользователями извлекает корректные идентификаторы")
    void handleFinish_ShouldExtractCorrectUserId_WhenDifferentUsersFinish() {
        Long idUserOther = 300L;
        User userOther = new User("AnotherPlayer", PASSWORD_TEST, Role.USER, true, false, false);
        userOther.setId(idUserOther);
        UsernamePasswordAuthenticationToken principalOther = new UsernamePasswordAuthenticationToken(
                userOther,
                null,
                userOther.getAuthorities()
        );
        FinishMessage messageFinish = new FinishMessage(70L, 210, new BigDecimal("96.00"));

        contestWebSocketHandler.handleFinish(ID_CONTEST_TEST, messageFinish, principalOther);

        verify(contestWebSocketService).processFinish(ID_CONTEST_TEST, idUserOther, messageFinish);
    }

    @Test
    @DisplayName("Обработка готовности с различными пользователями извлекает корректные идентификаторы")
    void handleReady_ShouldExtractCorrectUserId_WhenDifferentUsersBecomeReady() {
        Long idUserOther = 400L;
        User userOther = new User("ReadyPlayer", PASSWORD_TEST, Role.USER, true, false, false);
        userOther.setId(idUserOther);
        UsernamePasswordAuthenticationToken principalOther = new UsernamePasswordAuthenticationToken(
                userOther,
                null,
                userOther.getAuthorities()
        );

        contestWebSocketHandler.handleReady(ID_CONTEST_TEST, principalOther);

        verify(contestWebSocketService).processReady(ID_CONTEST_TEST, idUserOther);
    }
}
