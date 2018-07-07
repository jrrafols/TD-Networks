import java.io.Serializable;

//On-policy, features, single node
public class ColorWorldTDNet7 implements Serializable{
	public int nodes, inputs, a, numFeatures;

	public double lambda, alpha;

	public double[] x, y, z, c, beta, ytilde, delta;
	
	public double[][] W, e, W_accumulate;
	
	public ColorWorldTDNet7(double alpha, double lambda, int numFeatures) {
		this.nodes = 1;
		this.inputs = numFeatures;
		this.numFeatures = numFeatures;
		
		this.lambda = lambda;
		this.a = 0;
		this.W = new double[nodes][inputs];
		this.W_accumulate = new double[nodes][inputs];
		this.e = new double[nodes][inputs];
		this.x = new double[inputs];
		this.c = new double[nodes];
		this.beta = new double[nodes];
		this.z = new double[nodes];
		this.y = new double[nodes];
        
        	this.delta = new double[this.nodes];
		this.alpha = alpha;
	}

	public double sigma(double y_val) {return 1 / (1 + Math.exp(-y_val));}
	
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
	
	public void C(int cond) {
		if (cond == 3) {this.c[0] = 1;}
		else {this.c[0] = 0;}
	}
	
	public void Beta(int o, int cond) {
		if (o == 2) {this.beta[0] = 1;}
		else {this.beta[0] = 0;}
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
	
	public void E() {
		double firstterm = 0;
		for (int i = 0; i < this.nodes; i++) {
            		firstterm = this.lambda * (1 - this.beta[i]);
            		for (int j = 0; j < this.inputs; j++) {
                    		this.e[i][j] = firstterm * this.e[i][j] + this.x[j];
                	}
		}
	}
	
	public void computeNet(int a,int condition, int o, int[] features) {
		this.C(condition);
		this.E();
		
		this.X(a,features);
		this.ytilde = this.Y(a,features);
		
		this.Beta(o, condition);
		this.Z(o); 
		
		for (int i = 0; i < this.nodes; i++) {
                	delta[i] = this.beta[i] * this.z[i] + (1 - this.beta[i]) * this.ytilde[i] - this.y[i];
                	for (int j = 0; j < this.inputs; j++) {
                    		W_accumulate[i][j] = W_accumulate[i][j] + c[i] * delta[i] * this.e[i][j];
                	}
		}
		
		this.y = this.Y(a,features);
		
	}
}