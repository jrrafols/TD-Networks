import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

public class HoverWorldTDNet implements TDNet,Serializable{
    public static DecimalFormat df = new DecimalFormat("0.000");
	//General printing functions
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
    public static void print(double o){System.out.println(df.format(o));}
    public static void print_(double o){System.out.print(df.format(o));}
    public static void print(String o){System.out.println(o);}
    public static void print_(String o){System.out.print(o);}
    public static void print(double[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(df.format(p[i])+" ");
        }
        System.out.println("");
    }
    
	private class Node{
        public double value;
    }
    
    private class P_Node extends Node{
        public int n_inputs, n_actions, last_action;
        public double lambda, alpha, beta_val, delta, target_val, value_tilde, cond, kappa, rho;
        public int[] condition;
        public double[] termination, behavior;
        public double[] weights, traces;
        public Node target;
        public boolean debug = false;
        
        public P_Node(double val, int[] cond, double[] probs, double lam, double al, double[] term, Node targ, int ins, int acts){
            value = val;
            lambda = lam;
            condition = cond;
            termination = term;
            behavior = probs;
            target = targ;
            alpha = al;
            beta_val = 0;
            kappa = 1;
            rho = 1;
            
            n_inputs = ins;
            n_actions = acts;
            
            last_action = 0;
            weights = new double[n_actions*n_inputs];
            traces = new double[n_actions*n_inputs];
        }
        
        public double sigma(double y_val) {return 1 / (1 + Math.exp(-y_val));}
        public double max(double a, double b){
            if (a > b) return a;
            else return b;
        }
        
        public double bound(double y_val) {
            if (y_val > 1) return 1;
            else if (y_val < 0) return 0;
            else return y_val;
        }
        
        public double Rho(int action){
            return condition[action]/behavior[action];
        }
        
        public int Beta(int obs){
            if (termination[obs] == 1) return 1;
            else return 0;
        }
        
        public double dot(double[] vec_a, double[] vec_b){
            double sum = 0;
            for (int i = 0; i < vec_a.length; i++){ sum = sum + (vec_a[i] * vec_b[i]);}
            return sum;
        }
        
        public void updateTraces(double[] inputs, int action, int obs){
            //cond = condition[action];
            kappa = kappa * rho * (1 - beta_val) + 1;
            
            for (int i = 0; i < n_actions*n_inputs; i++){
                //traces[i] = cond * (lambda * (1 - beta_val) * traces[i] + inputs[i]);
                //traces[i] = rho * lambda * (1 - beta_val) * traces[i] + inputs[i];
                traces[i] = rho * lambda * (1 - beta_val) * traces[i] + kappa * inputs[i];
                //if (traces[i] > 1) traces[i] = 1;
            } 
            rho = Rho(action);
            beta_val = Beta(obs);
            target_val = target.value;
        }
        
        public void updateWeights(double[] inputs, int action){
            last_action = action;
            if (debug){
                print_("y: "); print_(value);
                print_(", beta: "); print_(beta_val);
                print_(", c: "); print_(condition[action]);
                print_(", rho: "); print_(rho);
            }
            
            //value_tilde = dot(inputs,weights);
            value_tilde = bound(dot(inputs,weights));
            //value_tilde = sigma(dot(inputs,weights));
            
            if (debug) { print_(", y~: "); print_(value_tilde); }
            if (debug) { print_(", z: "); print_(target_val); }            
            //delta = (beta_val * target_val) + ((1 - beta_val) * value_tilde) - value;
            delta = rho*((beta_val * target_val) + ((1 - beta_val) * value_tilde)) - value;
            if (debug) {print_(", delta: "); print(delta);}
            
            if (debug) {print_("Traces:\t\t"); print(traces);}
            if (debug) {print_("Old Weights:\t"); print(weights);}
            for (int i = 0; i < n_actions*n_inputs; i++){ weights[i] = weights[i] + alpha * delta * traces[i]; }
            if (debug) {print_("New Weights:\t"); print(weights);}
            
            //value = dot(inputs, weights);
            value = bound(dot(inputs, weights));
            //value = sigma(dot(inputs, weights));
            if (debug) {print_(", y: "); print_(value);}
        }
    }
	
	//Used for saving
	public int[] pos = new int[2];
	public int[] get_pos(){ return this.pos; }
	public void set_pos(int[] temp){ for (int i = 0; i < 2; i++) this.pos[i] = temp[i]; }
	
	public boolean debug = false;
	public int nodes, n_actions, n_inputs, a, n_obsbits;
	public double[] input_vec;
	Vector observations = new Vector();
	Vector predictions = new Vector();
	
