package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.FilledCellDto;
import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressService Tests")
class ProgressServiceTests {

    @Mock
    private MongoTemplate mongoTemplate;

    private ProgressService progressService;
    private ObjectId testObjectId;
    private List<FilledCellDto> testCells;

    @BeforeEach
    void setUp() {
        progressService = new ProgressService(mongoTemplate);
        testObjectId = new ObjectId();
        testCells = List.of(new FilledCellDto(0, 0), new FilledCellDto(1, 1));
    }

    // ==================== getById Tests ====================

    @Test
    @DisplayName("getById should return progress when found")
    void testGetByIdFound() {
        // Arrange
        String id = testObjectId.toString();
        ProgressDto expected = new ProgressDto(LocalDateTime.now(), testCells);

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(expected);

        // Act
        Optional<ProgressDto> result = progressService.getById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        verify(mongoTemplate, times(1)).findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION);
    }

    @Test
    @DisplayName("getById should return empty Optional when progress not found")
    void testGetByIdNotFound() {
        // Arrange
        String id = testObjectId.toString();

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(null);

        // Act
        Optional<ProgressDto> result = progressService.getById(id);

        // Assert
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION);
    }

    @Test
    @DisplayName("getById should return empty Optional on invalid ObjectId")
    void testGetByIdInvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        when(mongoTemplate.findById(any(), eq(ProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new IllegalArgumentException("Invalid ObjectId"));

        // Act
        Optional<ProgressDto> result = progressService.getById(invalidId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getById should return empty Optional on database exception")
    void testGetByIdDatabaseException() {
        // Arrange
        String id = testObjectId.toString();

        when(mongoTemplate.findById(any(ObjectId.class), eq(ProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        Optional<ProgressDto> result = progressService.getById(id);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getById should handle null id gracefully")
    void testGetByIdNullId() {
        // Arrange
        when(mongoTemplate.findById(any(), eq(ProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new NullPointerException());

        // Act
        Optional<ProgressDto> result = progressService.getById(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== upsertById Tests ====================

    @Test
    @DisplayName("upsertById should return updated progress when upsert succeeds")
    void testUpsertByIdSuccess() {
        // Arrange
        String id = testObjectId.toString();
        List<FilledCellDto> newCells = List.of(new FilledCellDto(5, 5), new FilledCellDto(6, 6));
        UpsertProgressDto upsertDto = new UpsertProgressDto(newCells);
        ProgressDto updatedProgress = new ProgressDto(LocalDateTime.now(), newCells);

        var mockResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(mockResult.wasAcknowledged()).thenReturn(true);
        when(mockResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mockResult);

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(updatedProgress);

        // Act
        Optional<ProgressDto> result = progressService.upsertById(id, upsertDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(updatedProgress, result.get());
        assertEquals(newCells, result.get().cells());
        verify(mongoTemplate, times(1)).replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION));
        verify(mongoTemplate, times(1)).findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION);
    }

    @Test
    @DisplayName("upsertById should return empty Optional when no document is modified")
    void testUpsertByIdNoModification() {
        // Arrange
        String id = testObjectId.toString();
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);

        var mockResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(mockResult.wasAcknowledged()).thenReturn(true);
        when(mockResult.getModifiedCount()).thenReturn(0L);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mockResult);

        // Act
        Optional<ProgressDto> result = progressService.upsertById(id, upsertDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("upsertById should return empty Optional when operation is not acknowledged")
    void testUpsertByIdNotAcknowledged() {
        // Arrange
        String id = testObjectId.toString();
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);

        var mockResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(mockResult.wasAcknowledged()).thenReturn(false);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mockResult);

        // Act
        Optional<ProgressDto> result = progressService.upsertById(id, upsertDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("upsertById should return empty Optional on invalid ObjectId")
    void testUpsertByIdInvalidId() {
        // Arrange
        String invalidId = "invalid-id";
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new IllegalArgumentException("Invalid ObjectId"));

        // Act
        Optional<ProgressDto> result = progressService.upsertById(invalidId, upsertDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("upsertById should return empty Optional on database exception")
    void testUpsertByIdDatabaseException() {
        // Arrange
        String id = testObjectId.toString();
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        Optional<ProgressDto> result = progressService.upsertById(id, upsertDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("upsertById should handle null id gracefully")
    void testUpsertByIdNullId() {
        // Arrange
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new NullPointerException());

        // Act
        Optional<ProgressDto> result = progressService.upsertById(null, upsertDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("upsertById should replace entire document with new highlighted cells")
    void testUpsertByIdReplacesDocument() {
        // Arrange
        String id = testObjectId.toString();
        List<FilledCellDto> newCells = List.of(new FilledCellDto(10, 10));
        UpsertProgressDto upsertDto = new UpsertProgressDto(newCells);
        ProgressDto updatedProgress = new ProgressDto(LocalDateTime.now(), newCells);

        var mockResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(mockResult.wasAcknowledged()).thenReturn(true);
        when(mockResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mockResult);

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(updatedProgress);

        // Act
        Optional<ProgressDto> result = progressService.upsertById(id, upsertDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(newCells, result.get().cells());
    }

    // ==================== deleteById Tests ====================

    @Test
    @DisplayName("deleteById should successfully delete progress")
    void testDeleteByIdSuccess() {
        // Arrange
        String id = testObjectId.toString();

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> progressService.deleteById(id));

        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION));
    }

    @Test
    @DisplayName("deleteById should handle invalid ObjectId gracefully")
    void testDeleteByIdInvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        when(mongoTemplate.remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new IllegalArgumentException("Invalid ObjectId"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> progressService.deleteById(invalidId));
    }

    @Test
    @DisplayName("deleteById should handle database exception gracefully")
    void testDeleteByIdDatabaseException() {
        // Arrange
        String id = testObjectId.toString();

        when(mongoTemplate.remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> progressService.deleteById(id));
    }

    @Test
    @DisplayName("deleteById should handle null id gracefully")
    void testDeleteByIdNullId() {
        // Arrange
        when(mongoTemplate.remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenThrow(new NullPointerException());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> progressService.deleteById(null));
    }

    @Test
    @DisplayName("deleteById should call mongoTemplate.remove with correct parameters")
    void testDeleteByIdCallsRemoveCorrectly() {
        // Arrange
        String id = testObjectId.toString();

        // Act
        progressService.deleteById(id);

        // Assert
        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION));
    }

    @Test
    @DisplayName("deleteById should handle empty response gracefully")
    void testDeleteByIdEmptyResponse() {
        // Arrange
        String id = testObjectId.toString();

        when(mongoTemplate.remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mock(com.mongodb.client.result.DeleteResult.class));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> progressService.deleteById(id));

        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(ProgressService.PROGRESS_COLLECTION));
    }

    // ==================== Integration-like Tests ====================

    @Test
    @DisplayName("Should be able to getById after successful upsertById")
    void testGetByIdAfterUpsert() {
        // Arrange
        String id = testObjectId.toString();
        UpsertProgressDto upsertDto = new UpsertProgressDto(testCells);
        ProgressDto progressAfterUpsert = new ProgressDto(LocalDateTime.now(), testCells);

        var mockResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(mockResult.wasAcknowledged()).thenReturn(true);
        when(mockResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.replace(any(Query.class), any(UpsertProgressDto.class), eq(ProgressService.PROGRESS_COLLECTION)))
                .thenReturn(mockResult);

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(progressAfterUpsert);

        // Act
        Optional<ProgressDto> upsertResult = progressService.upsertById(id, upsertDto);
        Optional<ProgressDto> getResult = progressService.getById(id);

        // Assert
        assertTrue(upsertResult.isPresent());
        assertTrue(getResult.isPresent());
        assertEquals(progressAfterUpsert, getResult.get());
    }

    @Test
    @DisplayName("getById should return empty after deleteById")
    void testGetByIdAfterDelete() {
        // Arrange
        String id = testObjectId.toString();

        when(mongoTemplate.findById(testObjectId, ProgressDto.class, ProgressService.PROGRESS_COLLECTION))
                .thenReturn(null);

        // Act
        progressService.deleteById(id);
        Optional<ProgressDto> result = progressService.getById(id);

        // Assert
        assertTrue(result.isEmpty());
    }
}




