import java.util.*;
import java.io.*;


class OneShareMaterial{
    private String name;
    private int balance;
    public OneShareMaterial(String n, int b){
        name = n;
        balance = b;
    }
    public void put(int x){
        balance+=x;
    }

    public void get(int x){
        balance-=x;
    }

    public String getNameM(){
        return this.name;
    }
    public int getbalance(){
        return this.balance;
    }
}

class Factory extends Thread{
    private int ID,lotsize;
    private String product;
    private ArrayList<Integer> material = new ArrayList<Integer>();
    public Factory(int id, String pro, int lot, int ma1, int ma2){
        super(pro);
        ID = id;
        product = pro;
        lotsize = lot;
        material.add(ma1*lotsize);
        material.add(ma2*lotsize);
        
    }
    
    public static void print_thread(String T){
        System.out.printf("Thread  %-2s  >>  ", T );
    }
    public int getMaterial(int i){
        return material.get(i);
    }
    public String getProduct(){
        return this.product;
    }
    public void run(){
        print_thread(Thread.currentThread().getName());
        System.out.println("asdfasdfasdf");
    }
}


public class FactorySimulation{
    public static void print_thread(String T){
        System.out.printf("Thread  %-2s  >>  ", T );
    }
    public static void main(String[] args){
        int filename_pass = 0,n_material,n_day;
        ArrayList<Factory> Flist = new ArrayList<Factory>();
        ArrayList<OneShareMaterial> Mlist = new ArrayList<OneShareMaterial>();
        String filename;
        Scanner keyboard = new Scanner(System.in);
        while(filename_pass != 1){
            try{
                print_thread(Thread.currentThread().getName());
                System.out.println("Enter product specification file = ");
                System.out.println("spec.txt");     ///For debug only
                //filename = keyboard.nextLine();
                Scanner readfile = new Scanner(new File("spec.txt"));
                while(readfile.hasNext()){
                    String line = readfile.nextLine();
                    String [] buf = line.split(",");
                    if(buf.length == 2){
                        for(int i =0;i<buf.length;i++){
                            OneShareMaterial n = new OneShareMaterial(buf[i].trim(),0);
                            Mlist.add(n);
                        }
                    }
                    else{
                        Factory f = new Factory(Integer.parseInt(buf[0].trim()),buf[1].trim(),Integer.parseInt(buf[2].trim()),Integer.parseInt(buf[3].trim()),Integer.parseInt(buf[4].trim()));
                        print_thread(Thread.currentThread().getName());
                        System.out.printf("%-2s factory\t%6s units per lot\t materials per lot = %d %s, %d %s\n",buf[1],buf[2],f.getMaterial(0),Mlist.get(0).getNameM(),f.getMaterial(0),Mlist.get(1).getNameM());
                        Flist.add(f);
                    }
                }
                filename_pass = 1;
            }
            catch(FileNotFoundException e){
                System.out.println(e);
                System.out.println("=== Cannot find input file Please enter again ===");
            }
            catch(Exception e){         //For Debug only
                System.out.println(e);
            }
            
            System.out.println();
            print_thread(Thread.currentThread().getName());
            System.out.println("Enter amount of meterial per day =");
            n_material = Integer.parseInt(keyboard.nextLine());
            System.out.println("Enter amount of Day =");
            n_day = Integer.parseInt(keyboard.nextLine());
            keyboard.close();

            for(int i=1;i<=n_day;i++){
                print_thread(Thread.currentThread().getName());
                System.out.println("Day "+ i);
                try{
                    Flist.get(2).start();
                    Flist.get(1).start();
                    Flist.get(0).start();
                }   
                catch (Exception e){
                    System.out.println(e);
                }             
                for(int j=0;j<Flist.size();j++){
                    try {
                        Flist.get(j).start();           ////// ****Not working - Can't start thread mulitple time**** /////
                    }
                    catch(Exception e){
                        System.out.println(e);
                    }
                }
                for(int k=0;k<Flist.size();k++){
                    try{
                        Flist.get(k).join();
                    }
                    catch(Exception e) {}
                }

                System.out.println("---------This is the end------------");
            }
        }
    }
}