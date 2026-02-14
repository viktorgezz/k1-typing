package ru.viktorgezz.coretyping.domain.multiplayer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.FinishMessage;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.ProgressUpdateMessage;
import ru.viktorgezz.coretyping.domain.multiplayer.service.intrf.ContestWebSocketService;

import java.security.Principal;

import static ru.viktorgezz.coretyping.security.util.WebSocketUserUtils.getUserFromPrincipal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ContestWebSocketHandler {

    private final ContestWebSocketService contestWebSocketService;

    @MessageMapping("/contest/{idContest}/progress")
    public void handleProgress(
            @DestinationVariable Long idContest,
            @Payload ProgressUpdateMessage message,
            Principal principal
    ) {
        Long idUser = getUserFromPrincipal(principal).getId();

        contestWebSocketService.processProgress(idContest, idUser, message);
    }

    @MessageMapping("/contest/{idContest}/finish")
    public void handleFinish(
            @DestinationVariable Long idContest,
            @Payload FinishMessage message,
            Principal principal
    ) {
        Long idUser = getUserFromPrincipal(principal).getId();
        log.debug("Finish: contest={}, user={}, speed={}, accuracy={}",
                idContest, idUser, message.speed(), message.accuracy());

        contestWebSocketService.processFinish(idContest, idUser, message);
    }

    @MessageMapping("/contest/{idContest}/ready")
    public void handleReady(
            @DestinationVariable Long idContest,
            Principal principal
    ) {
        Long idUser = getUserFromPrincipal(principal).getId();
        log.debug("Player ready: contest={}, user={}", idContest, idUser);

        contestWebSocketService.processReady(idContest, idUser);
    }
}
