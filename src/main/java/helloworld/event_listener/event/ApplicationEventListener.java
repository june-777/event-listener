package helloworld.event_listener.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import helloworld.event_listener.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationEventListener {
	private final NotificationService notificationService;

	// @EventListener
	@TransactionalEventListener
	@Async
	public void onApplicationEvent(RecruitmentEvent event) {
		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[ApplicationEventListener] event! {} received, txActive - {}", event, txActive);

		// notificationService.notify(event);	// REQUIRES_NEW 테스트하려면 주석처리
		notificationService.notifyWithNewTx(event);	// REQUIRED 테스트하려면 주석처리

		log.info("[ApplicationEventListener] event finished, txActive - {}", txActive);
	}

}
