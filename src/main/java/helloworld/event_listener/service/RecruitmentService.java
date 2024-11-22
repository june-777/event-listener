package helloworld.event_listener.service;

import java.util.NoSuchElementException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import helloworld.event_listener.domain.Member;
import helloworld.event_listener.domain.Recruitment;
import helloworld.event_listener.event.RecruitmentEvent;
import helloworld.event_listener.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecruitmentService {

	private final RecruitmentRepository recruitmentRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	public Recruitment create(Member member, String recruitmentTitle) {
		Recruitment recruitment = new Recruitment(member, recruitmentTitle);
		recruitmentRepository.save(recruitment);

		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[RecruitmentService] before publish event txActive - {}", txActive);

		applicationEventPublisher.publishEvent(new RecruitmentEvent(recruitment.getId()));

		txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[RecruitmentService] after publish event txActive - {}", txActive);

		return recruitment;
	}

	public Recruitment createWithExceptionHandle(Member member, String recruitmentTitle) {
		Recruitment recruitment = new Recruitment(member, recruitmentTitle);
		recruitmentRepository.save(recruitment);

		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[RecruitmentService] before publish event txActive - {}", txActive);

		try {
			applicationEventPublisher.publishEvent(new RecruitmentEvent(recruitment.getId()));
		} catch (IllegalArgumentException | NoSuchElementException e) {
			log.info("[RecruitmentService] exception occurred - {}", e.getMessage());
		}

		txActive = TransactionSynchronizationManager.isActualTransactionActive();
		log.info("[RecruitmentService] after publish event txActive - {}", txActive);

		return recruitment;
	}

}
