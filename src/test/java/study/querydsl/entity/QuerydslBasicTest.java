package study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

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
    @DisplayName("결과 조회")
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

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(nullsLast)
     */
    @Test
    @DisplayName("정렬")
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    @DisplayName("페이징-건 수 제한")
    public void paging1() {
        List<Member> members = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("페이징-전체 조회")
    public void paging2() {
        QueryResults<Member> results = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
        assertThat(results.getResults().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("집합")
    public void aggregation() {
        List<Tuple> result = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(10+20+30+40);
        assertThat(tuple.get(member.age.avg())).isEqualTo((double)((10+20+30+40)/4));
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 각 팀별로 평균 연령을 구하되, 팀 이름과 평균 연령으로 이루어진 테이블을 사용하라.
     */
    @Test
    @DisplayName("groupBy")
    public void group() throws Exception {
        List<Tuple> result = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo((double)(10+20)/2);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo((double)(30+40)/2);
    }

    /**
     * 팀 A에 소속된 모든 회원 조회하기
     */
    @Test
    @DisplayName("조인")
    public void join() {
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 동일한 회원을 조회하기 (즉 사람의 이름이 팀 이름과 똑같은 케이스들이다.)
     */
    @Test
    @DisplayName("세타 조인")
    public void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 "teamA"인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team on t.name = 'teamA'
     */
    @Test
    @DisplayName("ON절 활용 - 필터링")
    public void join_on_filtering() {
        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .rightJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("ON을 사용한 세타 조인")
    public void join_on_thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))    // member.team 이 아니라 team을 조인
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("페치 조인이 없을 때")
    public void without_fetchJoin() {
        em.flush();
        em.clear();

        Member member1 = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(isLoaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("페치 조인")
    public void using_fetchJoin() {
        em.flush();
        em.clear();

        Member member1 = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(isLoaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원을 조회하기
     */
    @Test
    @DisplayName("서브쿼리 eq")
    public void subQuery() {

        QMember sub_m = new QMember("sub_m");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(sub_m.age.max())
                                .from(sub_m)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원들을 조회하기
     */
    @Test
    @DisplayName("서브쿼리 goe")
    public void subQuery_goe() {

        QMember sub_m = new QMember("sub_m");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(sub_m.age.avg())
                                .from(sub_m)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 나이가 10살 초과인 회원들을 조회하기
     */
    @Test
    @DisplayName("서브쿼리 in")
    public void subQuery_in() {

        QMember sub_m = new QMember("sub_m");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(sub_m.age)
                                .from(sub_m)
                                .where(sub_m.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    @DisplayName("서브쿼리 select절")
    public void subQuery_select() {

        QMember sub_m = new QMember("sub_m");

        List<Tuple> result = jpaQueryFactory
                .select(member.username,
                        select(sub_m.age.avg())
                                .from(sub_m))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("CASE문 - 단순한 조건")
    public void simple_case() {
        List<String> result = jpaQueryFactory
                .select(member.age
                        .when(10).then("열 살")
                        .when(20).then("스무 살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String ageString : result) {
            System.out.println("ageString = " + ageString);
        }
    }

    @Test
    @DisplayName("CASE문 - CaseBuilder")
    public void caseBuilder_case() {
        List<String> result = jpaQueryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 - 20세")
                        .when(member.age.between(21, 30)).then("21 - 30세")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}