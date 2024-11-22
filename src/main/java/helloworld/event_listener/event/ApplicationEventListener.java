package helloworld.event_listener.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import helloworld.event_listener.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationEventListener {
	private final NotificationService notificationService;

	@EventListener
	public void onApplicationEvent(RecruitmentEvent event) {
		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[ApplicationEventListener] event! {} received, txActive - {}", event, txActive);

		notificationService.notify(event);

		log.info("[ApplicationEventListener] event finished, txActive - {}", txActive);
	}

}
