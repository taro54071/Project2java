import java.util.*;
import java.io.*;
import java.util.concurrent.*;

class OneShareMaterial {
    private String name;
    private int balance;

    public OneShareMaterial(String n, int b) {
        name = n;
        balance = b;
    }

    synchronized public void put(int x) {
        balance += x;
        System.out.printf("Thread %-14s >> Put %3d %s\tbalance = %3d %s\n", Thread.currentThread().getName(), x, name,
                balance, name);
    }

    synchronized public void get(int x) {
        balance -= x;
        System.out.printf("Thread %-14s >> Get %3d %s\tbalance = %3d %s\n", Thread.currentThread().getName(), x, name,
                balance, name);
    }

    public String getNameM() {
        return this.name;
    }

    public int getbalance() {
        return this.balance;
    }
}

class Factory extends Thread {
    private ArrayList<OneShareMaterial> OSM = new ArrayList<OneShareMaterial>();
    private int ID, total_lot = 0;
    private int lotsize = 0;
    private String product;
    private ArrayList<Integer> material = new ArrayList<Integer>();
    protected CyclicBarrier cfinish;
    // public Factory() {
    // }

    // public Factory(int id, String pro, int lot, int ma1, int ma2) {
    // super(pro);
    // ID = id;
    // product = pro;
    // lotsize = lot;
    // material.add(ma1 * lotsize);
    // material.add(ma2 * lotsize);
    // }

    // public Factory(int id, String pro, int lot, ArrayList<Integer> ma) {
    // super(pro);
    // ID = id;
    // product = pro;
    // lotsize = lot;
    // for (int i = 0; i < ma.size(); i++) {
    // material.add(ma.get(i) * lotsize);
    // }
    // }

    public Factory(int id, String pro, int lot, ArrayList<OneShareMaterial> osm, ArrayList<Integer> ma) {
        super(pro);
        ID = id;
        product = pro;
        lotsize = lot;
        OSM = osm;
        for (int i = 0; i < ma.size(); i++) {
            material.add(ma.get(i));
        }
    }

    public static void print_thread(String T) {
        System.out.printf("Thread %-14s >> ", T);
    }

    public int getMaterial(int i) {
        return material.get(i);
    }

    public String getProduct() {
        return this.product;
    }

    public int getID() {
        return this.ID;
    }

    public int getLot() {
        return this.lotsize;
    }

    public void setCyclicBarrier(CyclicBarrier f) {
        cfinish = f;
    }

