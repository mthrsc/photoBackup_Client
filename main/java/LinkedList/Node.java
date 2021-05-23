package LinkedList;

import java.io.Serializable;

import Objects.PhotoObject;

public class Node<T extends PhotoObject> implements Serializable {

    PhotoObject object;  //node object
    Node next;  //next pointer

    public Node(PhotoObject elem) {  //Overridden constructor
        object = elem;
        next = null;
    }
    public Node() {     //Empty constructor used when I need a temporary node
        object = null;
        next = null;
    }
}
