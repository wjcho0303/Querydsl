package study.querydsl.repository;

import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> searchByWhereParam(MemberSearchCondition condition);

    List<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    List<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