    synchronized public void run() {
        try {
            for (int i = 0; i < OSM.size(); i++) {
                if (material.get(i) * lotsize < OSM.get(i).getbalance()) {
                    OSM.get(i).get(material.get(i) * lotsize);
                } else {
                    print_thread(Thread.currentThread().getName());
                    System.out.println("----- Fail");
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}

public class FactorySimulation {
    public static void print_thread(String T) {
        System.out.printf("Thread %-14s >> ", T);
    }

    public static void main(String[] args) {
        int filename_pass = 0, n_material = 0, n_day, row = 0;
        ArrayList<Factory> Flist = new ArrayList<Factory>();
        ArrayList<Factory> Flist_temp = new ArrayList<Factory>();
        ArrayList<OneShareMaterial> Mlist = new ArrayList<OneShareMaterial>();

        String filename;
        CyclicBarrier finish = new CyclicBarrier(3);
        Scanner keyboard = new Scanner(System.in);
        while (filename_pass != 1) {
            try {
                print_thread(Thread.currentThread().getName());
                System.out.println("Enter product specification file = ");
                System.out.println("spec.txt"); /// For debug only
                // filename = keyboard.nextLine();
                Scanner readfile = new Scanner(new File("spec.txt"));
                while (readfile.hasNext()) {
                    String line = readfile.nextLine();
                    String[] buf = line.split(",");
                    // if (buf.length == 2) {
                    // for (int i = 0; i < buf.length; i++) {
                    // OneShareMaterial n = new OneShareMaterial(buf[i].trim(), 0);
                    // Mlist.add(n);
                    // }
                    if (row == 0) { // first row in spec.txt
                        for (int i = 0; i < buf.length; i++) {
                            OneShareMaterial n = new OneShareMaterial(buf[i].trim(), 0);
                            Mlist.add(n);
                            row++;
                        }
                    } else {
                        ArrayList<Integer> numsofmaterial = new ArrayList<Integer>();
                        numsofmaterial.add(Integer.parseInt(buf[3].trim()));
                        numsofmaterial.add(Integer.parseInt(buf[4].trim()));
                        Factory f = new Factory(Integer.parseInt(buf[0].trim()), buf[1].trim(),
                                Integer.parseInt(buf[2].trim()), Mlist, numsofmaterial);
                        f.setCyclicBarrier(finish);
                        print_thread(Thread.currentThread().getName());
                        // System.out.printf("%-2s factory\t%6s units per lot\t materials per lot = %5d
                        // %s, %5d %s\n",
                        // buf[1], buf[2], Integer.parseInt(buf[3].trim()), Mlist.get(0).getNameM(),
                        // Integer.parseInt(buf[4].trim()), Mlist.get(1).getNameM());
                        System.out.printf("%-9s factory \t%6s units per lot\t materials per lot = %5d %s, %5d %s\n",
                                f.getProduct(), f.getLot(), numsofmaterial.get(0), Mlist.get(0).getNameM(),
                                numsofmaterial.get(1), Mlist.get(1).getNameM());
                        Flist.add(f);
                    }
                }
                filename_pass = 1;
            } catch (

            FileNotFoundException e) {
                System.out.println(e);
                System.out.println("=== Cannot find input file Please enter again ===");
            } catch (Exception e) { // For Debug only
                System.out.println(e);
            }

            System.out.println();

            print_thread(Thread.currentThread().getName());
            System.out.println("Enter amount of meterial per day =");
            n_material = Integer.parseInt(keyboard.nextLine());
            System.out.println("Enter amount of Day =");
            n_day = Integer.parseInt(keyboard.nextLine());
            keyboard.close();
            System.out.println();

            for (int i = 1; i <= n_day; i++) {
                // for (int j = 0; j < Flist.size(); j++) {
                // ArrayList<Integer> numsofmaterial_temp = new ArrayList<Integer>();
                // Factory f_temp = new Factory(Flist.get(j).getID(), Flist.get(j).getProduct(),
                // Flist.get(j).getLot(),
                // Flist.get(j).getMaterial(0), Flist.get(j).getMaterial(1));
                // Flist_temp.add(f_temp);
                // }
                for (int j = 0; j < Flist.size(); j++) {
                    Factory f_temp = new Factory(Flist.get(j).getID(), Flist.get(j).getProduct(), Flist.get(j).getLot(),
                            Mlist, numsofmaterial);

                    // public Factory(int id, String pro, int lot, ArrayList<OneShareMaterial> osm,
                    // ArrayList<Integer> ma) {

                    Flist_temp.add(f_temp);
                }

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

                print_thread(Thread.currentThread().getName());
                System.out.println("Day " + i);

                for (int j = 0; j < Mlist.size(); j++) {
                    Mlist.get(j).put(n_material);
                }

                for (int j = 0; j < Flist_temp.size(); j++) {
                    try {
                        Flist_temp.get(j).start();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                for (int j = 0; j < Flist_temp.size(); j++) {
                    try {
                        Flist_temp.get(j).join();
                    } catch (Exception e) {
                    }
                }

                Flist_temp.clear();
                System.out.println("---------This is the end of the day------------");
                System.out.println();
            }

            print_thread(Thread.currentThread().getName());
            System.out.println("Summary");
            print_thread(Thread.currentThread().getName());
            System.out.println("");
        }
    }
}