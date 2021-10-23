import java.util.*;



class OneShareMaterial{

}

class Factory{

}


public class FactorySimulation {
    public static void print_thread(String T){
        System.out.printf("Thread  %-2s  >>  ", T );
    }
    public static void input_file(String x){
        int inputfile_pass = 0;
        String filename;
        Scanner keyboard = new Scanner(System.in);
        while(inputfile_pass != 1){
            try{
                print_thread(x);
                System.out.println("Enter product specification file = ");
                filename = keyboard.nextLine();
            }
            catch(Exception e){
                System.out.println("=== Cannot find input file Please enter again ===");
            }
        }
        keyboard.close();
    }
    public static void main(String[] args){
        input_file(Thread.currentThread().getName());

    }
}