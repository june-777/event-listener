package helloworld.event_listener.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import helloworld.event_listener.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
