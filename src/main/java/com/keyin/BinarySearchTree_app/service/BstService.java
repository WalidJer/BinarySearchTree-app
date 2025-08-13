package com.keyin.BinarySearchTree_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyin.BinarySearchTree_app.dto.TreeNodeDTO;
import com.keyin.BinarySearchTree_app.entity.TreeRecord;
import com.keyin.BinarySearchTree_app.model.BinaryNode;
import com.keyin.BinarySearchTree_app.model.BinarySearchTree;
import com.keyin.BinarySearchTree_app.model.TreeMapper;
import com.keyin.BinarySearchTree_app.repository.TreeRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BstService {
    private final TreeRecordRepository repo;
    private final ObjectMapper om;

    public BstService(TreeRecordRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    /** 1) Parse raw string into integers */
    public List<Integer> parseNumbers(String raw) {
        List<Integer> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) return out;
        for (String s : raw.split("[,;\\s]+")) {
            if (!s.isBlank()) out.add(Integer.parseInt(s.trim()));
        }
        return out;
    }

    /** 2) Build plain BST (insertion order) and return DTO */
    public TreeNodeDTO buildTreeDto(List<Integer> values) {
        BinarySearchTree bst = new BinarySearchTree();
        for (int v : values){
            bst.insert(v);
        }
        BinaryNode root = bst.root;
        return TreeMapper.toDto(root);
    }

    /** 3) Build balanced BST (bonus) and return DTO */
    public TreeNodeDTO buildBalancedTreeDto(List<Integer> values) {
        int[] arr = values.stream().distinct().sorted().mapToInt(i -> i).toArray();
        BinaryNode root = buildBalanced(arr, 0, arr.length - 1);
        return TreeMapper.toDto(root);
    }

    private BinaryNode buildBalanced(int[] a, int lo, int hi) {
        if (lo > hi) return null;
        int mid = (lo + hi) >>> 1;
        BinaryNode n = new BinaryNode(a[mid]);
        n.left  = buildBalanced(a, lo, mid - 1);
        n.right = buildBalanced(a, mid + 1, hi);
        return n;
    }

    /** 4) End-to-end: parse → build (plain/balanced) → serialize → save → return DTO */
    public TreeNodeDTO buildAndSave(String numbers, boolean balanced) throws JsonProcessingException {
        List<Integer> values = parseNumbers(numbers);
        TreeNodeDTO tree = balanced ? buildBalancedTreeDto(values) : buildTreeDto(values);

        TreeRecord rec = new TreeRecord();
        rec.setInputNumbers(numbers + (balanced ? " [balanced]" : ""));
        rec.setTreeJson(om.writeValueAsString(tree));
        repo.save(rec);

        return tree;
    }

    /** 5) Read previous submissions (simple projection) */
    public record TreeRecordView(Long id, String inputNumbers, java.time.Instant createdAt, String treeJson) {}

    public List<TreeRecordView> listPrevious() {
        return repo.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(r -> new TreeRecordView(r.getId(), r.getInputNumbers(), r.getCreatedAt(), r.getTreeJson()))
                .toList();
    }
}
