package helloworld.event_listener.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;

@Entity
@Getter
public class Notification {

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne
	private Member member;

	@OneToOne
	private Recruitment recruitment;

	public Notification() {
	}

	public Notification(Member member, Recruitment recruitment) {
		this.member = member;
		this.recruitment = recruitment;
	}
}
