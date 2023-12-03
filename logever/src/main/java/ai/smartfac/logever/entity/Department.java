package ai.smartfac.logever.entity;

import javax.persistence.*;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String site;
    private String hod;
    private String designee1;
    private String designee2;
    @Column(name = "parent_id",nullable = false)
    private Integer parentId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object department) {
        if(this.getId() == ((Department)department).getId()) {
            return true;
        } else {
            return false;
        }
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getHod() {
        return hod;
    }

    public void setHod(String hod) {
        this.hod = hod;
    }

    public String getDesignee1() {
        return designee1;
    }

    public void setDesignee1(String designee1) {
        this.designee1 = designee1;
    }

    public String getDesignee2() {
        return designee2;
    }

    public void setDesignee2(String designee2) {
        this.designee2 = designee2;
    }
}
