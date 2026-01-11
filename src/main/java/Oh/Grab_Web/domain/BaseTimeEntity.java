package Oh.Grab_Web.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA Entity들이 이 클래스를 상속받을 경우 필드들도 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) // Spring Data JPA Auditing 적용
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false) // 생성일은 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}