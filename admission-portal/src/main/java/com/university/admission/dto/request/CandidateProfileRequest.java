package com.university.admission.dto.request;

import com.university.admission.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Du lieu form sua thong tin ca nhan cua thi sinh (Candidate).
 */
public class CandidateProfileRequest {

    // @DateTimeFormat bat buoc phai co de Thymeleaf/Spring HIEN THI dung gia tri LocalDate
    // ra input type="date" (dinh dang yyyy-MM-dd). Neu thieu annotation nay, luc LUU van hoat
    // dong binh thuong (Spring tu doan duoc khi doc du lieu tu form gui len), nhung luc HIEN
    // LAI gia tri cu vao form thi se bi rong.
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private Gender gender;
    private String idCardNumber;
    private String phone;
    private String address;
    private String province;
    private String highSchool;
    private Integer graduationYear;

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getHighSchool() { return highSchool; }
    public void setHighSchool(String highSchool) { this.highSchool = highSchool; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
}
