package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberTeamDto> searchByWhereParam(MemberSearchCondition condition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return hasText(usernameCond) ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression teamNameEq(String teamNameCond) {
        return hasText(teamNameCond) ? team.name.eq(teamNameCond) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoeCond) {
        return ageGoeCond != null ? member.age.goe(ageGoeCond) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoeCond) {
        return ageLoeCond != null ? member.age.loe(ageLoeCond) : null;
    }
}
