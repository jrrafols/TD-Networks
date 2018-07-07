import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

//Off-Policy, Features
public class ColorWorldTDNet3 implements Serializable{
	///////////////////////////////////////////////////////////
    //General printing functions
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
    public static void print(double o){System.out.println(o);}
    public static void print_(double o){System.out.print(o);}
    public static void print(String o){System.out.println(o);}
    public static void print_(String o){System.out.print(o);}
    public static void print(double[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    public static void print(int[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    ///////////////////////////////////////////////////////////
	
	public static DecimalFormat df = new DecimalFormat("0.000");
	
	public static String[] node_names = new String[] {"Leap\t\t", "L-Leap\t\t","R-Leap\t\t","Leap-L-Leap\t","Leap-R-Leap\t","F\t\t","L\t\t","R\t\t","Wander\t\t"};
	
	//Used for saving
	public int[] pos = new int[3];
	public int[] get_pos(){ return this.pos; }
	public void set_pos(int[] temp){ for (int i = 0; i < 3; i++) this.pos[i] = temp[i]; }
	
	public int nodes, a, inputs, numFeatures;

	public double lambda, alpha;

    public int[] current_features;

	public double[] x,y, z, kappa, rho, rho0, rho1, rho2, beta, beta1, beta2, ytilde, delta;
	
	public double[][] W, e, W_accumulate;
	
	public Vector[] traceMem;
	
	public ColorWorldTDNet3(double alpha, double lambda, int numFeatures, double[] behavior) {
		this.nodes = 45;
		//this.inputs = (numFeatures+7)*3; //numFeatures + 6 obs bits + bias
		this.inputs = numFeatures+7; //numFeatures + 6 obs bits + bias
		this.numFeatures = numFeatures;
		
		traceMem = new Vector[nodes];
		for (int i =0; i < nodes; i++){ traceMem[i] = new Vector(); }
		current_features = new int[1];
		
		
		this.lambda = lambda;
		this.a = 0;
		this.W = new double[nodes][inputs];
		this.W_accumulate = new double[nodes][inputs];
		
		this.e = new double[nodes][inputs];
		this.x = new double[inputs];
		this.rho = new double[nodes];
		this.beta = new double[nodes];
		this.z = new double[nodes];
		this.y = new double[nodes];
        this.kappa = new double[nodes];
        for (int i = 0; i < nodes; i++){kappa[i] = 1;}
        
        
        this.delta = new double[this.nodes];
        
        this.beta1 = new double[this.nodes];
		this.beta2 = new double[this.nodes];
		for (int i = 0; i < this.nodes; i++){
			this.beta1[i] = 1;
			this.beta2[i] = 1;
		}
		for(int j = 0; j < 5; j++){
			this.beta1[j] = 0;
			this.beta1[3*5+j] = 0;
			this.beta1[4*5+j] = 0;
		}
		for(int i = 0; i < 5; i++){
			this.beta1[8*5+i] = 0.5;
		}
		
		this.rho0 = new double[this.nodes];
		this.rho1 = new double[this.nodes];
		this.rho2 = new double[this.nodes];
		
		for(int j = 0; j < 5; j++){
			//F
			this.rho0[j] = 1.0/behavior[0];
			this.rho0[3*5+j] = 1.0/behavior[0];
			this.rho0[4*5+j] = 1.0/behavior[0];
			this.rho0[5*5+j] = 1.0/behavior[0];
			
			//L
			this.rho2[5+j] = 1.0/behavior[2];
			this.rho2[6*5+j] = 1.0/behavior[2];
			//R
			this.rho1[2*5+j] = 1.0/behavior[1];
			this.rho1[7*5+j] = 1.0/behavior[1];
			
			//Wander
			this.rho0[8*5+j] = (1.0/3.0)/behavior[0];
			this.rho1[8*5+j] = (1.0/3.0)/behavior[1];
			this.rho2[8*5+j] = (1.0/3.0)/behavior[2];
		}
		
		this.alpha = alpha;
	}

    public String toString(){
        String s = "\t\t    1     2     3     4     5\n";
        for (int i = 0; i < 9; i++){
            s+= node_names[i]+ ": ";
            for (int j = 0; j < 5; j++){
                s+= df.format(y[(i*5)+j]) + " ";
            }
            s+= "\n";
		}
        return s;
    }
    
    //First line is the last action
    //Second line is the input vector
    //Remaining Lines are weights
    public String stateToString(){
        String s = this.a+"\n";
        
        for (int i = 0; i < inputs; i++){
            s += x[i]+" ";
        }
        s+= "\n";
        
        for (int i = 0; i < nodes; i++){
            for (int j = 0; j < inputs; j++){
                s += W[i][j] + " ";
            }
            s+="\n";
        }
        
        return s;
    }

	public double sigma(double y_val) {return 1 / (1 + Math.exp(-y_val));}
	
	/*public void X(int a, int[] features){
		this.x = new double[inputs];
		int offset = a*(numFeatures+7);
		
		for(int i = 0; i < features.length; i++){
		  this.x[offset+features[i]] = 1;//active features, obs, bias
		}
	}*/
	
	public void X(int a, int[] features){
		this.x = new double[inputs];
		
		for(int i = 0; i < features.length; i++){
		  this.x[features[i]] = 1;//active features, obs, bias
		}
	}
	
	/*public double dot(int a, int[] features, int i) {
		int offset = a*(numFeatures+7);
		
		double sum = 0;
		for (int j = 0; j < features.length; j++) {sum += this.W[i][offset+features[j]];}
		return sum;
	}*/
	
	public double dot(int a, int[] features, int i) {
		double sum = 0;
		for (int j = 0; j < features.length; j++) {sum += this.W[i][features[j]];}
		return sum;
	}
	
	public void Rho(int a) {
		if (a == 0) {this.rho = this.rho0;}
		else if (a == 1) {this.rho = this.rho1;} 
		else if (a == 2) {this.rho = this.rho2;}
	}
	
	public void Beta(int o) {
		if (o == 0) {this.beta = this.beta1;}
		else{this.beta = this.beta2;}
	}
	
	public void Z(int o) {
		int[] obs = new int[6];
		obs[o] = 1;
		for(int j = 0; j < 5; j++){
			this.z[j] = obs[j+1];
			this.z[5+j] = ytilde[j];
			this.z[2*5+j] = ytilde[j];
			this.z[3*5+j] = ytilde[5+j];
			this.z[4*5+j] = ytilde[10+j];
			this.z[5*5+j] = obs[j+1];
			this.z[6*5+j] = obs[j+1];
			this.z[7*5+j] = obs[j+1];
			this.z[8*5+j] = obs[j+1];
		}
	}
	
	public double[] Y(int a, int[] features) {
		double[] temp = new double[this.nodes];
		for (int i = 0; i < this.nodes; i++) {
			temp[i] = dot(a,features,i);
            if (temp[i] < 0) temp[i] = 0.0;
            else if (temp[i] > 1) temp[i] = 1.0;
		}
		return temp;
	}
	
	public void Kappa(){
	   for(int i = 0; i < this.nodes; i++){ this.kappa[i] = this.kappa[i] * this.rho[i] * (1 - beta[i]) + 1;}
	}
	
	public void E() {
        double firstterm;
        for (int i = 0; i < this.nodes; i++){
            if (rho[i] != 0){
                firstterm = this.lambda * this.rho[i] * (1 - this.beta[i]);
                for (int j = 0; j < this.inputs; j++){
                    this.e[i][j] = firstterm * this.e[i][j] + kappa[i]*this.x[j];
                }
            }
            else{
                this.e[i] = this.x.clone();
            }
        }
	}
	
	
	/*public void E() {
        double firstterm;
        int index;
        Integer tempInt;
        
        for (int i = 0; i < this.nodes; i++){
            if(this.rho[i] != 0){ //if this node is active
                firstterm = this.lambda * this.rho[i] * (1 - this.beta[i]); //only need to compute this once
                for (int k = 0; k < current_features.length; k++){
                    tempInt = new Integer(current_features[k]); //make a new integer object of the feature index
                    if(!traceMem[i].contains(tempInt)) traceMem[i].addElement(tempInt); //if not already in the memory, add it in           
                }
                for (int j = 0; j < traceMem[i].size(); j++){ //for all active features
                    index = ((Integer)traceMem[i].elementAt(j)).intValue(); //retrieve the index in int form
                    this.e[i][index] = firstterm * this.e[i][index] + kappa[i]*this.x[index]; //update active trace                    
                }
            }
            else{ //if node is inactive
                this.e[i] = this.x.clone(); //because if rho = 0, kappa = 1, copy the feature vector into the trace matrix
                
                //only active traces are those pertaining to the current features
                traceMem[i] = new Vector();
                for (int k = 0; k < current_features.length; k++){
                    traceMem[i].addElement(new Integer(current_features[k]));
                }
            }
        }
	}*/
	
	public void computeNet(int a, int o, int[] features) {
		this.a = a;
		
		//this.Kappa();
		
		this.E();
		this.Rho(a);
		this.X(a,features);
		this.ytilde = this.Y(a,features);
		
		this.Beta(o);
		this.Z(o);
		
		/*for (int i = 0; i < this.nodes; i++) {
            //rolled the alpha into the delta equation for computational purposes
            delta[i] = alpha * (this.rho[i]*(this.beta[i] * this.z[i] + (1 - this.beta[i]) * this.ytilde[i]) - this.y[i]);
            for (int j = 0; j < this.inputs; j++) {
                W[i][j] = W[i][j] + delta[i] * this.e[i][j];
            }
		}*/   
		
		//Accumulating weights
		for (int i = 0; i < this.nodes; i++) {
            delta[i] = this.rho[i]*(this.beta[i] * this.z[i] + (1 - this.beta[i]) * this.ytilde[i]) - this.y[i];
            for (int j = 0; j < this.inputs; j++) {
                W_accumulate[i][j] = W_accumulate[i][j] + delta[i] * this.e[i][j];
            }
		}
		
		this.y = this.Y(a,features);
		current_features = features;
	}
}