	public HoverWorldTDNet(double lambda, double alpha, double[] probs) {
		n_obsbits = 3;
		nodes = (n_obsbits-1)*2;
		n_actions = 2;
		n_inputs = this.nodes + n_obsbits + 1;
		input_vec = new double[n_actions*n_inputs];

        int[] c1 = {1,0,0,0};
        int[] c2 = {0,1,0,0};
        int[] c3 = {0,0,1,0};
        int[] c4 = {0,0,0,1};
        
        double[] p1 = probs;
        
        double[] beta1 = {0,1,1,1};

        for (int i = 0; i < n_obsbits; i++){ observations.addElement(new Node()); }
        
        //double val, int[] cond, double[] probs, double lam, double al, double[] term, Node targ, int ins, int acts
        for (int i = 1; i < n_obsbits; i++){
            predictions.addElement(new P_Node(0.0, c1, p1, lambda, alpha, beta1, (Node)observations.elementAt(i), n_inputs, n_actions));
            predictions.addElement(new P_Node(0.0, c2, p1, lambda, alpha, beta1, (Node)observations.elementAt(i), n_inputs, n_actions));
        }
	}
    
    public String toString(){
        String s = "Observations: ";
        
        for (Enumeration e = observations.elements() ; e.hasMoreElements() ;) {
            s += ((Node)e.nextElement()).value + " ";
        }
        
        s+= "\nPredictions: \n\tN\tS";
        
        int counter = 0;
        for (Enumeration e = predictions.elements() ; e.hasMoreElements() ;) {
            if (counter%2 == 0) s+= "\n" + (counter/2+1)+"\t";
            s += df.format(((Node)e.nextElement()).value) + "\t";
            counter++;
            
        }
        
        return s;
    }
    
    //First line is the last action
    //Second line is the input vector
    //Remaining Lines are weights
    public String stateToString(){
        //String s = this.a+"\n";
        String s = "";
        
        for (int i = 0; i < n_inputs; i++){
            s += input_vec[i]+" ";
        }
        s+= "\n";
        
        for (Enumeration e = predictions.elements() ; e.hasMoreElements() ;) {
            //s += ((Node)e.nextElement()).weights + " ";   
            s+= "temp ";
        }
        
        return s;
    }
	
	public void debug_toggle(boolean b){
	   for (int i = 0; i < predictions.size(); i++){
            ((P_Node)predictions.elementAt(i)).debug = b;
        }
	}
	
	public void computeNet(int a, int o) {
	   if (debug){ print("\n"); print_("Old Inputs: "); print(input_vec); }
	   if (a == 2) a = 1; //Adjusting for the two action case
	   
	   //Set values of observation nodes
	    for (int i = 0; i < observations.size(); i++){
            if (o == i) ((Node)observations.elementAt(i)).value = 1;
            else ((Node)observations.elementAt(i)).value = 0;
        }
	    
	    //Update trace vectors
        for (int i = 0; i < predictions.size(); i++){ ((P_Node)predictions.elementAt(i)).updateTraces(input_vec, a, o); }
        
        //Construct input vector
        input_vec = new double[n_actions*n_inputs];
        int offset = a*n_inputs;
        input_vec[offset] = 1; //bias term
        //input_vec[0] = 1;
        input_vec[offset+o+1] = 1; //obs bit set to 1
        for(int i = 0; i < predictions.size(); i++){ input_vec[offset+1+n_obsbits+i] = ((Node)predictions.elementAt(i)).value; } //predictions
        
        if (debug){ print("\n"); print("Traces Updated"); print_("New Inputs: "); print(input_vec); }
        
        //Update weights
        for (int i = 0; i < predictions.size(); i++){
            if (debug) System.out.println("\nNode "+i+":");
            ((P_Node)predictions.elementAt(i)).updateWeights(input_vec,a);
        }
	}
	
	public void updateNet(int a, int o){
        if (a == 2) a = 1;
        //Construct input vector
        input_vec = new double[n_actions*n_inputs];
        int offset = a*n_inputs;
        //bias term
        input_vec[offset] = 1;
        //obs bit set to 1
        input_vec[offset+o+1] = 1;
        //predictions
        for(int i = 0; i < predictions.size(); i++){
            input_vec[offset+1+n_obsbits+i] = ((Node)predictions.elementAt(i)).value;
        }
        
        P_Node temp;
        
        for (int i = 0; i < predictions.size(); i++){
            if (debug) System.out.println("\nNode "+i+":");
            temp = (P_Node)predictions.elementAt(i);
            if (debug){ print_("Traces: "); print(temp.traces); }
            if (debug){ print_("Weights: "); print(temp.weights); }
            temp.value = temp.bound(temp.dot(input_vec, temp.weights));
        } 
	}
}