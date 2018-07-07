import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

//On-Policy, TDNet
public class ColorWorldTDNet2 implements Serializable{
	public static DecimalFormat df = new DecimalFormat("0.000");
	
	public static String[] node_names = new String[] {"Leap\t\t", "L-Leap\t\t","R-Leap\t\t","Leap-L-Leap\t","Leap-R-Leap\t","F\t\t","L\t\t","R\t\t","Wander\t\t"};
	
	//Used for saving
	public int[] pos = new int[3];
	public int[] get_pos(){ return this.pos; }
	public void set_pos(int[] temp){ for (int i = 0; i < 3; i++) this.pos[i] = temp[i]; }
	
	public int nodes, inputs, a;

	public double lambda, alpha;

	public double[] x,y, z, c, c0, c1, c2, c3, c4, beta, beta0, beta1, beta2, ytilde, delta;
	
	public double[][] W, e;
	
	public ColorWorldTDNet2(double alpha, double lambda) {
		this.nodes = 45;
		this.inputs = 52*3; //(45 nodes + 6 obs bits + bias) * 3 actions
		
		this.lambda = lambda;
		this.a = 0;
		this.W = new double[nodes][inputs];
		this.e = new double[nodes][inputs];
		this.x = new double[inputs];
		this.c = new double[nodes];
		this.beta = new double[nodes];
		this.z = new double[nodes];
		this.y = new double[nodes];
        
        this.delta = new double[this.nodes];
        
        this.beta0 = new double[this.nodes];
		this.beta1 = new double[this.nodes];
		this.beta2 = new double[this.nodes];
		
		for(int counter = 0; counter < 45; counter++){
            this.beta0[counter] = 0;
            this.beta1[counter] = 1;
            this.beta2[counter] = 0.5;
		}
		
		this.c0 = new double[this.nodes]; //F
		this.c1 = new double[this.nodes]; //R
		this.c2 = new double[this.nodes]; //L
		this.c3 = new double[this.nodes]; //Leap
		this.c4 = new double[this.nodes]; //Wander
		
		for(int counter = 0; counter < 5; counter++){
			this.c3[counter] = 1; //Leap
			this.c2[5+counter] = 1; //L
			this.c1[10+counter] = 1; //R
			this.c3[15+counter] = 1; //Leap-L-Leap
			this.c3[20+counter] = 1; //Leap-R-Leap
			this.c0[25+counter] = 1; //F
			this.c2[30+counter] = 1; //L
			this.c1[35+counter] = 1; //R
			this.c4[40+counter] = 1; //Wander
			
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
	
	public void X(int a, int o){
		this.x = new double[inputs];
		int offset = a*(7+nodes);
		for(int i = 0; i<nodes; i++){
			this.x[offset+i] = this.y[i];
		}
		this.x[offset+(nodes)+o] = 1; //observation bit
		this.x[offset+(nodes)+6] = 1; //bias term
	}
	
	public double dot(int a, double[] tempW) {
		double sum = 0;
		for (int i = a*(nodes+7); i < (a+1)*(nodes+7); i++) {sum += x[i]*tempW[i];}
		return sum;
	}
	
	public void C(int cond) {
		if (cond == 0) {this.c = this.c0;}
		else if (cond == 1) {this.c = this.c1;} 
		else if (cond == 2) {this.c = this.c2;}
		else if (cond == 3) {this.c = this.c3;}
		else if (cond == 4) {this.c = this.c4;}
	}
	
	public void Beta(int o, int cond) {
		if (cond < 3) {this.beta = this.beta1;}
		else if (cond == 3 && o == 0) {this.beta = this.beta0;}
		else if (cond == 3 && o != 0) {this.beta = this.beta1;}
		else if (cond == 4 && o == 0) {this.beta = this.beta2;}
		else if (cond == 4 && o != 0) {this.beta = this.beta1;}
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
	
	public double[] Y(int a) {
		double[] temp = new double[this.nodes];
		for (int i = 0; i < this.nodes; i++) {
			//temp[i] = sigma(dot(a,this.W[i])); //sigmoidal
			
			temp[i] = dot(a,this.W[i]); //linear
            if (temp[i] < 0) temp[i] = 0.0;
            else if (temp[i] > 1) temp[i] = 1.0;
		}
		return temp;
	}
	
	public void E() {
		double firstterm = 0;
		for (int i = 0; i < this.nodes; i++) {
            if (this.c[i] != 0){
                firstterm = this.lambda * (1 - this.beta[i]);
                for (int j = 0; j < this.inputs; j++) {
                    this.e[i][j] = firstterm * this.e[i][j] + this.x[j];
                }
            }
            else {this.e[i] = new double[this.inputs];}
		}
	}
	
	public void computeNet(int a,int condition, int o) {
		this.C(condition);
		this.E();
		
		this.X(a,o);
		this.ytilde = this.Y(a);
		
		this.Beta(o, condition);
		this.Z(o);
		
		for (int i = 0; i < this.nodes; i++) {
            if (this.c[i] != 0){
                //rolled the alpha into the delta equation for computational purposes
                delta[i] = alpha * (this.beta[i] * this.z[i] + (1 - this.beta[i]) * this.ytilde[i] - this.y[i]);
                for (int j = 0; j < this.inputs; j++) {
                    W[i][j] = W[i][j] + delta[i] * this.e[i][j];
                }
            }
		}    
		
		this.y = this.Y(a);
		
	}
}