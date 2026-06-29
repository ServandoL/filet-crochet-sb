package com.filet_crochet.filet_crochet.domains;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Document(collection = "PATTERNS")
public class Patterns {
    private String name;
    private int rows;
    private int cols;
    private int sectionRowInterval;
    private int sectionColInterval;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private List<FilledCell> cells;

    public List<FilledCell> getCells() {
        return cells;
    }

    public void setCells(List<FilledCell> cells) {
        this.cells = cells;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getSectionColInterval() {
        return sectionColInterval;
    }

    public void setSectionColInterval(int sectionColInterval) {
        this.sectionColInterval = sectionColInterval;
    }

    public int getSectionRowInterval() {
        return sectionRowInterval;
    }

    public void setSectionRowInterval(int sectionRowInterval) {
        this.sectionRowInterval = sectionRowInterval;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Patterns patterns = (Patterns) o;
        return rows == patterns.rows && cols == patterns.cols && sectionRowInterval == patterns.sectionRowInterval && sectionColInterval == patterns.sectionColInterval && Objects.equals(name, patterns.name) && Objects.equals(updatedAt, patterns.updatedAt) && Objects.equals(createdAt, patterns.createdAt) && Objects.equals(cells, patterns.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rows, cols, sectionRowInterval, sectionColInterval, updatedAt, createdAt, cells);
    }

}
