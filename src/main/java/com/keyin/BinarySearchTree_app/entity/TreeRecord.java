package com.keyin.BinarySearchTree_app.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class TreeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String inputNumbers;       // e.g., "7,3,9,1,5"

    @Lob
    private String treeJson;           // serialized JSON of the built BST

    private Instant createdAt = Instant.now();

    // getters/setters
    public Long getId() { return id; }
    public String getInputNumbers() { return inputNumbers; }
    public void setInputNumbers(String inputNumbers) { this.inputNumbers = inputNumbers; }
    public String getTreeJson() { return treeJson; }
    public void setTreeJson(String treeJson) { this.treeJson = treeJson; }
    public Instant getCreatedAt() { return createdAt; }
}
