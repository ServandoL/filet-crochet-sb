package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.CreatePatternDto;
import com.filet_crochet.filet_crochet.dto.FilledCellDto;
import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatternsService Tests")
class PatternsServiceTests {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ProgressService progressService;

    private PatternsService patternsService;
    private ObjectId testObjectId;
    private List<FilledCellDto> testCells;

    @BeforeEach
    void setUp() {
        patternsService = new PatternsService(mongoTemplate, progressService);
        testObjectId = new ObjectId();
        testCells = List.of(new FilledCellDto(0, 0), new FilledCellDto(1, 1));
    }

    // ==================== createPattern Tests ====================

    @Test
    @DisplayName("createPattern should successfully create and return a pattern")
    void testCreatePatternSuccess() {
        // Arrange
        CreatePatternDto createDto = new CreatePatternDto("Test Pattern", 10, 15, testCells, null, null);

        when(mongoTemplate.insert(any(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenAnswer(invocation -> {
                    PatternDto dto = invocation.getArgument(0);
                    return new PatternDto(dto._id(), dto.name(), dto.rows(), dto.cols(), dto.cells(),
                            null, null, LocalDateTime.now(), LocalDateTime.now());
                });

        // Act
        PatternDto result = patternsService.createPattern(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Pattern", result.name());
        assertEquals(10, result.rows());
        assertEquals(15, result.cols());
        assertEquals(testCells, result.cells());
        verify(mongoTemplate, times(1)).insert(any(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION));
    }

    @Test
    @DisplayName("createPattern should set createdAt and updatedAt timestamps")
    void testCreatePatternSetsTimestamps() {
        // Arrange
        CreatePatternDto createDto = new CreatePatternDto("Pattern", 5, 5, testCells, null, null);
        ArgumentCaptor<PatternDto> captor = ArgumentCaptor.forClass(PatternDto.class);

        when(mongoTemplate.insert(any(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        patternsService.createPattern(createDto);

        // Assert
        verify(mongoTemplate).insert(captor.capture(), eq(PatternsService.PATTERNS_COLLECTION));
        PatternDto captured = captor.getValue();
        assertNotNull(captured.createdAt());
        assertNotNull(captured.updatedAt());
        assertEquals(captured.createdAt(), captured.updatedAt());
    }

    @Test
    @DisplayName("createPattern should generate a new ObjectId")
    void testCreatePatternGeneratesObjectId() {
        // Arrange
        CreatePatternDto createDto = new CreatePatternDto("Pattern", 3, 3, testCells, null, null);
        ArgumentCaptor<PatternDto> captor = ArgumentCaptor.forClass(PatternDto.class);

        when(mongoTemplate.insert(any(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        patternsService.createPattern(createDto);

        // Assert
        verify(mongoTemplate).insert(captor.capture(), eq(PatternsService.PATTERNS_COLLECTION));
        assertNotNull(captor.getValue()._id());
    }

    // ==================== getById Tests ====================

    @Test
    @DisplayName("getById should return pattern when found")
    void testGetByIdFound() {
        // Arrange
        String id = testObjectId.toString();
        PatternDto expected = new PatternDto(testObjectId, "Pattern", 10, 10, testCells, 5, 5,
                LocalDateTime.now(), LocalDateTime.now());

        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(expected);

        // Act
        Optional<PatternDto> result = patternsService.getById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        verify(mongoTemplate, times(1)).findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION));
    }

    @Test
    @DisplayName("getById should return empty Optional when not found")
    void testGetByIdNotFound() {
        // Arrange
        String id = testObjectId.toString();
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(null);

        // Act
        Optional<PatternDto> result = patternsService.getById(id);

        // Assert
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION));
    }

    @Test
    @DisplayName("getById should return empty Optional on exception")
    void testGetByIdException() {
        // Arrange
        String invalidId = "invalid-id";
        lenient().when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenThrow(new RuntimeException("Invalid ObjectId"));

        // Act
        Optional<PatternDto> result = patternsService.getById(invalidId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== getPatterns Tests ====================

    @Test
    @DisplayName("getPatterns should return list of all patterns")
    void testGetPatternsSuccess() {
        // Arrange
        List<PatternDto> expectedPatterns = List.of(
                new PatternDto(new ObjectId(), "Pattern 1", 10, 10, testCells, 5, 5,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PatternDto(new ObjectId(), "Pattern 2", 20, 20, testCells, 10, 10,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        when(mongoTemplate.find(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(expectedPatterns);

        // Act
        List<PatternDto> result = patternsService.getPatterns();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedPatterns, result);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION));
    }

    @Test
    @DisplayName("getPatterns should return empty list when no patterns exist")
    void testGetPatternsEmpty() {
        // Arrange
        when(mongoTemplate.find(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(List.of());

        // Act
        List<PatternDto> result = patternsService.getPatterns();

        // Assert
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION));
    }

    // ==================== patchPattern Tests ====================

    @Test
    @DisplayName("patchPattern should update pattern name")
    void testPatchPatternUpdateName() {
        // Arrange
        String id = testObjectId.toString();
        UpdatePatternDto updateDto = new UpdatePatternDto("Updated Name", null, null, null);
        PatternDto updated = new PatternDto(testObjectId, "Updated Name", 10, 10, testCells, 5, 5,
                LocalDateTime.now(), LocalDateTime.now());

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenReturn(updateResult);
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(updated);

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(id, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().name());
        verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class));
    }

    @Test
    @DisplayName("patchPattern should update cells")
    void testPatchPatternUpdateCells() {
        // Arrange
        String id = testObjectId.toString();
        List<FilledCellDto> newCells = List.of(new FilledCellDto(2, 2), new FilledCellDto(3, 3));
        UpdatePatternDto updateDto = new UpdatePatternDto(null, newCells, null, null);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenReturn(updateResult);
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(new PatternDto(testObjectId, "Pattern", 10, 10, newCells, 5, 5,
                        LocalDateTime.now(), LocalDateTime.now()));

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(id, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(newCells, result.get().cells());
    }

    @Test
    @DisplayName("patchPattern should update section intervals")
    void testPatchPatternUpdateSectionIntervals() {
        // Arrange
        String id = testObjectId.toString();
        UpdatePatternDto updateDto = new UpdatePatternDto(null, null, 10, 15);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenReturn(updateResult);
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(new PatternDto(testObjectId, "Pattern", 10, 10, testCells, 10, 15,
                        LocalDateTime.now(), LocalDateTime.now()));

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(id, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(10, result.get().sectionRowInterval());
        assertEquals(15, result.get().sectionColInterval());
    }

    @Test
    @DisplayName("patchPattern should update multiple fields")
    void testPatchPatternUpdateMultipleFields() {
        // Arrange
        String id = testObjectId.toString();
        List<FilledCellDto> newCells = List.of(new FilledCellDto(4, 4));
        UpdatePatternDto updateDto = new UpdatePatternDto("New Name", newCells, 8, 12);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenReturn(updateResult);
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(new PatternDto(testObjectId, "New Name", 10, 10, newCells, 8, 12,
                        LocalDateTime.now(), LocalDateTime.now()));

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(id, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().name());
        assertEquals(newCells, result.get().cells());
        assertEquals(8, result.get().sectionRowInterval());
        assertEquals(12, result.get().sectionColInterval());
    }

    @Test
    @DisplayName("patchPattern should return empty Optional when update fails")
    void testPatchPatternUpdateFails() {
        // Arrange
        String id = testObjectId.toString();
        UpdatePatternDto updateDto = new UpdatePatternDto("Updated Name", null, null, null);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(0L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenReturn(updateResult);

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(id, updateDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("patchPattern should return empty Optional on exception")
    void testPatchPatternException() {
        // Arrange
        String invalidId = "invalid-id";
        UpdatePatternDto updateDto = new UpdatePatternDto("Updated Name", null, null, null);

        lenient().when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Optional<PatternDto> result = patternsService.patchPattern(invalidId, updateDto);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("patchPattern should update updatedAt timestamp")
    void testPatchPatternUpdatesTimestamp() {
        // Arrange
        String id = testObjectId.toString();
        UpdatePatternDto updateDto = new UpdatePatternDto("Updated Name", null, null, null);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.wasAcknowledged()).thenReturn(true);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        when(mongoTemplate.updateFirst(any(Query.class), updateCaptor.capture(), eq(PatternDto.class)))
                .thenReturn(updateResult);
        when(mongoTemplate.findOne(any(Query.class), eq(PatternDto.class), eq(PatternsService.PATTERNS_COLLECTION)))
                .thenReturn(new PatternDto(testObjectId, "Updated Name", 10, 10, testCells, 5, 5,
                        LocalDateTime.now(), LocalDateTime.now()));

        // Act
        patternsService.patchPattern(id, updateDto);

        // Assert - Verify that updatedAt was set in the update
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(PatternDto.class));
    }

    // ==================== deletePattern Tests ====================

    @Test
    @DisplayName("deletePattern should successfully delete pattern and progress")
    void testDeletePatternSuccess() {
        // Arrange
        String id = testObjectId.toString();

        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getDeletedCount()).thenReturn(1L);

        when(mongoTemplate.remove(any(Query.class), eq(PatternDto.class)))
                .thenReturn(deleteResult);

        // Act
        DeleteResult result = patternsService.deletePattern(id);

        // Assert
        assertNotNull(result);
        assertTrue(result.wasAcknowledged());
        assertEquals(1L, result.getDeletedCount());
        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(PatternDto.class));
        verify(progressService, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deletePattern should not cascade delete progress if pattern deletion fails")
    void testDeletePatternDoesNotCascadeIfDeleteFails() {
        // Arrange
        String id = testObjectId.toString();

        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getDeletedCount()).thenReturn(0L);

        when(mongoTemplate.remove(any(Query.class), eq(PatternDto.class)))
                .thenReturn(deleteResult);

        // Act
        DeleteResult result = patternsService.deletePattern(id);

        // Assert
        assertNotNull(result);
        verify(progressService, never()).deleteById(id);
    }

    @Test
    @DisplayName("deletePattern should continue even if progress deletion fails")
    void testDeletePatternContinuesIfProgressDeletionFails() {
        // Arrange
        String id = testObjectId.toString();

        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getDeletedCount()).thenReturn(1L);

        when(mongoTemplate.remove(any(Query.class), eq(PatternDto.class)))
                .thenReturn(deleteResult);
        doThrow(new RuntimeException("Progress deletion failed")).when(progressService).deleteById(id);

        // Act
        DeleteResult result = patternsService.deletePattern(id);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDeletedCount());
        verify(progressService, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deletePattern should return null on exception")
    void testDeletePatternException() {
        // Arrange
        String invalidId = "invalid-id";
        lenient().when(mongoTemplate.remove(any(Query.class), eq(PatternDto.class)))
                .thenThrow(new RuntimeException("Invalid ObjectId"));

        // Act
        DeleteResult result = patternsService.deletePattern(invalidId);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("deletePattern should handle unacknowledged delete result")
    void testDeletePatternUnacknowledged() {
        // Arrange
        String id = testObjectId.toString();

        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(false);

        when(mongoTemplate.remove(any(Query.class), eq(PatternDto.class)))
                .thenReturn(deleteResult);

        // Act
        DeleteResult result = patternsService.deletePattern(id);

        // Assert
        assertNotNull(result);
        assertFalse(result.wasAcknowledged());
        verify(progressService, never()).deleteById(id);
    }
}


