package com.suhwakhaeng.common.domain.user.entity;

import com.suhwakhaeng.common.domain.user.enums.Role;
import com.suhwakhaeng.common.domain.user.enums.Status;
import com.suhwakhaeng.common.global.common.entity.Location;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String nickname;
    private String profileImage;
    private String email;
    private String profileContent;

    @Embedded
    private OauthId oauthId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Embedded
    private Location location;

    public void updateProfile(User user) {
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.profileContent = user.getProfileContent();

        if (this.role == Role.USER || this.role == Role.FARMER) {
            this.role = user.getRole();
        }

        this.location = user.getLocation();
    }

    public void withdraw() {
        this.status = Status.EXIST;
    }

    public void rejoin() {
        this.status = Status.RUN;
    }

    public void accpetedBusiness() {
        this.role = Role.BUISNESS;
    }
}
