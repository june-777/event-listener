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

			// 아래 테스트를 위해 주석처리
			// when
			// applicationEventPublisher.publishEvent(event);

			// then
			// Mockito.verify(applicationEventListener, times(1)).onApplicationEvent(event);
		}
	}

	/**
	 * Listener: @EventListener
	 * NotificationService txPropagation: REQUIRED
	 */
	@Nested
	@DisplayName("@EventListener 기본 트랜잭션 테스트")
	class EventListenerTxTest {

		@Test
		@DisplayName("하나의 스레드에서 동기적으로 실행되므로 이벤트 발행자/수신자는 하나의 트랜잭션이다.")
		void publisherAndListenerIsSameTx() {
			// given
			// when: 모집공고 생성 -> 알림 이벤트 생성
			recruitment = recruitmentService.create(member, recruitmentTitle);

			// then: 모집공고 서비스 커밋
			assertThat(recruitmentRepository.findById(recruitment.getId()))
					.isPresent().get()
					.extracting(Recruitment::getId)
					.isEqualTo(recruitment.getId());

			// then: 알림 서비스 커밋
			assertThat(notificationRepository.findAll()).isNotEmpty();
		}

		@Test
		@DisplayName("동일 트랜잭션이므로, 수신측에서 예외가 발생하면 성공했던 발행측도 함께 롤백된다.")
		void publisherAndListenerRollback() {
			// given, when: 모집공고 서비스 성공, 알림 서비스에서 예외 발생
			assertThatThrownBy(() -> recruitmentService.create(member, "invalid"))
					.isInstanceOf(IllegalArgumentException.class);

			// then: 모집공고 서비스, 알림 서비스 모두 롤백
			assertThat(recruitmentRepository.findAll()).isEmpty();
			assertThat(notificationRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("발행측에서 수신측의 예외를 정상처리하면 발행측을 커밋할 수 있다. 하지만 수신측의 예외를 모두 알아야 하는 단점이 있다.")
		void publisherExceptionHandle() {
			// given, when: 모집공고 서비스 성공, 알림 서비스에서 예외 발생
			recruitmentService.createWithExceptionHandle(member, "invalid");

			// then
			assertThat(recruitmentRepository.findAll()).isNotEmpty();    // 예외 정상처리로 커밋
			assertThat(notificationRepository.findAll()).isEmpty();    // 예외 발생으로 저장 로직 실행자체가 x
		}
	}

	/**
	 * Listener: @EventListener
	 * NotificationService txPropagation: REQUIRES_NEW
	 */
	@Nested
	@DisplayName("@EventListener 와 REQUIRES_NEW 트랜잭션 테스트")
	class EventListenerNewTxTest {

		@Test
		@DisplayName("이벤트 발행자 / 수신자의 트랜잭션이 분리되어, 발행측에서 영속화한 엔티티를 수신측에서 조회하지 못한다.")
		void test() {
			// given, when: 모집공고 서비스 성공, 알림 서비스 예외, 모집공고 서비스에서 예외 핸들링 X
			assertThatThrownBy(() -> recruitmentService.create(member, recruitmentTitle))
					.isInstanceOf(NoSuchElementException.class);
		}

		@Test
		@DisplayName("이벤트 발행자 / 수신자의 트랜잭션이 분리되고, 발행자가 예외를 정상처리 하였으므로 커밋 / 롤백이 정상 분리된다.")
		void test2() {
			// given, when: 모집공고 서비스는 성공, 알림 서비스는 예외, 모집공고 서비스에서 예외 핸들링 O
			recruitmentService.createWithExceptionHandle(member, recruitmentTitle);

			// then:
			assertThat(recruitmentRepository.findAll()).isNotEmpty();    // 커밋
			assertThat(notificationRepository.findAll()).isEmpty();        // 롤백
		}
	}

	/**
	 * Listener: @TransactionalEventListener
	 */
	@Nested
	@DisplayName("@TransactionalEventListener 트랜잭션 테스트")
	class TransactionalEventListenerNewTxTest {

		/**
		 * Listener: @TransactionalEventListener
		 * NotificationService txPropagation: REQUIRED
		 */
		@Test
		@DisplayName("수신측의 트랜잭션 전파 옵션이 REQUIRED 인 경우 기존의 트랜잭션과 합쳐지고, "
				+ "수신측 실행 시점 (after_commit)엔 이미 커밋되었기 때문에 수신측의 커밋은 무시된다.")
		void test() {
			// given, when: 모집공고 서비스 성공, 알림 서비스 성공
			recruitment = recruitmentService.create(member, recruitmentTitle);

			// then
			assertThat(recruitmentRepository.findAll()).isNotEmpty();    // 모집공고는 커밋 성공
			assertThat(notificationRepository.findAll()).isEmpty();        // 알림은 커밋 무시
		}

		/**
		 * Listener: @TransactionalEventListener
		 * NotificationService txPropagation: REQUIRES_NEW
		 */
		@Test
		@DisplayName("수신측의 트랜잭션 전파 옵션이 REQUIRES_NEW 인 경우, 새로운 트랜잭션이 생성되었으므로 커밋된다.")
		void test2() {
			// given, when: 정상 모집공고 저장 성공, 알림 이벤트 발행 성공
			recruitment = recruitmentService.create(member, recruitmentTitle);

			// then
			assertThat(recruitmentRepository.findAll()).isNotEmpty();    // 모집공고 커밋 성공
			assertThat(notificationRepository.findAll()).isNotEmpty();        // 알림 커밋 성공
		}
	}

	@Nested
	@DisplayName("이벤트 리스너를 비동기로 처리하는 테스트")
	class AsyncListener {

		/**
		 * Listener: @EventListener
		 * NotificationService txPropagation: REQUIRES_NEW
		 * 일부 테스트가 실패해야 정상
		 */
		@RepeatedTest(50)
		@DisplayName("비동기는 작업 실행 순서를 보장하지 않기 때문에, @EventListener 를 사용하면"
				+ "이벤트 발행측에서 트랜잭션 자원이 커밋되기 전에"
				+ "이벤트 수신측에서 접근할 수 있어 문제가 발생할 수 있다.")
		void test() {
			// given, when: 정상 모집공고 저장 성공, 알림 이벤트는 일부 테스트에서 NoSuchElementException 발생
			recruitment = recruitmentService.create(member, recruitmentTitle);

			// then
			assertThat(recruitmentRepository.findAll()).isNotEmpty(); // 모집공고 커밋 성공

			// 비동기 작업 추적
			await().atMost(1, TimeUnit.SECONDS)
					.untilAsserted(() ->
							assertThat(notificationRepository.findAll()).isNotEmpty()
					);
		}

		/**
		 * Listener: @TransactionalEventListener
		 * NotificationService txPropagation: REQUIRES_NEW
		 */
		@RepeatedTest(50)
		@DisplayName("@TransactionalEventListener 를 사용하면 "
				+ "이벤트 발행측의 트랜잭션이 종료되고 비동기 로직을 호출하기 때문에"
				+ "커밋 타이밍으로 인한 문제가 발생하지 않는다.")
		void test2() {
			// given, when: 정상 모집공고 저장 성공, 알림 이벤트는 일부 테스트에서 NoSuchElementException 발생
			recruitment = recruitmentService.create(member, recruitmentTitle);

			// then
			assertThat(recruitmentRepository.findAll()).isNotEmpty(); // 모집공고 커밋 성공

			// 비동기 작업 추적
			await().atMost(1, TimeUnit.SECONDS)
					.untilAsserted(() ->
							assertThat(notificationRepository.findAll()).isNotEmpty()
					);
		}
	}

}