package com.rtlis.core.temporal;

import com.rtlis.core.model.Point;

/**
 * Represents a min-heap structure specifically designed to manage and process events
 * based on their timestamps. Events with the earliest timestamps are processed first.
 * The heap is implemented as an array, where the elements are stored in a specific order
 * to maintain the min-heap property.
 */
//-- we are using a minHeap here because we are processing events according to their timestamps
// and we wanna process the one with earliest timestamp first
public class EventHeap {
    private Point[] heap;
    private int size;
    private int capacity;

    public EventHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new Point[capacity];
        this.size = 0;
    }

    /**
     * Inserts a new {@code Point} into the min-heap. This method adds the point
     * to the next available position in the heap array and then restores the
     * min-heap property by moving the element upwards as needed.
     *
     * @param p the {@code Point} to be inserted into the heap
     * @throws IllegalStateException if the heap is full
     */
    //-- exchange two array positions
    public void insert(Point p) {
        if (size == capacity) throw new IllegalStateException("Heap is full");

        heap[size] = p;
        size++;
        heapifyUp(size - 1);
    }

    /**
     * Removes and returns the smallest element (root) from the min-heap. The method ensures
     * that the heap maintains its min-heap property after the removal by reorganizing the
     * elements as necessary.
     *
     * @return the smallest element in the heap, or {@code null} if the heap is empty
     */
    //-- save root -> move last to root -> shrink -> bubble down -> return saved root
    public Point extractMin() {
        //-- if heap is empty, there is no minimum
        if (size == 0) return null;

        //-- save the root before overwriting it
        Point min = heap[0];
        //move the last element to root i.e. the leaf
        heap[0] = heap[size - 1];

        //-- we clear the old last slot
        heap[size - 1] = null;

        //-- we shrink the heap
        size--;

        //-- if the heap is not empty,start repair downward, starting from the root
        if (size > 0) heapifyDown(0);

        //-- we return the saved root
        return min;
    }


    /**
     * Restores the min-heap property by moving the element at the specified index up the heap
     * until the heap order is valid. The method assumes that the heap property is only violated
     * between the specified index and its parent.
     *
     * @param index the index of the element to be moved up in the heap
     */
    //-- note minHeap is the one in use here
    //-- while current < parent, swap upward
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;

            //-- if current timestamp is greater than or equal to parent timestamp, then the heap order is correct
            //-- and, we stop the loop
            if (this.heap[index].getTimestamp() >= this.heap[parentIndex].getTimestamp()) {
                break;
            }

            //-- swap position if heap order is broken
            swap(index, parentIndex);

            //-- after swapping, the node we are tracking now belongs to the parent's old position
            //-- so we continue from there, to avoid keep checking the old location
            index = parentIndex;
        }
    }

    /**
     * Restores the min-heap property by moving the element at the specified index down the heap
     * until the heap order is valid. The method reorganizes the heap by comparing the element
     * with its children and swapping if necessary, ensuring the smallest element is always at the root.
     *
     * @param index the index of the element to be moved down in the heap
     */
    //-- while current > smaller child, swap downward
    private void heapifyDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            //-- leftChild < size, to ensure left child exists, coz we don't know how much deep down we are gonna go
            if (leftChild < size && heap[leftChild].getTimestamp() < heap[smallest].getTimestamp()) {
                smallest = leftChild;
            }

            //-- rightChild < size, to ensure right child exists
            if (rightChild < size && heap[rightChild].getTimestamp() < heap[smallest].getTimestamp()) {
                smallest = rightChild;
            }

            //-- if the current node is still the smallest, that mean the heap is in correct order
            //-- andwe stop the loop
            if (smallest == index) break;

            //-- if one of the children is smaller than the current node, that means the heap is broken
            //-- and we need to swap the elements
            swap(index, smallest);

            //-- after swapping, the node we are currently tracking has move downwards,
            //-- so we need to continue from there
            index = smallest;
        }
    }

    //-- exchange two array positions
    private void swap(int i, int j) {
        Point temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public Point[] getHeap() {
        return heap;
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }
}
