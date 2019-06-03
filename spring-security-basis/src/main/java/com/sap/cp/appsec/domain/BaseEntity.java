package com.sap.cp.appsec.domain;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    /**
     ** technical fields
     **/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Version
    @Column(name = "version")
    protected long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    protected Timestamp createdAt;

    @Column(name = "modified_at", insertable = false)
    @LastModifiedDate
    protected Timestamp modifiedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    @CreatedBy
    protected String createdBy;

    @Column(name = "modified_by", insertable = false)
    @LastModifiedBy
    protected String modifiedBy;


    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    // use only in tests or when you need to map DTO to Entity
    public void setId(Long id) {
        this.id = id;
    }

    // use only in tests or when you need to map DTO to Entity
    public void setVersion(long version) {
        this.version = version;
    }

}
