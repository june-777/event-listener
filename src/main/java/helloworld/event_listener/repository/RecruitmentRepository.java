package helloworld.event_listener.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import helloworld.event_listener.domain.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
}
