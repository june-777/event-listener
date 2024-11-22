package helloworld.event_listener.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RecruitmentEvent {

	private final Long id;

	public RecruitmentEvent(Long id) {
		this.id = id;
	}
}
