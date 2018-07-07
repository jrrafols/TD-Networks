import java.util.Random;
import java.io.*;
import java.lang.Math;
import java.text.DecimalFormat;

public class OnTrainingB{    
    ///////////////////////////////////////////////////////////
    //General printing functions
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
    public static void print(long o){System.out.println(o);}
    public static void print_(long o){System.out.print(o);}
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
    public static void print_(int[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
    }
    public static void print(long[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    public static void print_(long[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
    }
    
    ///////////////////////////////////////////////////////////
    
    
    public static int x_size, y_size, wanderNum;
    public static double[][] predictions;
    static Random r = new Random();
    public static DecimalFormat df = new DecimalFormat("0.000");
    public static String filename;
    
    public static long[][] freq = new long[256][3];
    
    static WorldReader w;
    
    public static void train(){
        int a, rand_num;
        double randDouble;
        
        rand_num = r.nextInt(100);
        if (rand_num < 50){ //F,R,L
            w.takeAction(0);
        }
        else if (rand_num < 70){ //F,R,L
            w.takeAction(1);
        }
        else if (rand_num < 90){ //F,R,L
            w.takeAction(2);
        }
        else if (rand_num < 95){ //Leap
            do{
                w.takeAction(0); //Step Forward
            }while(w.getObs() == 0);
            w.takeAction(0); //Step Forward
        }
        else{ //Wander
            do{
                a = r.nextInt(3);
                randDouble = r.nextDouble();
                w.takeAction(a); //Random Step
            }while(w.getObs() == 0 && randDouble < 0.5);
            
        }
    }
    
    public static int stateNum(int pos0,int pos1,int pos2){return pos0 * y_size * 4 + pos1*4 + pos2;}
    
    public static void recordState(int a){
        int temp0,temp1,temp2,stateNumber;
        temp0 = w.pos[0];temp1 = w.pos[1];temp2 = w.pos[2];    
        stateNumber = stateNum(temp0,temp1,temp2);
        
        freq[stateNumber][a]++;
        
    }
    
    public static void main (String[] args){
        try{
            BufferedReader br = new BufferedReader(new FileReader("config"));
		  filename = br.readLine();
        }
        catch(IOException e){}
        
        int a, rand_num;
        double randDouble;
        
        w = new WorldReader(filename+".world");
        x_size = w.size0;
        y_size = w.size1;
        
        long time1 = System.currentTimeMillis();
        
        for (long i = 10*1000*1000*1000; i > 0 ; i--){
            rand_num = r.nextInt(100);
            if (rand_num < 50){ //F,R,L
                recordState(0);
                w.takeAction(0);
            }
            else if (rand_num < 70){ //F,R,L
                recordState(1);
                w.takeAction(1);
                
            }
            else if (rand_num < 90){ //F,R,L
                recordState(2);
                w.takeAction(2);
                
            }
            else if (rand_num < 95){ //Leap
                do{
                    recordState(0);
                    w.takeAction(0); //Step Forward
                    
                }while(w.getObs() == 0);
                recordState(0);
                w.takeAction(0); //Step Forward
                
            }
            else{ //Wander
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    recordState(a);
                    w.takeAction(a); //Random Step
                    
                }while(w.getObs() == 0 && randDouble < 0.5);      
            }
            
        }
        
        print(System.currentTimeMillis()-time1);
        
        /*//Prints out the randomly generated state Vectors
        int stateNumber;
        for (int i = 1; i < x_size - 1; i++){
            for (int j = 1; j < y_size - 1; j++){
                for (int k = 0; k < 4; k++){
                    stateNumber = stateNum(i,j,k);
                    //System.out.print(i+","+j+","+k+", "+stateNumber+" : ");print(freq[stateNumber]);
                    System.out.print(stateNumber+" ");print(freq[stateNumber]);
                }
            }
        }*/
        
        for (int j = 0; j < 256; j++){
            print_(j);
            print_(" ");
            print_(freq[j]);
            print_(" ");
            double temp = freq[j][0]+freq[j][1]+freq[j][2];
            if (temp != 0){
                print_(freq[j][0]/temp);
                print_(" ");
                print_(freq[j][1]/temp);
                print_(" ");
                print_(freq[j][2]/temp);
            }
            else{
                print_(freq[j]);
            }
            print(" ");
        }
    }

}