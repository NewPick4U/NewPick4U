package com.newpick4u.client.client.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_client")
public class Client extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "client_id", nullable = false)
  private UUID clientId;
  @Column(nullable = false, length = 50)
  private String name;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Industry industry;
  @Column(nullable = false, length = 50)
  private String email;
  @Column(nullable = false, length = 50)
  private String phone;
  @Column(nullable = false, length = 100)
  private String address;

  @Builder
  private Client(String name, Industry industry, String email, String phone,
      String address) {
    this.name = name;
    this.industry = industry;
    this.email = email;
    this.phone = phone;
    this.address = address;
  }

  public static Client create(String name, Industry industry, String email, String phone,
      String address) {
    return Client.builder()
        .name(name)
        .industry(industry)
        .email(email)
        .phone(phone)
        .address(address)
        .build();
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updatePhone(String phone) {
    this.phone = phone;
  }

  public void updateAddress(String address) {
    this.address = address;
  }

  public void updateClient(String name, String email, String phone, String address) {
    if (StringUtils.hasText(name)) {
      this.name = name;
    }
    if (StringUtils.hasText(email)) {
      this.email = email;
    }
    if (StringUtils.hasText(phone)) {
      this.phone = phone;
    }
    if (StringUtils.hasText(address)) {
      this.address = address;
    }
  }

  @Getter
  public enum Industry {
    TECHNOLOGY("기술"),
    FINANCE("금융"),
    HEALTHCARE("헬스케어"),
    EDUCATION("교육"),
    MANUFACTURING("제조"),
    TRANSPORTATION("운송"),
    REAL_ESTATE("부동산");

    private final String description;

    Industry(String description) {
      this.description = description;
    }
  }
}
