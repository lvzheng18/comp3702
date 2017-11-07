/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solver;

/**
 *
 * @author lvzheng
 */
import java.util.ArrayList;


public class Node implements Comparable<Node> {
    int type = 0;
    int order_num = 0;
    double average_reward = 0;
    
    public Node (int type, int order, double reward) {
        this.type = type;
        this.order_num = order;
        this.average_reward = reward;
    
    }
    
    public int get_type () {
        return this.type;
    }
    
    public int get_order_num () {
        return this.order_num;
    }
    
    public double get_average_reward () {
        return this.average_reward;
    }
    
    @Override
	public int compareTo(Node anotherNode) {
	    return this.average_reward < anotherNode.average_reward ? 1 : -1 ;
	}
    
    @Override
	public String toString() {
		return ("type " + this.type + " order: " + this.order_num +
				 " av_reward " + this.average_reward);
	}
    
    
}
