package LinkedList;

import java.io.Serializable;

import Objects.PhotoObject;


public class LinkedListPhoto<T extends PhotoObject> implements Serializable {       //add implements Interface - Generate interface

    private Node<PhotoObject> first;     //First and last node for LinkedList
    private Node<PhotoObject> last;

    public void add(PhotoObject object) {        //If list is empty then add node at first, otherwise at last.next
        Node<PhotoObject> newNode = new Node<>(object);
        if (isEmpty()) {
            first = newNode;
            last = first;
        } else {
            last.next = newNode;
            last = last.next;       //Update of last node for future add
        }
    }

    public void replace(int index, PhotoObject object) { //add at index
        if (index < 0 || index >= size()) {     // If index is out of bound, throw exception
            throw new IndexOutOfBoundsException();
        }
        Node<PhotoObject> n = new Node<>(object);
        if (index == 0) {       //if index = 0, replace first node
            first.object = n.object;
            if (first.next == null) {
                last = first;       //update last
            }
        } else {
            Node curr = first;      //if index > 0, we cycle through the list and stop just before the index
            for (int i = 1; i < index; i++) {
                curr = curr.next;
            }
            curr.next.object = n.object;    //We can then work on curr.next and change its object
            if (curr.next.next == null) {
                last = curr.next;
            }
        }
    }

    public PhotoObject getPhotoObject(int index) {
        PhotoObject object = null;       //Returning a node rather than the object would have made things more difficult since Gui.java doesn't know about nodes, but knows about SongObjects
        Node curr = first;

        if (index < 0 || index >= size()) {     // If index is out of bound, throw exception
            throw new IndexOutOfBoundsException();
        }

        int i = 0;
        while (i != index) {
            curr = curr.next;
            i++;
        }
        return curr.object;
    }

    public boolean contains(PhotoObject photoObject) {
        boolean b = false;
        for (int i = 0; i < size(); i++) {
//            System.out.println("----Comparing " + getPhotoObject(i).getFileName() + " with " + photoObject.getFileName());
            if (getPhotoObject(i).toString().equals(photoObject.toString())) {
                b = true;
                return b;
            }
        }
        return b;
    }

    public boolean containsFileLocation(String locationOnDevice) {
        boolean b = false;
        for (int i = 0; i < size(); i++) {
            if (getPhotoObject(i).getLocationOnDevice() != null) {
                if (getPhotoObject(i).getLocationOnDevice().equals(locationOnDevice)) {
                    b = true;
                    return b;
                }
            }
        }
        return b;
    }

    public boolean containsId(int id) {
        boolean b = false;
        for (int i = 0; i < size(); i++) {
            if (getPhotoObject(i).getId() == id) {
                b = true;
                return b;
            }
        }
        return b;
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size()) {     // If index is out of bound, throw exception
            throw new IndexOutOfBoundsException();
        }
        if (index == 0) {       //If index = 0 set first.next (second) as first
            if (first.next == null) {   //Unless it is null and the list is now empty
                last = first = null;
            } else {
                first = first.next;
            }
        } else {        //Else cycle through list and stop before index
            Node<PhotoObject> curr = first;
            for (int i = 1; i < index; i++) {
                curr = curr.next;
            }
            curr.next = curr.next.next;     //curr.next will no point at the second object after itself, letting curr.next invisible from now on
            if (curr.next == null) {
                last = curr;
            }
        }
    }

    public void removeByPath(String path) {
        for (int i = 0; i < size(); i++) {
            if (getPhotoObject(i).getLocationOnDevice() != null) {
                String objectPath = getPhotoObject(i).getLocationOnDevice();
                if (objectPath.equals(path)) {
                    remove(i);
                }
            }
        }
    }

    public int size() {
        int i = 0;

        if (first == null) {        //If first node is null list is empty
            return 0;
        } else {
            Node curr = first;      //Start at first node and go through the list until we reach the end, adding 1 to size at every hop
            do {
                i++;
                curr = curr.next;
            } while (curr != null);

            return i;
        }
    }

    public boolean isEmpty() {
        boolean b = false;
        if (first == null) {    //If first is null, then list is empty
            b = true;
        } else {
            b = false;
        }
        return b;
    }

    public void sortChronologically() {
        //This method will organise PhotoObject based on dateUploaded
        int swapAmount = 0;
        PhotoObject temp1 = new PhotoObject();
        PhotoObject temp2 = new PhotoObject();

        for (int i = 0; i < size() - 1; i++) {
            temp1 = getPhotoObject(i);
//            System.out.println("----- sortChronologically - temp1: " + temp1.toString());
            temp2 = getPhotoObject(i + 1);
//            System.out.println("----- sortChronologically - temp2: " + temp2.toString());

            if (temp1.getDateUploaded().compareToIgnoreCase(temp2.getDateUploaded()) < 0) {
                replace(i, temp2);
                replace(i + 1, temp1);
                swapAmount++;
            }
        }
        if (swapAmount > 0) {
            sortChronologically();
        }
    }
}
