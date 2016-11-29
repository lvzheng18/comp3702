/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp3702_a2;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.DecimalFormat;
import java.util.Collections;
//import java.lang.*;

import problem.ArmConfig;
import problem.Obstacle;
import problem.ProblemSpec;
import tester.Tester;
import static tester.Tester.grow;


public class Comp3702_a2 {
        public static final double MIN_JOINT_ANGLE = -150.0 * Math.PI / 180.0;
	/** Maximum joint angle in radians */
	public static final double MAX_JOINT_ANGLE = 150 * Math.PI / 180.0;
        public static final double MAX_JOINT_STEP = 0.1 * Math.PI / 180.0;
        public static final double JOINT_STEP = 0.1 * Math.PI / 200.0;
	public static final double DEFAULT_MAX_ERROR = 1e-5;
	private double maxError;
	/** The workspace bounds, with allowable error. */
	private Rectangle2D lenientBounds;
        private static ArrayList<ArrayList<Double>> map = new ArrayList<ArrayList<Double>>();
        private static ArrayList<ArmConfig> array_cif = new ArrayList<ArmConfig>();
        private static ArrayList<Double> h = new ArrayList<Double>();
        private static Tester tester;
        private static boolean gripper = false; 
        private static int joint_num = 0;
        private static String init_joint_cif;
        private static double error = 0.025;
        private static ArmConfig goal_point;
        private static ArmConfig goal_point2;
        private static ArmConfig init_point;
        private static String gripper_cif = "";
        private static String solution_path = "";
        
    public static void main(String[] args) {
        double maxError = DEFAULT_MAX_ERROR;
        double d = 0;
        String problemPath = args[0];
        solution_path = args[1];
        String solutionPath = null;
        ArrayList<Integer> route = new ArrayList<Integer>();
        
        
        ArmConfig random_point;
        int i, j, if_run = 10;
        
        search s = null;
        
        tester = new Tester(maxError);
        try {
                tester.ps.loadProblem(problemPath);
        } catch (IOException e1) {
                System.out.println("FAILED: Invalid problem file");
                System.exit(1);
        }
        
        
        
        init_point = tester.ps.getInitialState();
        joint_num = init_point.getJointCount();
        if (joint_num == 0) {
            init_joint_cif = "";
            error = 0.023;
        } else {
            init_joint_cif = "";
            for (double angle : init_point.getJointAngles()) {
                init_joint_cif += ( " " + angle);
                
            }
            if (joint_num < 5) {
                error = 0.030;
            } else {
                error = 0.035;
            }
        }
        goal_point = tester.ps.getGoalState();
        
        gripper = tester.ps.gripper;
        
        if (gripper) {
            gripper_cif = " 0.03 0.03 0.03 0.03";
            goal_point2 = new ArmConfig(goal_point);
            
            String buffer10 = "";
            for (double angle : goal_point2.getJointAngles()) {
                buffer10 += ( " " + angle);
                
            }
            goal_point = new ArmConfig(goal_point2.getBaseCenter().getX() + " " + goal_point2.getBaseCenter().getY() + buffer10 + gripper_cif, gripper);
        }
        
        array_cif.add(new ArmConfig(init_point));
        array_cif.add(new ArmConfig(goal_point));
        init_map(init_point, goal_point);
        
        while(if_run > 0) {
            random_point = create_random_p();
            array_cif.add(random_point);            
            h.add(random_point.getBaseCenter().distance(goal_point.getBaseCenter()));
            map.add(new ArrayList<Double>());
            for (i = 0; i < array_cif.size() - 1; i++) {
                if (check_link(array_cif.get(i), random_point)) {
                    d = random_point.getBaseCenter().distance(array_cif.get(i).getBaseCenter());
                } else {
                    d = 0;
                }                
                map.get(i).add(d);
                map.get(map.size() - 1).add(d);                
            }
            map.get(map.size() - 1).add(0.0);
            
            
            
            if_run--; 
            
            if (if_run == 0) {
                s = new search (1, 2, map.size(), map, h); 
                if (s.get_route() != null) {
                    
                    outputs(s.get_route());
                    break;
                } else {
                    if_run = map.size();
                    
                }
            }            
        }
    }
    
