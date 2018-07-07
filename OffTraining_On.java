import java.util.Random;
import java.io.*;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.Vector;

public class OffTraining_On{    
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
    
    
    public static int x_size, y_size, wanderNum;
    public static double[][] predictions;
    static Random r = new Random();
    static Random r_seeded = new Random(15);
    public static DecimalFormat df = new DecimalFormat("0.000");
    public static String filename;
    
    static WorldReader w;
    static ColorWorldTDNet6 t;
  
    public static void train(int steps){
        int rand_num;
        int counter = 0;

        while (counter < steps){
		rand_num = r.nextInt(4);
		if (rand_num == 3) rand_num = 0;
		w.takeAction(rand_num);
		t.computeNet(rand_num, w.getObs(),w.getFeatures());
		counter++;
        }
    }
    
    public static void main (String[] args){
	try{
		BufferedReader br = new BufferedReader(new FileReader("config"));
		filename = br.readLine();
	}
	catch(IOException e){}
        
       w = new WorldReader(filename+".world",41,13);
       x_size = w.size0;
       y_size = w.size1;

	t = new ColorWorldTDNet6(0.05, 0.0, w.numFeatures);
       double[][] expected_update = new double[t.nodes][t.inputs];
	double[] temp_vec = new double[t.inputs];
        
	int iterations = 250000;
       for(int k = 0; k < iterations; k++){
		w = new WorldReader(filename+".world",41,13);
		w.pos[2] = 1;
		t = new ColorWorldTDNet6(0.05, 0.0, w.numFeatures);
		train(4);

		for (int i = 0; i < t.W_accumulate.length; i++){
			for (int j = 0; j < t.W_accumulate[i].length; j++){
				expected_update[i][j] += t.W_accumulate[i][j];
			}
		}
		
		if(k != 0 && k % 1000 == 0){
			print(k);
			for (int i = 0; i < t.W_accumulate.length; i++){
				for (int j = 0; j < t.W_accumulate[i].length; j++){
                			temp_vec[j] = expected_update[i][j]/(double)k;
            			}
            			print(temp_vec);
       		}
			print(" ");
		}
	}
	for (int i = 0; i < t.W_accumulate.length; i++){
		for (int j = 0; j < t.W_accumulate[i].length; j++){
			temp_vec[j] = expected_update[i][j]/(double)iterations;
            	}
            	print(temp_vec);
	}
	print(" ");
   }

}