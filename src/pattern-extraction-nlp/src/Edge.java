/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;

/**
 *
 * @author aa496
 */
public class Edge implements Serializable{

    public Node source;
    public Node target;
    public String label;
    public int sourceIndex;
    public int targetIndex;
    public boolean visible = false;
    public int height;

    @Override
    public String toString() {
	return label+"["+sourceIndex+"->" + targetIndex+"]";
    }


}
