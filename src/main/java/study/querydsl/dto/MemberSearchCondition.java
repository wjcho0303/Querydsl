package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {

    // 회원명, 팀명, 나이(ageGoe, ageLoe)
    private String username;
    private String teamName;

    // 나이 데이터가 null 일 경우를 대비하여 Integer
    private Integer ageGoe;
    private Integer ageLoe;
}
