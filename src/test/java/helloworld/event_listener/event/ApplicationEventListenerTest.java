package helloworld.event_listener.event;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;

import helloworld.event_listener.domain.Member;
import helloworld.event_listener.domain.Recruitment;
import helloworld.event_listener.repository.MemberRepository;
import helloworld.event_listener.repository.NotificationRepository;
import helloworld.event_listener.repository.RecruitmentRepository;
import helloworld.event_listener.service.RecruitmentService;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class ApplicationEventListenerTest {

	@Autowired
	private RecruitmentService recruitmentService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RecruitmentRepository recruitmentRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	// fixture
	Member member = new Member("멤버1");
	Recruitment recruitment;
	String recruitmentTitle = "모집공고1";

	@BeforeEach
	void init() {
		log.info("======= beforeEach start =======");
		member = memberRepository.save(member);
		log.info("======= beforeEach end =======");
	}

	@AfterEach
	void delete() {
		log.info("======= afterEach start =======");
		notificationRepository.deleteAll();
		recruitmentRepository.deleteAll();
		memberRepository.deleteAll();
		log.info("======= afterEach end =======");
	}

	@Nested
	@DisplayName("@EventListener 기본 동작 테스트")
	class EventListenerBasicTest {

		@Autowired
		ApplicationEventPublisher applicationEventPublisher;

		@SpyBean
		ApplicationEventListener applicationEventListener;

		@Test
		@DisplayName("ApplicationEventListener 로 발신한 이벤트는 @EventListener 가 수신한다.")
		void test() {
			// given, when
			RecruitmentEvent event = new RecruitmentEvent(123L);

			// when
			applicationEventPublisher.publishEvent(event);

			// then
			Mockito.verify(applicationEventListener, times(1)).onApplicationEvent(event);
		}
	}

}