package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import problem.Store;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Store store;
    private List<Matrix> probabilities;
	private double[][] R_value;
    double[][] totalReward;
    
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		store = spec.getStore();
        probabilities = spec.getProbabilities();
	}
	
	public void doOfflineComputation() {
	    // TODO Write your own code here.
        int storeCapacity = store.getCapacity();
        int storeType = store.getMaxTypes();
        int miss = 0;
        
        double factor = spec.getDiscountFactor();
        
        R_value = new double[store.getMaxTypes()][store.getCapacity() + 1];
        totalReward = new double[store.getMaxTypes()][store.getCapacity() + 1];
        double[][] buffer = new double[store.getMaxTypes()][store.getCapacity() + 1];
        List<Double> price = spec.getPrices();
        
        
        
        
        for (int i = 0; i <  storeType; i++) {
            for (int j = 0; j < (storeCapacity + 1); j++) {
                for (int k = 0; k < (storeCapacity + 1); k++) {
                    R_value[i][j] += probabilities.get(i).get(j, k) * Math.min(k, j) * price.get(i) * 0.75;
                    if (j < k) {
                        miss = k - j;
                        R_value[i][j] -= probabilities.get(i).get(j, k) * miss * price.get(i) * 0.25;
                    }
                }
            }
        }
        
        for (int i = 0; i < R_value.length; i++) {
            totalReward[i] = R_value[i].clone();
        }
        
        int ifRun = 10000;
        double TV = 0;
        
        //j stock; k how much can I sale
        while (ifRun > 0) {
            for (int i = 0; i <  storeType; i++) {
                for (int j = 0; j < (storeCapacity + 1); j++) {
                    for (int k = 0; k < (storeCapacity + 1); k++) {
                        if (k == 0) {
                            TV = probabilities.get(i).get(j, k) * totalReward[i][j - Math.min(k, j)];
                        } else {
                            TV = Math.max(TV, (probabilities.get(i).get(j, k) * totalReward[i][j - Math.min(k, j)]));
                        }
                    }
                    
                    buffer[i][j] = R_value[i][j] + factor * TV;

                }
            }
            
            double maxdiff = 0;
            
            for (int i = 0; i <  storeType; i++) {
                for (int j = 0; j < (storeCapacity + 1); j++) {
                    maxdiff = Math.abs(buffer[i][j] - totalReward[i][j]);
                }
            }
            
            for (int i = 0; i < buffer.length; i++) {
                totalReward[i] = buffer[i].clone();
            }
            
            //System.out.println(ifRun);
            if (maxdiff < 1e-4) {
                break;
            }
            
            ifRun--;
        }
        
        
	}
	
	public List<Integer> generateStockOrder(List<Integer> stockInventory,
											int numWeeksLeft) {
        int storeType = store.getMaxTypes();
		int[] itemOrder = new int[store.getMaxTypes()];   
        int[] itemreturn = new int[store.getMaxTypes()];  
        
        int maxOrder = store.getMaxPurchase();
        int maxReturn = store.getMaxReturns();
        int index = -1;
        double eran = 0;
        int if_run = 1;
        int count = 0;
        
        double ave_reward = 0;
        
        PriorityQueue<Node> best_action_buy = new PriorityQueue<Node>();
        PriorityQueue<Node> best_action_return = new PriorityQueue<Node>();

		// Example code that buys one of each item type.
        // TODO Replace this with your own code.

		int totalItems = 0;
		for (int i : stockInventory) {
			totalItems += i;
		}
		
		int totalOrder = 0;
        int totalReturn = 0;
        
        for (int i = 0; i < storeType; i++) {
            for (int j = 1; j <= maxOrder; j++) {
                if (((stockInventory.get(i) + j) <= store.getCapacity())) {
                    if (totalReward[i][stockInventory.get(i) + j] - totalReward[i][stockInventory.get(i)] > 0) {
                        ave_reward = (totalReward[i][stockInventory.get(i) + j] - totalReward[i][stockInventory.get(i)]) / j;
                        best_action_buy.add(new Node(i, j, ave_reward));   
                    }
                }
            }

            for (int k = 1; k <= maxReturn; k++) {
                if (stockInventory.get(i) >= k) {
                    if ((totalReward[i][stockInventory.get(i) - k] - totalReward[i][stockInventory.get(i)] - 
                            0.25 * k * spec.getPrices().get(i)) > 0) {
                        
                        ave_reward = (totalReward[i][stockInventory.get(i) - k] - 
                                totalReward[i][stockInventory.get(i)] - 0.5 * k * spec.getPrices().get(i)) / k;
                        
                        best_action_return.add(new Node(i, k, ave_reward));   
                        
                    }
                }
            }
        }
        
        
        while (best_action_buy.size() > 0) {
            Node best_order;
            best_order = best_action_buy.poll();
            
            if (((best_order.order_num + totalItems) <= store.getCapacity())
                && ((best_order.order_num + totalOrder) <= store.getMaxPurchase())){
                        
                itemOrder[best_order.type] += best_order.order_num;
                totalOrder += best_order.order_num;
                totalItems += best_order.order_num;

                
            } else if (((best_order.order_num + totalItems - store.getMaxReturns()) <= store.getCapacity())
                && ((best_order.order_num + totalOrder) <= store.getMaxPurchase())) {
                
                int run = 1;
                PriorityQueue<Node> best_buffer_return = new PriorityQueue<Node>(best_action_return);
                
                while((best_buffer_return.size() > 0) && (run == 1)) {
                    Node best_return = best_buffer_return.poll();
                    if ((best_return.order_num <= (best_order.order_num + totalItems - store.getCapacity())) && 
                            ((totalReturn + best_return.order_num) < store.getMaxReturns()) &&
                            (itemreturn[best_return.type] == 0) && (itemOrder[best_return.type] == 0)) {
                        
                        itemreturn[best_return.type] =  best_return.order_num;
                        totalReturn += best_return.order_num;
                        
                        itemOrder[best_order.type] += best_order.order_num;
                        totalOrder += best_order.order_num;
                        totalItems += (best_order.order_num - best_return.order_num);
                        
                        run = 0;
                        
                    }
                
                }
                
                run = 1;
                
            
            }
            
            if (totalItems >= store.getCapacity() ||
                totalOrder >= store.getMaxPurchase()) {
                count = 0;
                break;

            }
            
        }
        

		/*for (int i = 0; i < store.getMaxTypes(); i++) {
			if (totalItems >= store.getCapacity() ||
                totalOrder >= store.getMaxPurchase()) {
				itemOrders.add(0);
			} else {
				itemOrders.add(1);
				totalOrder ++;
				totalItems ++;
			}
			itemReturns.add(0);
		}*/


		// combine orders and returns to get change for each item type
		List<Integer> order = new ArrayList<Integer>(store.getMaxPurchase());
		for(int i = 0; i < store.getMaxTypes(); i++) {
			order.add(itemOrder[i] - itemreturn[i]);
		}
        
        best_action_buy.clear();
        best_action_return.clear();
        
		return order;
	}

}
