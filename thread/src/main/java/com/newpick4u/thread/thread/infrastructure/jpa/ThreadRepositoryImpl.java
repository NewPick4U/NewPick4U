package com.newpick4u.thread.thread.infrastructure.jpa;

import com.newpick4u.thread.thread.domain.entity.Thread;
import com.newpick4u.thread.thread.domain.entity.ThreadStatus;
import com.newpick4u.thread.thread.domain.repository.ThreadRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThreadRepositoryImpl implements ThreadRepository {

  private final ThreadJpaRepository threadJpaRepository;
  private final ThreadRepositoryCustom threadRepositoryCustom;

  @Override
  public Slice<Thread> findAll(Pageable pageable) {
    return threadRepositoryCustom.findSliceBy(pageable);
  }

  @Override
  public Optional<Thread> findById(UUID threadId) {
    return threadJpaRepository.findById(threadId);
  }

  @Override
  public Optional<Thread> findByTagName(String tagName) {
    return threadJpaRepository.findByTagName(tagName);
  }

  @Override
  public Thread save(Thread thread) {
    return threadJpaRepository.save(thread);
  }

  @Override
  public List<Thread> findAllByStatus(ThreadStatus threadStatus) {
    return threadJpaRepository.findAllByStatus(threadStatus);
  }

  @Override
  public void deleteAll() {
    threadJpaRepository.deleteAll();
  }

  @Override
  public void incrementScoreForTags(Set<String> existingTags) {
    threadJpaRepository.incrementScoreForTags(existingTags);
  }

  @Override
  public void saveAll(List<Thread> toCreate) {
    threadJpaRepository.saveAll(toCreate);
  }

  @Override
  public Optional<Thread> findTop1ByStatusOrderByScoreAsc(ThreadStatus status) {
    return threadJpaRepository.findTop1ByStatusOrderByScoreAsc(status);
  }
}
