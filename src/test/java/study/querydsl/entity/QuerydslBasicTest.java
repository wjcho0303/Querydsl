package study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before() {
        jpaQueryFactory = new JPAQueryFactory(em);
        // 트랜잭션에 바인딩 되어 분배되도록 설계되었기 때문에 이렇게 해도 멀티 쓰레드 환경에서 문제가 발생하지 않는다.

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("JPQL을 통한 조회")
    public void startJPQL() {
        // member1 을 찾는 JPQL
        String query = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("Querydsl을 통한 조회")
    public void startQuerydsl() {
        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))    // 파라미터 바인딩 자동 처리됨
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("검색조건 쿼리")
    public void search() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member2")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member2");
    }

    @Test
    @DisplayName("AND -> ,")
    public void searchAndParam() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member2"),  // WHERE 내부의 AND를 콤마(,)로 처리할 수도 있다.
                        member.age.eq(20)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member2");
    }

    @Test
    public void resultFetch() {

        // .fetch()
        List<Member> members = jpaQueryFactory
                .selectFrom(member)
                .fetch();


        // .fetchOne()
        // NonUniqueResultException 발생으로 인해 주석 처리
//        Member member1 = jpaQueryFactory
//                .selectFrom(member)
//                .fetchOne();
//        System.out.println("member1 = " + member1);


        // .fetchFirst() = .limit(1).fetchOne()
        Member member2 = jpaQueryFactory
                .selectFrom(member)
                .fetchFirst();
        System.out.println("member2 = " + member2);


        // .fetchFirst() = .limit(1).fetchOne()
        Member member3 = jpaQueryFactory
                .selectFrom(member)
                .limit(1)
                .fetchOne();
        System.out.println("member3 = " + member3);


        // .fetchResults()
        QueryResults<Member> results = jpaQueryFactory
                .selectFrom(member)
                .fetchResults();
        System.out.println("results = " + results);

        System.out.println("results.getResults(); = " + results.getResults());
        System.out.println("results.getTotal() = " + results.getTotal());
        System.out.println("results.getOffset() = " + results.getOffset());
        System.out.println("results.getLimit() = " + results.getLimit());


        // .fetchCount()
        long total = jpaQueryFactory
                .selectFrom(member)
                .fetchCount();
        System.out.println("total = " + total);
    }

}