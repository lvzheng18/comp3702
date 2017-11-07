/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp3702_a2;

import java.io.PrintWriter;
import java.util.*;

/**
 *
 * @author lvzheng
 */
public class search {
	
	Node first_node;
	Node poll_node;
	List<ArrayList<Double>> CostList;
	ArrayList<Double> sum_cost;
	PriorityQueue<Node> queue;
	ArrayList<Node> finalPath;
        ArrayList<Integer> route;
        int find_it = 1;
	
	public search (int start_id, int target_id, int size, ArrayList<ArrayList<Double>> matrix, ArrayList<Double> h) {
		
		sum_cost = new ArrayList<Double>();
		queue = new PriorityQueue<>();
		finalPath = new ArrayList<>();
                route = new ArrayList<Integer>();
		CostList = new ArrayList<ArrayList<Double>>();
		first_node = new Node(start_id, 0, 0, h.get(start_id - 1));
                
		
		for(int i = 0; i < size + 1; i++) sum_cost.add((double) 0);

		queue.add(first_node);
		poll_node = queue.poll();
		while(poll_node.get_id() != target_id) {
			if (poll_node.sum_cost < 6.0) {                        
                            int currIndex = poll_node.get_id();
                            sum_cost.set(currIndex, poll_node.get_sum_cost() + sum_cost.get(currIndex)); 

                            for(int i = 0; i < size; i++) {

                                    if(matrix.get(poll_node.get_id() - 1).get(i) != 0) {
                                            double cost = sum_cost.get(currIndex) + matrix.get(poll_node.get_id() - 1).get(i);
                                            Node childNode = new Node(i + 1, currIndex, cost, h.get(i));

                                            childNode.set_pre_node(poll_node);
                                            
                                            if(childNode.get_id() != poll_node.get_pre_id()) {
                                                queue.add(childNode);
                                            }
                                            

                                    }

                            }
                        }
                        
                        if (queue.isEmpty()) {
                            find_it = 0;
                            break;
                        }
			poll_node = queue.poll();	
		}
		
                if (find_it == 1) {
                    finalPath.add(poll_node);

                    while(poll_node.get_pre_id() != 0) {
                            poll_node = poll_node.get_pre_node();
                            finalPath.add(poll_node);
                    }

                    Collections.reverse(finalPath);
                    for(int i = 0; i < finalPath.size(); i++) {
                            route.add(finalPath.get(i).get_id() - 1);
                    }
                }


	}
        
        public ArrayList<Integer> get_route() {
            if (find_it == 1) {
                return route;
            } else {
                return null;
            }
            
        }
	
}
