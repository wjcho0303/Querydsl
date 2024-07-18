package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import jdk.jfr.Frequency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository repository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = repository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = repository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }
}