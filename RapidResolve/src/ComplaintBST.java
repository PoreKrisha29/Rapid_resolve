/**
 * Simple Binary Search Tree keyed by `complaintId` for in-memory sorting.
 * Used to present a citizen's complaints in ascending order without extra SQL ordering.
 */
public class ComplaintBST {

    /**
     * Internal BST node wrapper for `Complaint` data.
     */
    class Node {
        Complaint data;
        Node left, right;

        Node(Complaint data) {
            this.data = data;
            left = right = null;
        }
    }

    private Node root;

    // ✅ Insert into BST based on complaintId
    /**
     * Insert a complaint into the tree based on its `complaintId`.
     */
    public void insert(Complaint c) {
        root = insertRec(root, c);
    }

    /**
     * Recursive insert helper.
     */
    private Node insertRec(Node root, Complaint c) {
        if (root == null) return new Node(c);

        if (c.getComplaintId() < root.data.getComplaintId()) {
            root.left = insertRec(root.left, c);
        } else {
            root.right = insertRec(root.right, c);
        }
        return root;
    }

    // ✅ In-order traversal (sorted by complaintId)
    /**
     * Print the tree contents in-order (sorted by complaintId).
     */
    public void inOrder() {
        if (root == null) {
            System.out.println("📭 No complaints found.");
            return;
        }
        inOrderRec(root);
    }

    /**
     * Recursive in-order traversal helper.
     */
    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.left);
            System.out.println(root.data);  // relies on Complaint.toString()
            inOrderRec(root.right);
        }
    }
}
