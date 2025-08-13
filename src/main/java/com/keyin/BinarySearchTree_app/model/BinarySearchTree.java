package com.keyin.BinarySearchTree_app.model;

public class BinarySearchTree {

    public BinaryNode root;

    public BinarySearchTree() {
        root = null;
    }

    // Recursive insert method
    private BinaryNode insert(BinaryNode currentNode, int value) {
        if (currentNode == null) {
            BinaryNode newNode = new BinaryNode(value);
            return newNode;
        } else if (value <= currentNode.value) {
            currentNode.left = insert(currentNode.left, value);
            return currentNode;
        } else {
            currentNode.right = insert(currentNode.right, value);
            return currentNode;
        }
    }

    // Public insert method
    public void insert(int value) {
        root = insert(root, value);
    }
}
