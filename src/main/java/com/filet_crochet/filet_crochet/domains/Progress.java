package com.filet_crochet.filet_crochet.domains;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Document(collection = "PROGRESS")
public class Progress {
    private List<FilledCell> highlightedCells;
    private LocalDateTime updatedAt;

    public List<FilledCell> getHighlightedCells() {
        return highlightedCells;
    }

    public void setHighlightedCells(List<FilledCell> highlightedCells) {
        this.highlightedCells = highlightedCells;
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
        Progress progress = (Progress) o;
        return Objects.equals(highlightedCells, progress.highlightedCells) && Objects.equals(updatedAt, progress.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(highlightedCells, updatedAt);
    }
}
