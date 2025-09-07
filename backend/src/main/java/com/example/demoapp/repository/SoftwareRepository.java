// src/main/java/com/example/demoapp/repository/SoftwareRepository.java
package com.example.demoapp.repository;

import com.example.demoapp.model.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {
}
