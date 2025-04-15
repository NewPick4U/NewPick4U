package com.newpick4u.thread.thread.domain.repository;

import com.newpick4u.thread.thread.domain.entity.Thread;
import com.newpick4u.thread.thread.domain.entity.ThreadStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ThreadRepository {

  Page<Thread> findAll(Pageable pageable);

  Optional<Thread> findById(UUID threadId);

  Optional<Thread> findByTagName(String tagName);

  Thread save(Thread thread);

  List<Thread> findAllByStatus(ThreadStatus threadStatus);

  void deleteAll();
}
