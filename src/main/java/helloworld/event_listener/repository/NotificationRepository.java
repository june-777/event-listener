package helloworld.event_listener.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import helloworld.event_listener.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
