package varna.mit.kln.unimart.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    private Boolean active = true;

    public CategoryRequestDto() {}

    public CategoryRequestDto(String name, Boolean active) {
        this.name = name;
        this.active = active != null ? active : true;
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
