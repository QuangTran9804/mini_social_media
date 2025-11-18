package com.example.social.config;

import com.example.social.domain.Post;
import com.example.social.domain.User;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Populate a minimal dataset on application startup so the app is usable immediately
 * without any manual seeding. Idempotent: safe to run multiple times.
 */
@Component
@Transactional
public class StartupSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final PostRepository postRepository;

	public StartupSeeder(UserRepository userRepository,
						 PasswordEncoder passwordEncoder,
						 PostRepository postRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.postRepository = postRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		// Create exactly three specific users if missing:
		// ('User A','a@example.com','123456'),
		// ('User B','b@example.com','123456'),
		// ('User C','c@example.com','123456')
		createIfMissing("User A", "a@example.com", "123456", 11);
		createIfMissing("User B", "b@example.com", "123456", 12);
		createIfMissing("User C", "c@example.com", "123456", 13);

		seedPosts();
	}

	private void createIfMissing(String username, String email, String rawPassword, int avatarIndex) {
		User existing = null;
		try {
			existing = userRepository.findByEmail(email);
		} catch (Exception ignored) {}
		if (existing != null) return;
		User user = User.builder()
				.username(username)
				.email(email)
				.password(passwordEncoder.encode(rawPassword))
				.avatarUrl("https://i.pravatar.cc/150?img=" + avatarIndex)
				.bio("Hello, I'm " + username)
				.build();
		userRepository.save(user);
	}

	private void seedPosts() {
		if (postRepository.count() > 0) {
			return;
		}
		User userA = userRepository.findByEmail("a@example.com");
		User userB = userRepository.findByEmail("b@example.com");
		User userC = userRepository.findByEmail("c@example.com");

		if (userA == null || userB == null || userC == null) {
			return;
		}

		List<Post> posts = List.of(
				Post.builder()
						.user(userA)
						.content("Chào mọi người! Đây là bài viết đầu tiên của tôi trên Mini Social 👋")
						.imageUrl("https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=800&q=80")
						.build(),
				Post.builder()
						.user(userB)
						.content("Hôm nay thời tiết thật đẹp, cùng nhau ra ngoài thôi! ☀️")
						.imageUrl("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80")
						.build(),
				Post.builder()
						.user(userC)
						.content("Đang học Spring Boot và React, ai có tips hay chia sẻ với mình nhé!")
						.build()
		);

		postRepository.saveAll(posts);
	}
}


