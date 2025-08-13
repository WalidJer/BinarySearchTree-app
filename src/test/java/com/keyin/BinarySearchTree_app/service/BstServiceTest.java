package com.keyin.BinarySearchTree_app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyin.BinarySearchTree_app.dto.TreeNodeDTO;
import com.keyin.BinarySearchTree_app.entity.TreeRecord;
import com.keyin.BinarySearchTree_app.repository.TreeRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BstServiceTest {

    @Mock
    TreeRecordRepository repo;
    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    BstService svc;

    // ---------- parseNumbers ----------
    @Test
    void parseNumbers_acceptsSpacesCommasSemicolons() {
        assertEquals(List.of(7,3,9,1,5), svc.parseNumbers("7, 3;9 1 5"));
    }

    @Test
    void parseNumbers_emptyStringReturnsEmptyList() {
        assertTrue(svc.parseNumbers("   ").isEmpty());
    }

    @Test
    void parseNumbers_nonNumericThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> svc.parseNumbers("1, two, 3"));
    }

    // ---------- plain BST ----------
    @Test
    void buildTreeDto_insertionOrderAndDuplicatesGoLeft() {
        // with <= in insert, duplicates go LEFT: 5 <- 5 <- 5 (a left chain)
        TreeNodeDTO t = svc.buildTreeDto(List.of(5, 5, 5));
        assertEquals(5, t.value);
        assertNotNull(t.left);
        assertEquals(5, t.left.value);
        assertNotNull(t.left.left);
        assertEquals(5, t.left.left.value);
        assertNull(t.right);
    }

    // ---------- balanced BST ----------
    @Test
    void buildBalancedTreeDto_usesSortedDistinctAndCentersRoot() {
        // duplicates removed by distinct(), expect middle(1,2,3,4,5)=3
        TreeNodeDTO t = svc.buildBalancedTreeDto(List.of(1,2,3,3,4,5,5));
        assertEquals(3, t.value);
        assertEquals(1, t.left.value);
        assertEquals(2, t.left.right.value);   // 2 > 1 -> right child
        assertEquals(4, t.right.value);
        assertEquals(5, t.right.right.value);
    }

    @Test
    void buildAndSave_savesToRepository() throws Exception {
        TreeNodeDTO dto = svc.buildAndSave("9,4,7,1,8", false);

        assertEquals(9, dto.value); // root check
        verify(repo, times(1)).save(any(TreeRecord.class)); // just check it saved something
    }


    @Test
    void buildAndSave_withBalancedFlagSaves() throws Exception {
        TreeNodeDTO dto = svc.buildAndSave("1,2,3,4,5", true);

        assertEquals(3, dto.value); // balanced root should be 3
        verify(repo, times(1)).save(any(TreeRecord.class)); // just check it saved something
    }


}
