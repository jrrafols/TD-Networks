import java.util.Random;
import java.io.*;
import java.text.DecimalFormat;

public class ErrorMeasure{
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
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
    
    public static String[] node_names = new String[] {"Leap\t\t", "L-Leap\t\t","R-Leap\t\t","Leap-L-Leap\t","Leap-R-Leap\t","F\t\t","L\t\t","R\t\t","Wander\t\t"};
    public static DecimalFormat df = new DecimalFormat("0.000");

    public static WorldReader w;
    public static int x_size, y_size;
    public static double[][] predictions;
    public static Random r = new Random();
    
    public static int stateNum(int[] pos){
        return pos[0] * y_size * 4 + pos[1]*4 + pos[2];
    }
    
    public static double[] takeSeq(int seq){
        double[] result = null;
        if(seq == 0){
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 1){
            w.takeAction(2);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 2){
            w.takeAction(1);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 3){
            while (w.getObs() == 0){w.takeAction(0);}
            w.takeAction(2);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 4){
            while (w.getObs() == 0){w.takeAction(0);}
            w.takeAction(1);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 5){
            w.takeAction(0);
            result = w.getBitObs();
        }
        else if(seq == 6){
            w.takeAction(2);
            result = w.getBitObs();
        }
        else if(seq == 7){
            w.takeAction(1);
            result = w.getBitObs();
        }
        else if(seq == 8){
            int[] temp = new int[3];
            temp[0] = w.pos[0]; temp[1] = w.pos[1]; temp[2] = w.pos[2];
            double[] temp2;
            double randDouble;
            int a;
            result = new double[6];
            int reps = 10000;
            
            for (int i = 0; i < reps; i++){
                w.set_pos(temp);
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    w.takeAction(a); //Random Step
                }while(w.getObs() == 0 && randDouble < 0.5);
                
                for (int j = 0; j < 6; j++){
                    temp2 = w.getBitObs();
                    result[j] += temp2[j];
                }
            }
            for(int i = 0; i < 6; i++){
                result[i] = result[i]/reps;
            }
            w.set_pos(temp);
        }
        
        return result;
    }
    
    public static String statePredictions(int[] pos){
        String s = "\t\t    1     2     3     4     5\n";
        for (int i = 0; i < 9; i++){
            s+= node_names[i]+ ": ";
            for (int j = 0; j < 5; j++){
                s+= df.format(predictions[stateNum(pos)][(i*5)+j]) + " ";
            }
            s+= "\n";
		}
        return s;
    }
    
    public static void main (String[] args){
        String filename = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader("config"));
		  filename = br.readLine();
        }
        catch(IOException e){}
        
        w = new WorldReader(filename+".world");
        
        x_size = w.size0;
        y_size = w.size1;
        int[] temp_pos = new int[3];
        double[] result;
        predictions = new double[x_size*y_size*4][45];
        
        for (int i = 1; i < x_size - 1; i++){
            for (int j = 1; j < y_size - 1; j++){
                for (int k = 0; k < 4; k++){
                    temp_pos[0] = i; temp_pos[1] = j; temp_pos[2] = k;
                    for (int l = 0; l < 9; l++){
                        //w.pos[0] = i; w.pos[1] = j; w.pos[2] = k;
                        w.set_pos(temp_pos);
                        result = takeSeq(l);
                        for (int m = 0; m < 5; m++){
                            predictions[stateNum(temp_pos)][l*5+m] = result[m+1];
                        }
                    }
                    w.set_pos(temp_pos);
                    print(w.pos);
                    print(w);
                    System.out.println(statePredictions(temp_pos));
                }
            }
        }
          
    }
}