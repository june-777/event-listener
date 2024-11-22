package helloworld.event_listener.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import helloworld.event_listener.domain.Notification;
import helloworld.event_listener.domain.Recruitment;
import helloworld.event_listener.event.RecruitmentEvent;
import helloworld.event_listener.repository.NotificationRepository;
import helloworld.event_listener.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final RecruitmentRepository recruitmentRepository;

	public Notification notify(RecruitmentEvent event) {
		Recruitment recruitment = findRecruitment(event.getId());

		valid(recruitment);

		Notification notification = new Notification(recruitment.getMember(), recruitment);
		return notificationRepository.save(notification);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Notification notifyWithNewTx(RecruitmentEvent event) {
		log.info("notifyWithNewTx - start");
		Recruitment recruitment = findRecruitment(event.getId());

		valid(recruitment);

		Notification notification = new Notification(recruitment.getMember(), recruitment);
		log.info("notifyWithNewTx - end");
		return notificationRepository.save(notification);
	}

	private void valid(Recruitment recruitment) {
		if (recruitment.getTitle().contains("invalid")) {
			throw new IllegalArgumentException("강제 예외 발생");
		}
	}

	private Recruitment findRecruitment(final Long recruitmentId) {
		return recruitmentRepository.findById(recruitmentId).orElseThrow();
	}

}
