# Admission Portal - He thong quan ly tuyen sinh truc tuyen

Do an mon Cong nghe Java - Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, MySQL.

## Cach mo project trong NetBeans

1. Mo NetBeans -> File -> Open Project -> chon thu muc `admission-portal` (thu muc chua file `pom.xml`).
   NetBeans se tu nhan day la Maven project va tu tai dependency tu `pom.xml`.
2. Doi username/password MySQL trong file:
   `src/main/resources/application.properties`
   ```
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
3. Dam bao da chay file `admission_schema.sql` de tao database `admission_db` (xem huong dan buoc truoc).
4. Chuot phai vao project -> Run (hoac chay class `AdmissionApplication.java`).
5. Mo trinh duyet: http://localhost:8080

## Tai khoan mau (da co san trong CSDL)

| Username | Password | Vai tro |
|----------|----------|---------|
| admin    | admin123 | ADMIN   |
| staff01  | admin123 | STAFF   |

## Cau truc thu muc chinh

```
src/main/java/com/university/admission/
  entity/       - Cac lop JPA anh xa bang CSDL (User, Candidate, Application...)
  enums/        - Role, ApplicationStatus, SessionStatus...
  repository/   - (se bo sung o buoc sau) Spring Data JPA interfaces
  service/      - (se bo sung o buoc sau) Logic nghiep vu
  controller/   - (se bo sung o buoc sau) Xu ly request, tra ve view
  config/       - (se bo sung o buoc sau) SecurityConfig, WebConfig...
  security/     - (se bo sung o buoc sau) CustomUserDetailsService...

src/main/resources/
  application.properties  - Cau hinh ket noi CSDL, JPA, Thymeleaf
  templates/               - Cac file HTML Thymeleaf (se bo sung o buoc sau)
  static/                  - CSS, JS, hinh anh
```

## Trang thai hien tai

- [x] Cau truc project Maven chuan
- [x] Ket noi CSDL MySQL
- [x] 9 Entity JPA anh xa day du 9 bang trong database
- [ ] Repository (Spring Data JPA)
- [ ] Service layer
- [ ] Spring Security config (dang nhap, phan quyen)
- [ ] Controller + giao dien Thymeleaf

## Ghi chu quan trong

`spring.jpa.hibernate.ddl-auto=update` duoc dat trong `application.properties` de Hibernate
tu dong doi chieu Entity voi bang co san, KHONG xoa du lieu. Neu Entity va bang SQL lech nhau
(vi du sai ten cot), hay kiem tra lai script SQL hoac Entity tuong ung.
