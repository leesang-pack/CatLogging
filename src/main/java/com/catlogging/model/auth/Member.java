package com.catlogging.model.auth;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(
        name="Member",
        uniqueConstraints={
                @UniqueConstraint(
                        name = "NAME_AGE_UNIQUE",
                        columnNames={"id","memberId"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@ToString
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    private String memberId;

    private String password;

    private Boolean isAdmin;
}