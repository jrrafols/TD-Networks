import java.io.Serializable;

//Off-Policy, Features, single node
public class ColorWorldTDNet6 implements Serializable{
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
	
	public int nodes, inputs, numFeatures;

	public double lambda, alpha;

	public double[] x, y, z, kappa, rho, beta, ytilde, delta;
	
	public double[][] W, e, W_accumulate;
	
	public ColorWorldTDNet6(double alpha, double lambda, int numFeatures) {
		this.nodes = 1;
		this.inputs = numFeatures;
		this.numFeatures = numFeatures;
		
		this.lambda = lambda;
		this.W = new double[nodes][inputs];
		this.W_accumulate = new double[nodes][inputs];
		
		this.e = new double[nodes][inputs];
		this.x = new double[inputs];
		this.rho = new double[nodes];
		this.beta = new double[nodes];
		this.z = new double[nodes];
		this.y = new double[nodes];
        	this.kappa = new double[nodes];
		for (int i = 0; i < nodes; i++){ this.kappa[i] = 1; }
        	this.delta = new double[this.nodes];
		this.alpha = alpha;	
	}

	public void X(int a, int[] features){
		this.x = new double[inputs];
		
		for(int i = 0; i < features.length; i++){
			this.x[features[i]] = 1;//active features, obs, bias
		}
	}

	public double dot(int a, int[] features, int i) {
		double sum = 0;
		for (int j = 0; j < features.length; j++) {sum += this.W[i][features[j]];}
		return sum;
	}
	
	public void Rho(int a) {
		if (a == 0) {this.rho[0] = 2.0;}
		else {this.rho[0] = 0;} 
	}

	public void Beta(int o) {
		if (o == 2) {this.beta[0] = 1;}
		else{this.beta[0] = 0;}
	}
	
	public void Z(int o) {
		if (o == 2) { this.z[0] = 1; }
		else { this.z[0] = 0; }
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
		for(int i = 0; i < this.nodes; i++){
			this.kappa[i] = this.kappa[i] * this.rho[i] * (1 - beta[i]) + 1;
		}
	}
	
	public void E() {
		double firstterm;
        	for (int i = 0; i < this.nodes; i++){
			firstterm = this.lambda * this.rho[i] * (1 - this.beta[i]);
			for (int j = 0; j < this.inputs; j++){
				this.e[i][j] = firstterm * this.e[i][j] + kappa[i] * this.x[j];
				//this.e[i][j] = firstterm * this.e[i][j] + this.x[j];
			}
        	}
	}
	
	public void computeNet(int a, int o, int[] features) {
		//print_(a);print_(", ");print(features);
		this.Kappa();
		this.E();
		this.Rho(a);
		this.X(a,features);
		this.ytilde = this.Y(a,features);
		this.Beta(o);
		this.Z(o);
		
		//Accumulating weights
		for (int i = 0; i < this.nodes; i++) {
            	delta[i] = this.rho[i]*(this.beta[i] * this.z[i] + (1 - this.beta[i]) * this.ytilde[i]) - this.y[i];
			for (int j = 0; j < this.inputs; j++) {
                		W_accumulate[i][j] = W_accumulate[i][j] + delta[i] * this.e[i][j];
            		}
		}
		
		this.y = this.Y(a,features);
		/*if (delta[0] != 0){
			print_("Action: ");print(a);
			print_("kappa: ");print(kappa);
			print_("rho: ");print(rho);
			print_("trace: ");print(e[0]);
			print_("ytilde: ");print(ytilde);
			print_("beta: ");print(beta);
			print_("z: ");print(z);
			print_("delta: ");print(delta[0]);
			print_("y: ");print(y);
			print_("Accumulate: ");print(W_accumulate[0]);
			print(" ");
		}*/
	}
}