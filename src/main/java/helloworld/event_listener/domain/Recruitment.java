package helloworld.event_listener.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString(of = "id")
public class Recruitment {

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	private Member member;

	private String title;

	public Recruitment() {
	}

	public Recruitment(Member member, String title) {
		this.member = member;
		this.title = title;
	}
}
