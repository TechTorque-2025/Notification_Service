package com.techtorque.notification_service.repository;

import com.techtorque.notification_service.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();

        testSubscription = Subscription.builder()
                .userId("user123")
                .token("firebase-token-abc123")
                .platform(Subscription.Platform.WEB)
                .active(true)
                .build();
    }

    @Test
    void testSaveSubscription() {
        Subscription saved = subscriptionRepository.save(testSubscription);

        assertThat(saved).isNotNull();
        assertThat(saved.getSubscriptionId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user123");
        assertThat(saved.getPlatform()).isEqualTo(Subscription.Platform.WEB);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void testFindById() {
        subscriptionRepository.save(testSubscription);

        Optional<Subscription> found = subscriptionRepository.findById(testSubscription.getSubscriptionId());

        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("firebase-token-abc123");
    }

    @Test
    void testFindByUserIdAndActiveTrue() {
        Subscription subscription2 = Subscription.builder()
                .userId("user123")
                .token("firebase-token-xyz789")
                .platform(Subscription.Platform.ANDROID)
                .active(true)
                .build();

        Subscription subscription3 = Subscription.builder()
                .userId("user123")
                .token("firebase-token-inactive")
                .platform(Subscription.Platform.IOS)
                .active(false)
                .build();

        subscriptionRepository.save(testSubscription);
        subscriptionRepository.save(subscription2);
        subscriptionRepository.save(subscription3);

        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByUserIdAndActiveTrue("user123");

        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).allMatch(Subscription::getActive);
    }

    @Test
    void testFindByUserIdAndToken() {
        subscriptionRepository.save(testSubscription);

        Optional<Subscription> found = subscriptionRepository
                .findByUserIdAndToken("user123", "firebase-token-abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getPlatform()).isEqualTo(Subscription.Platform.WEB);
    }

    @Test
    void testFindByToken() {
        subscriptionRepository.save(testSubscription);

        Optional<Subscription> found = subscriptionRepository
                .findByToken("firebase-token-abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user123");
    }

    @Test
    void testExistsByUserIdAndToken() {
        subscriptionRepository.save(testSubscription);

        boolean exists = subscriptionRepository
                .existsByUserIdAndToken("user123", "firebase-token-abc123");

        assertThat(exists).isTrue();

        boolean notExists = subscriptionRepository
                .existsByUserIdAndToken("user123", "non-existent-token");

        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByActiveTrueOrderByCreatedAtDesc() {
        Subscription subscription2 = Subscription.builder()
                .userId("user456")
                .token("firebase-token-def456")
                .platform(Subscription.Platform.IOS)
                .active(true)
                .build();

        Subscription subscription3 = Subscription.builder()
                .userId("user789")
                .token("firebase-token-ghi789")
                .platform(Subscription.Platform.ANDROID)
                .active(false)
                .build();

        subscriptionRepository.save(testSubscription);
        subscriptionRepository.save(subscription2);
        subscriptionRepository.save(subscription3);

        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByActiveTrueOrderByCreatedAtDesc();

        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).allMatch(Subscription::getActive);
    }

    @Test
    void testUpdateSubscription() {
        subscriptionRepository.save(testSubscription);

        testSubscription.setActive(false);
        Subscription updated = subscriptionRepository.save(testSubscription);

        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void testDeleteSubscription() {
        subscriptionRepository.save(testSubscription);
        String subscriptionId = testSubscription.getSubscriptionId();

        subscriptionRepository.deleteById(subscriptionId);

        Optional<Subscription> deleted = subscriptionRepository.findById(subscriptionId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testMultiplePlatforms() {
        Subscription webSub = Subscription.builder()
                .userId("user123")
                .token("web-token")
                .platform(Subscription.Platform.WEB)
                .active(true)
                .build();

        Subscription iosSub = Subscription.builder()
                .userId("user123")
                .token("ios-token")
                .platform(Subscription.Platform.IOS)
                .active(true)
                .build();

        Subscription androidSub = Subscription.builder()
                .userId("user123")
                .token("android-token")
                .platform(Subscription.Platform.ANDROID)
                .active(true)
                .build();

        subscriptionRepository.save(webSub);
        subscriptionRepository.save(iosSub);
        subscriptionRepository.save(androidSub);

        List<Subscription> allSubscriptions = subscriptionRepository
                .findByUserIdAndActiveTrue("user123");

        assertThat(allSubscriptions).hasSize(3);
        assertThat(allSubscriptions).anyMatch(s -> s.getPlatform() == Subscription.Platform.WEB);
        assertThat(allSubscriptions).anyMatch(s -> s.getPlatform() == Subscription.Platform.IOS);
        assertThat(allSubscriptions).anyMatch(s -> s.getPlatform() == Subscription.Platform.ANDROID);
    }
}