    public static void outputs(ArrayList<Integer> x) {
        PrintWriter outf = null;
        ArrayList<ArmConfig> output_route = new ArrayList<ArmConfig>();
        ArrayList<ArmConfig> output_route2 = new ArrayList<ArmConfig>();
        ArrayList<ArmConfig> output_route_g1 = new ArrayList<ArmConfig>();
        ArrayList<ArmConfig> output_route_g2 = new ArrayList<ArmConfig>();
        ArmConfig c, c1, c2;
        error = 0.0004;
        int if_run = 1;
        int index = 0;
        double x1, y1;
        int if_small = 1;
        String angle_c = new String();
        List<Double> angleA;
        List<Double> angleB;
        
        double g1, g2, g3, g4;
        String temp_g;
        if (gripper) {
            output_route_g1.add(init_point);
            temp_g =  init_point.getBaseCenter().getX() + " " + init_point.getBaseCenter().getY() + init_joint_cif;
            ArmConfig tempc = new ArmConfig( temp_g + gripper_cif, gripper);
            output_route_g1.add(new ArmConfig(tempc));
            
            while(if_run == 1){
                
                if (output_route_g1.get(index).maxGripperDiff(output_route_g1.get(index + 1)) > 0.001) {
                    g1 = (output_route_g1.get(index).getGripperLengths().get(0) + output_route_g1.get(index + 1).getGripperLengths().get(0)) / 2.0;
                    g2 = (output_route_g1.get(index).getGripperLengths().get(1) + output_route_g1.get(index + 1).getGripperLengths().get(1)) / 2.0;
                    g3 = (output_route_g1.get(index).getGripperLengths().get(2) + output_route_g1.get(index + 1).getGripperLengths().get(2)) / 2.0;
                    g4 = (output_route_g1.get(index).getGripperLengths().get(3) + output_route_g1.get(index + 1).getGripperLengths().get(3)) / 2.0;
                    output_route_g1.add(index + 1,  new ArmConfig(temp_g + " " + g1 + " " + g2 + " " + g3 + " " + g4, gripper));    
                } else {
                    index++;
                }
                
                if ((index + 1) == output_route_g1.size()) {
                    break;
                } 
            }
            
            if_run  = 1;
            index = 0;
            output_route_g2.add(new ArmConfig(goal_point));
            output_route_g2.add(new ArmConfig(goal_point2));
            
            String goal_joint_cif = "";
            for (int n = 0; n < goal_point.getJointAngles().size(); n++) {
                goal_joint_cif += " " + goal_point.getJointAngles().get(n);
            
            }
            
            temp_g =  goal_point.getBaseCenter().getX() + " " + goal_point.getBaseCenter().getY() + goal_joint_cif;
            
            while (if_run == 1) {
                if (output_route_g2.get(index).maxGripperDiff(output_route_g2.get(index + 1)) > 0.001) {
                    g1 = (output_route_g2.get(index).getGripperLengths().get(0) + output_route_g2.get(index + 1).getGripperLengths().get(0)) / 2.0;
                    g2 = (output_route_g2.get(index).getGripperLengths().get(1) + output_route_g2.get(index + 1).getGripperLengths().get(1)) / 2.0;
                    g3 = (output_route_g2.get(index).getGripperLengths().get(2) + output_route_g2.get(index + 1).getGripperLengths().get(2)) / 2.0;
                    g4 = (output_route_g2.get(index).getGripperLengths().get(3) + output_route_g2.get(index + 1).getGripperLengths().get(3)) / 2.0;
                    output_route_g2.add(index + 1,  new ArmConfig(temp_g + " " + g1 + " " + g2 + " " + g3 + " " + g4, gripper));    
                } else {
                    index++;
                }
                
                if ((index + 1) == output_route_g2.size()) {
                    break;
                }            
            }
        }
        
        
        index = 0;
        for (int i = 0; i < x.size(); i++) {
            output_route.add(new ArmConfig(array_cif.get(x.get(i)))); 
        }
        
        /*cut route*/
        if_run = 1;
        
        
        while(if_run == 1) {
            if (output_route.get(index).getBaseCenter().distance(output_route.get(index + 1).getBaseCenter()) > 0.001) {
                x1 = (output_route.get(index).getBaseCenter().getX() + output_route.get(index + 1).getBaseCenter().getX())/2.0;
                y1 = (output_route.get(index).getBaseCenter().getY() + output_route.get(index + 1).getBaseCenter().getY())/2.0;
                output_route.add(index + 1,new ArmConfig(x1 + " " + y1, false));
            }  else {
                index++;
            }
            
            if ((index + 1) == output_route.size()) {
                break;
            } 
        
        }
        
        
        if ((joint_num != 0) && (output_route.size() > 2)) {
            if (!gripper) {
                if_run = 1;
                index = 1;
                output_route2.add(new ArmConfig(output_route.get(0)));
                int extra_index2 = 8;
                /*add angle*/
                while (if_run == 1) {
                    if ((output_route.size() - output_route2.size()) >= 9) {
                        extra_index2 = 8;
                    } else {
                        extra_index2 = output_route.size() - output_route2.size() - 1;
                        if ((output_route2.size()) == output_route.size()) { /*change !!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
                            if_run = 0;
                            break;
                        }
                    }
                    c1 = new ArmConfig(output_route.get(index).getBaseCenter().getX() + " " + output_route.get(index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                    c2 = new ArmConfig(output_route.get(index + extra_index2).getBaseCenter().getX() + " " + output_route.get(index + extra_index2).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);

                    if (!if_vaild_p(c2)) {
                        while (!if_vaild_p(c2) || !check_angle(new ArmConfig(c1), new ArmConfig(c2) )) { /*stop here*/
                            
                            init_joint_cif = "";
                            for (int i = 0; i < joint_num; i++) {
                                init_joint_cif += " " + ((Math.random() -0.5) * 2 * MAX_JOINT_ANGLE );
                            }

                            
                            c2 = new ArmConfig(output_route.get(index + extra_index2).getBaseCenter().getX() + " " + output_route.get(index + extra_index2).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                            
                        }
                        c1 = new ArmConfig(output_route.get(index).getBaseCenter().getX() + " " + output_route.get(index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                    }
                    output_route2.add(new ArmConfig(c1));

                    

                    index++;
                    
                }
            } else if (gripper){
                output_route2 = add_angle_gripper(output_route);
            
            }
            
            if_run = 1;
           
            
            //index = output_route2.size() - 1;
            ArrayList<ArmConfig> op0 = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> opup = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> opdown = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> opright = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> opleft = new ArrayList<ArmConfig>();
            
            ArrayList<ArmConfig> op_return0 = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> op_return1 = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> op_return2 = new ArrayList<ArmConfig>();
            ArrayList<ArmConfig> op_return3 = new ArrayList<ArmConfig>();
            
            ArmConfig bufferx;
            int[] which_bad = new int[4];
            which_bad[0] = 0;
            which_bad[1] = 0;
            which_bad[2] = 0;
            which_bad[3] = 0;
            
            int good_one = 0;
            
            op0.add(new ArmConfig(output_route2.get(output_route2.size() - 1)));
            opup.add(new ArmConfig(output_route2.get(output_route2.size() - 1)));
            opdown.add(new ArmConfig(output_route2.get(output_route2.size() - 1)));
            opright.add(new ArmConfig(output_route2.get(output_route2.size() - 1)));
            opleft.add(new ArmConfig(output_route2.get(output_route2.size() - 1)));
            
            String current_angle_cfg = "";
            String goal_an_cfg = "";
            for (int i = 0; i < joint_num; i++) {
                current_angle_cfg += " " + output_route2.get(output_route2.size() - 1).getJointAngles().get(i);
                goal_an_cfg += " " + goal_point.getJointAngles().get(i);
            }
            
            for (int o = 0; o < 50; o++) {
                
                if (which_bad[0] == 0) {
                    opup.add(new ArmConfig(opup.get(opup.size() - 1).getBaseCenter().getX() + " " + (opup.get(opup.size() - 1).getBaseCenter().getY() + 0.00095) + current_angle_cfg + gripper_cif, gripper)); 
                    bufferx = new ArmConfig(opup.get(opup.size() - 1).getBaseCenter().getX() + " " + (opup.get(opup.size() - 1).getBaseCenter().getY()) + goal_an_cfg + gripper_cif, gripper);
                    op_return0.add(new ArmConfig(bufferx));
                    if (!if_vaild_p(opup.get(opup.size() - 1)) || !if_vaild_p(bufferx)) {
                        which_bad[0] = 1;
                    }
                }
                
                if (which_bad[1] == 0) {
                    opdown.add(new ArmConfig(opdown.get(opdown.size() - 1).getBaseCenter().getX() + " " + (opdown.get(opdown.size() - 1).getBaseCenter().getY() - 0.00095) + current_angle_cfg + gripper_cif, gripper));
                    bufferx = new ArmConfig(opdown.get(opdown.size() - 1).getBaseCenter().getX() + " " + (opdown.get(opdown.size() - 1).getBaseCenter().getY()) + goal_an_cfg + gripper_cif, gripper);
                    op_return1.add(new ArmConfig(bufferx));
                    if (!if_vaild_p(opdown.get(opdown.size() - 1)) || !if_vaild_p(bufferx)) {
                        which_bad[1] = 1;
                    }
                }
                
                if (which_bad[2] == 0) { 
                    opright.add(new ArmConfig((opright.get(opright.size() - 1).getBaseCenter().getX() +  0.00095) + " " + (opright.get(opright.size() - 1).getBaseCenter().getY()) + current_angle_cfg + gripper_cif, gripper));
                    bufferx = new ArmConfig(opright.get(opright.size() - 1).getBaseCenter().getX() + " " + (opright.get(opright.size() - 1).getBaseCenter().getY()) + goal_an_cfg + gripper_cif, gripper);
                    op_return2.add(new ArmConfig(bufferx));
                    if (!if_vaild_p(opright.get(opright.size() - 1)) || !if_vaild_p(bufferx)) {
                        which_bad[2] = 1;
                    }
                }
                
                if (which_bad[3] == 0) { 
                    opleft.add(new ArmConfig((opleft.get(opleft.size() - 1).getBaseCenter().getX() - 0.00095) + " " + (opleft.get(opleft.size() - 1).getBaseCenter().getY() + 0.00095) + current_angle_cfg + gripper_cif, gripper));
                    bufferx = new ArmConfig(opleft.get(opleft.size() - 1).getBaseCenter().getX() + " " + (opleft.get(opleft.size() - 1).getBaseCenter().getY()) + goal_an_cfg + gripper_cif, gripper);
                    op_return3.add(new ArmConfig(bufferx));
                    if (!if_vaild_p(opleft.get(opleft.size() - 1)) || !if_vaild_p(bufferx)) {
                        which_bad[3] = 1;
                    }
                }    
            }
            
            
            if_run = 1;
            
            ArmConfig up1, down1, left1, right1;
            String c_up = opup.get(opup.size() - 1).getBaseCenter().getX() + " "  + opup.get(opup.size() - 1).getBaseCenter().getY();
            String c_down = opdown.get(opdown.size() - 1).getBaseCenter().getX() + " "  + opdown.get(opdown.size() - 1).getBaseCenter().getY();
            String c_right = opright.get(opright.size() - 1).getBaseCenter().getX() + " "  + opright.get(opright.size() - 1).getBaseCenter().getY();
            String c_left = opleft.get(opleft.size() - 1).getBaseCenter().getX() + " "  + opleft.get(opleft.size() - 1).getBaseCenter().getY();
            
            
            while(if_run == 1) {
                                        
                init_joint_cif = "";
                for (int i = 0; i < joint_num; i++) {
                    init_joint_cif += " " + ((Math.random() -0.5) * 2 * MAX_JOINT_ANGLE );
                }
                
                if (check_angle(op0.get(op0.size() - 1), goal_point)) {
                    good_one = 5;
                    if_run = 0;
                    break;
                }
                c = new ArmConfig(goal_point.getBaseCenter().getX() + " " + goal_point.getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                if (check_angle(op0.get(op0.size() - 1), c)) {
                    op0.add(new ArmConfig(c));                    
                }  
                                                
                if (which_bad[0] == 0) {
                    if (check_angle(opup.get(opup.size() - 1), goal_point)) {
                        good_one = 0;
                        if_run = 0;
                        break;
                    }
                    
                    c = new ArmConfig(c_up + init_joint_cif + gripper_cif, gripper);
                    if (check_angle(opup.get(opup.size() - 1), c)) {
                        opup.add(new ArmConfig(c));                    
                    }                    
                }
                
                if (which_bad[1] == 0) {
                    if (check_angle(opdown.get(opdown.size() - 1), goal_point)) {
                        good_one = 1;
                        if_run = 0;
                        break;
                    }
                    
                    c = new ArmConfig(c_up + init_joint_cif + gripper_cif, gripper);
                    if (check_angle(opdown.get(opdown.size() - 1), c)) {
                        opdown.add(new ArmConfig(c));                    
                    }                    
                }
                
                if (which_bad[2] == 0) {
                    if (check_angle(opright.get(opright.size() - 1), goal_point)) {
                        good_one = 2;
                        if_run = 1;
                        break;
                    }
                    
                    c = new ArmConfig(c_up + init_joint_cif + gripper_cif, gripper);
                    if (check_angle(opright.get(opright.size() - 1), c)) {
                        opright.add(new ArmConfig(c));                    
                    }                    
                }
                
                if (which_bad[3] == 0) {
                    if (check_angle(opleft.get(opleft.size() - 1), goal_point)) {
                        good_one = 3;
                        if_run = 0;
                        break;
                    }
                    
                    c = new ArmConfig(c_up + init_joint_cif + gripper_cif, gripper);
                    if (check_angle(opleft.get(opleft.size() - 1), c)) {
                        opleft.add(new ArmConfig(c));                    
                    }                    
                }
                
                
            
            }
            //
            ///
            
            if(good_one == 5) {
                output_route2.addAll(op0);
            } else if (good_one == 0) {
                output_route2.addAll(opup);
                Collections.reverse(op_return0);
                output_route2.addAll(op_return0);
            } else if (good_one == 1) {
                output_route2.addAll(opdown);
                Collections.reverse(op_return1);
                output_route2.addAll(op_return1);
            } else if (good_one == 2) {
                output_route2.addAll(opright);
                Collections.reverse(op_return2);
                output_route2.addAll(op_return2);
            } else if (good_one == 3) {
                output_route2.addAll(opleft);
                Collections.reverse(op_return3);
                output_route2.addAll(op_return3);
            }
            
            
            ///
            output_route2.add(goal_point);
            index = 0;
            
            angleA = new ArrayList<Double>(output_route2.get(index).getJointAngles());
            angleB = new ArrayList<Double>(output_route2.get(index + 1).getJointAngles()); /*bug need to be change!!!!!!!!!!!!!!!!!!!*/
            
            /*divide angle*/

            
            if_run = 1;
            
            while (if_run == 1) {
                double buffer = 0.0;
                
                if_small = 1;
                angle_c = "";
                
                for (int i = 0; i < joint_num; i++) {
                    
                    if (Math.abs(angleA.get(i) - angleB.get(i)) > MAX_JOINT_STEP) {
                        
                        if_small = 0;                        
                        
                        if (angleA.get(i) < angleB.get(i)) {
                            buffer =  JOINT_STEP + angleA.get(i);
                        } else {
                            buffer =   angleA.get(i) - JOINT_STEP;
                        }
                        
                    } else {
                        buffer = angleA.get(i);
                    }
                    angle_c += " " + buffer;
                }
                
                if (if_small == 0) {
                    c = new ArmConfig(output_route2.get(index).getBaseCenter().getX() + " " + output_route2.get(index).getBaseCenter().getY() + angle_c + gripper_cif, gripper);
                    output_route2.add(index + 1, c);

                } else {
                    index++;
                    if ((index + 1) == output_route2.size()) {
                        if_run = 0;
                        break;
                    }
                    
                }
                
                angleA = new ArrayList<Double>(output_route2.get(index).getJointAngles());
                angleB = new ArrayList<Double>(output_route2.get(index + 1).getJointAngles());
                
                
            }
            
        }else if (output_route.size() <= 2) {
            output_route2 = output_route;         
        }
        
        try {
            outf = new PrintWriter(solution_path, "UTF-8");
        }
        catch (
            Exception e){System.out.println("fail to open file");
        }
        
        if (gripper) {
            output_route2.remove(0);
            output_route_g1.addAll(output_route2);
            output_route2 = output_route_g1;
            output_route2.addAll(output_route_g2);
 
        }
        
        if (joint_num == 0) {
            outf.println((output_route.size() -1));
            for (ArmConfig i : output_route) {
                outf.println(i.getBaseCenter().getX() + " " + i.getBaseCenter().getY());
            }
        } else {
            outf.println((output_route2.size() -1));
            for (ArmConfig i : output_route2) {
                outf.print(i.getBaseCenter().getX() + " " + i.getBaseCenter().getY());
                for (int k = 0; k < joint_num; k++) {
                    outf.print(" " + i.getJointAngles().get(k));
                }
                
                if (gripper) {
                    
                    
                    for (int j = 0; j < 4; j++) {
                        
                        outf.print(" " + i.getGripperLengths().get(j)); 
                       
                    }
                    
                
                }
                
                outf.print("\n");
            }
        }
        
        
        outf.close();
    
    }
    
    public static ArrayList<ArmConfig> add_angle_gripper(ArrayList<ArmConfig> route_without_angle) {
        int if_run = 1;
        int index = 1;
        int extra_index = 15;
        ArrayList<ArmConfig> route_with_angle = new ArrayList<ArmConfig>();
        route_with_angle.add(new ArmConfig(init_point));
        ArmConfig c1, c2;
        
        int try1 = 0;
        while (if_run == 1) {
                if ((route_without_angle.size() - route_with_angle.size()) >= 16) {
                    extra_index = 15;
                } else {
                    extra_index = route_without_angle.size() - route_with_angle.size() - 1;
                    if ((route_with_angle.size()) == route_without_angle.size()) { /*change !!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
                        if_run = 0;
                        break;
                    }
                }
                
                
                c1 = new ArmConfig(route_without_angle.get(index).getBaseCenter().getX() + " " + route_without_angle.get(index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                c2 = new ArmConfig(route_without_angle.get(index + extra_index).getBaseCenter().getX() + " " + route_without_angle.get(index + extra_index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                
                if (!if_vaild_p(c2)) {
                    while (!if_vaild_p(c2) || !check_angle(new ArmConfig(c1), new ArmConfig(c2) )) { /*stop here*/
                        
                        init_joint_cif = "";
                        for (int i = 0; i < joint_num; i++) {
                            init_joint_cif += " " + ((Math.random() -0.5) * 2 * MAX_JOINT_ANGLE );
                        }
                        
                        c2 = new ArmConfig(route_without_angle.get(index + extra_index).getBaseCenter().getX() + " " + route_without_angle.get(index + extra_index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                        
                    }
                    c1 = new ArmConfig(route_without_angle.get(index).getBaseCenter().getX() + " " + route_without_angle.get(index).getBaseCenter().getY() + init_joint_cif + gripper_cif, gripper);
                }
                
                route_with_angle.add(new ArmConfig(c1));
               
                
                
                index++;
                
            }
        
        
        return route_with_angle;
    
    }
    
    public static boolean check_angle(ArmConfig A, ArmConfig B) {
        double step = Math.PI / 360.0;
        List<Double> angleA = new ArrayList<Double> (A.getJointAngles());
        List<Double> angleB = new ArrayList<Double> (B.getJointAngles());
        ArmConfig temp;
        String angle_c = new String();
        String A_location = A.getBaseCenter().getX() + " " + A.getBaseCenter().getY();
        
        int if_run = 1;
        
        while(if_run == 1) {
            if_run = 0;
            angle_c = "";
            for (int i = 0; i < joint_num; i++) {
                
                if (Math.abs(angleA.get(i) - angleB.get(i)) > step) {
                    if (angleA.get(i) < angleB.get(i)) {
                        angleA.set(i, step + angleA.get(i));
                    } else {
                        angleA.set(i, angleA.get(i) - step);
                    }
                    
                    if_run = 1;
                }
               
                angle_c += " " + angleA.get(i);                
            }
            
            temp = new ArmConfig(A_location + angle_c + gripper_cif, gripper);
            
            if (!if_vaild_p(temp)) {
                
                return false;
            }
            
        
        }
        
        
        return true;
    }
    
    public static void init_map(ArmConfig A, ArmConfig B) {
        double distance = 0.0;   
        double x = 0.2;
        double y = 0.2;
        double d = 0.0;
        ArmConfig c;
        map.clear();
        if (check_link(A, B)) {
            distance = A.getBaseCenter().distance(B.getBaseCenter());
        }
        map.add(new ArrayList<Double>());
        map.add(new ArrayList<Double>());
        map.get(0).add(0.0);
        map.get(0).add(distance);
        map.get(1).add(distance);
        map.get(1).add(0.0);
        h.add(1.0);
        h.add(0.0);
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                c = new ArmConfig(x + " " + y, false);
                
                
                
                if (if_vaild_p(c)) {
                    
                    array_cif.add(c);            
                    h.add(c.getBaseCenter().distance(B.getBaseCenter()));
                    map.add(new ArrayList<Double>());
                    for (int k = 0; k < array_cif.size() - 1; k++) {
                        if (check_link(array_cif.get(k), c)) {
                            d = c.getBaseCenter().distance(array_cif.get(k).getBaseCenter());
                        } else {
                            d = 0;
                        }                
                        map.get(k).add(d);
                        map.get(map.size() - 1).add(d);                
                    }
                    map.get(map.size() - 1).add(0.0);
                }
                x += 0.2;
            }
            x = 0.2;
            y += 0.2;
        }
        
       
        
    }
    
    public static boolean check_link(ArmConfig A, ArmConfig B) {
        Line2D test_line = new Line2D.Double(A.getBaseCenter(), B.getBaseCenter());
        List<Obstacle> ObsList = tester.ps.getObstacles();
        Rectangle2D lenientRect;
        
        for (Obstacle o : ObsList) {
            lenientRect = tester.grow(o.getRect(), error);
            if (test_line.intersects(lenientRect)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean if_vaild_p(ArmConfig p) {
       
        if ((!tester.hasSelfCollision(p)) && (!tester.hasCollision(p, tester.ps.getObstacles())) && (tester.fitsBounds(p))) {
            return true;
        } else {
            return false;
        }
    }
    
    public static ArmConfig create_random_p () {
        double x;
        double y;
        
        ArmConfig c;
        do{
            x = Math.random();
            y = Math.random();
            /*if (joint_num != 0) {
                for (int i = 0; i < joint_num; i++) {
                    joint_angle += " " + ((Math.random() -0.5) * MAX_JOINT_ANGLE);
                
                }

                
            }*/
            c = new ArmConfig(x + " " + y, false);        
        }while(!if_vaild_p(c));
        
        return c;
    }

    
    
    
    
}
