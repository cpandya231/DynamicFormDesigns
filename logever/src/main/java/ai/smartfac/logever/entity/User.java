package ai.smartfac.logever.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String username;
    @Column(nullable = true)
    private String email;
    @Column(nullable=false)
    private String first_name;
    private String last_name;
    @Column(nullable=false,name = "date_of_birth")
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date dateOfBirth;
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;
    private String windows_id;
    private String reporting_manager;
    @Column(nullable=false)
    private String password;
    private String code;
    private String designation;
    @CreationTimestamp
    @JsonFormat(pattern="yyyy-MM-dd")
    @Column(columnDefinition = "DATE DEFAULT (CURRENT_DATE)")
    private Date hireDate;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @Column(name = "last_password_changed_dt")
    private Date lastPasswordChangedDate;
    @CreatedBy
    @JsonIgnore
    @Column(name = "created_by")
    private String createdBy;
    @Column(name="create_dt")
    @CreationTimestamp
    private Timestamp createDt;
    @Column(name = "updated_by")
    @LastModifiedBy
    @JsonIgnore
    private String updatedBy;
    @JsonIgnore
    @Column(name="update_dt")
    @UpdateTimestamp
    private Timestamp updateDt;
    @Column(name = "is_active",nullable = false,columnDefinition = "BOOLEAN")
    private Boolean isActive;
    @Column(name = "last_login_dt")
    private Timestamp lastLoginDt;

    @Transient
    private String fullName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role",joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    @Override
    public String getUsername() {
        return username;
    }


    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.getIsActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.getIsActive();
    }

    @Override
    public boolean isEnabled() {
        return this.getIsActive();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Date getLastPasswordChangedDate() {
        return lastPasswordChangedDate;
    }

    public void setLastPasswordChangedDate(Date lastPasswordChangedDate) {
        this.lastPasswordChangedDate = lastPasswordChangedDate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = this.getRoles().stream().flatMap(role->role.getPermissions().stream())
                .map(perm->new SimpleGrantedAuthority(perm.getPermission())).collect(Collectors.toList());
        grantedAuthorities.addAll(this.getRoles().stream().map(role->new SimpleGrantedAuthority(role.getRole())).collect(Collectors.toList()));
        return grantedAuthorities;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Timestamp createDt) {
        this.createDt = createDt;
    }

    public Timestamp getUpdateDt() {
        return updateDt;
    }

    public void setUpdateDt(Timestamp updateDt) {
        this.updateDt = updateDt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Timestamp getLastLoginDt() {
        return lastLoginDt;
    }

    public void setLastLoginDt(Timestamp lastLoginDt) {
        this.lastLoginDt = lastLoginDt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmployee_code() {
        return employeeCode;
    }

    public void setEmployee_code(String employee_code) {
        this.employeeCode = employee_code;
    }

    public String getWindows_id() {
        return windows_id;
    }

    public void setWindows_id(String windows_id) {
        this.windows_id = windows_id;
    }

    public String getReporting_manager() {
        return reporting_manager;
    }

    public void setReporting_manager(String reporting_manager) {
        this.reporting_manager = reporting_manager;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public String getFullName() {
        return first_name + " " + last_name;
    }
}
