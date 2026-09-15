package varna.mit.kln.unimart.category.dto;

import varna.mit.kln.unimart.category.entity.Category;

public class CategoryResponseDto {

    private Integer id;
    private String name;
    private Boolean active;

    public CategoryResponseDto() {}

    public CategoryResponseDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.active = category.getActive();
    }

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
