package com.axono.model;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String name = "";
    private String yearOfStudy = "";
    private String institution = "";
    private List<String> subjects = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String v) {
        yearOfStudy = v;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String v) {
        institution = v;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> v) {
        subjects = v;
    }
}
