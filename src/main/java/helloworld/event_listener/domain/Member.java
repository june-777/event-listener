package helloworld.event_listener.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Member {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	public Member() {
	}

	public Member(String name) {
		this.name = name;
	}
}
