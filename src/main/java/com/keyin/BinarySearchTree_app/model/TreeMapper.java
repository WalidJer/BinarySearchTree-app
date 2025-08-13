package com.keyin.BinarySearchTree_app.model;

import com.keyin.BinarySearchTree_app.dto.TreeNodeDTO;

public class TreeMapper {
    public static TreeNodeDTO toDto(BinaryNode n) {
        if (n == null) return null;
        TreeNodeDTO t = new TreeNodeDTO(n.value);
        t.left  = toDto(n.left);
        t.right = toDto(n.right);
        return t;
    }
}
