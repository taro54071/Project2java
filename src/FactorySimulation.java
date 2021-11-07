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
        if (balance < 0) {
            balance = 0;
            x = 0;
        }
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

class Factory extends Thread implements Comparable<Factory> {
    private ArrayList<OneShareMaterial> OSM = new ArrayList<OneShareMaterial>();
    private int ID, completed_lot = 0;
    private int lotsize = 0;
    private String product;
    private ArrayList<Integer> material = new ArrayList<Integer>();
    private ArrayList<Integer> stored_material = new ArrayList<Integer>();
    protected CyclicBarrier cfinish;

    public Factory(int id, String pro, int lot, ArrayList<OneShareMaterial> osm, ArrayList<Integer> ma) {
        super(pro);
        ID = id;
        product = pro;
        lotsize = lot;
        OSM = osm;
        for (int i = 0; i < ma.size(); i++) {
            material.add(ma.get(i));
        }
        // System.out.println("hello");
        // Collections.copy(stored_material, material);
        for (int i = 0; i < material.size(); i++) {
            stored_material.add(material.get(i));
        }
    }

    public int compareTo(Factory otherFactory) {
        if (this.completed_lot > otherFactory.completed_lot) {
            return 1;
        } else if (this.completed_lot < otherFactory.completed_lot) {
            return -1;
        } else {
            return 0;
        }
    }

    public static void print_thread(String T) {
        System.out.printf("Thread %-14s >> ", T);
    }

    public ArrayList<Integer> getMaterialAL() {
        return material;

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

    public int getTotalCLot() {
        return this.completed_lot;
    }

    public void setCyclicBarrier(CyclicBarrier f) {
        cfinish = f;
    }

    public void run() {
        int check = 0;
        int set_sleep = (int) (Math.random() * 200);
        try {
            sleep(set_sleep);
        } catch (InterruptedException e) {
            // TODO: handle exception
        }
        synchronized (this) {
            try {
                for (int i = 0; i < material.size(); i++) {
                    if (stored_material.get(i) != 0) {
                        int get_material = 0;
                        // stored_material.get(0) = 20
                        // stored_material.get(1) = 60
                        if (stored_material.get(i) <= OSM.get(i).getbalance()) {
                            get_material = stored_material.get(i);
                            OSM.get(i).get(get_material);
                        } else if (stored_material.get(i) > OSM.get(i).getbalance() && OSM.get(i).getbalance() != 0) {
                            // material from yesterday
                            get_material = OSM.get(i).getbalance(); // take all of the balance
                            OSM.get(i).get(get_material);
                            check = 1;
                        } else if (stored_material.get(i) > OSM.get(i).getbalance() && OSM.get(i).getbalance() == 0) {
                            OSM.get(i).get(0);
                            check = 1;
                        }

                        // System.out.printf("This is before stored material %d\n",
                        // stored_material.get(i));
                        // stored_material.set(i, stored_material.get(i) - get_material);
                        // System.out.printf("This is after stored material %d\n",
                        // stored_material.get(i));

                        // hand 100 balance 60
                        // stored.get(1) = 40
                        // check += stored_material.get(i);
                        stored_material.set(i, stored_material.get(i) - get_material);
                        // System.out.printf("This is round %d: %d\n", i + 1, check);
                    } else {
                        continue;
                    }
                }
                if (check == 0) { // success
                    completed_lot++;
                    print_thread(Thread.currentThread().getName());
                    System.out.printf("+++++ Complete Lot %d\n", completed_lot);
                    for (int i = 0; i < stored_material.size(); i++) {
                        stored_material.set(i, material.get(i));
                    }
                } else { // fail
                    print_thread(Thread.currentThread().getName());
                    System.out.println("----- Fail");
                }
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println(e);
            }
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
        ArrayList<Thread> Flist_temp = new ArrayList<Thread>();
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
                        for (int i = 3; i < buf.length; i++) {
                            numsofmaterial.add(Integer.parseInt(buf[i].trim()) * Integer.parseInt(buf[2].trim()));
                        }
                        // numsofmaterial.add(Integer.parseInt(buf[3].trim()));
                        // numsofmaterial.add(Integer.parseInt(buf[4].trim()));
                        // System.out.printf("This is buttons %d and this is zippers %d \n",
                        // Integer.parseInt(buf[3].trim()), Integer.parseInt(buf[4].trim()));

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
                // for (int j = 0; j < Flist.size(); j++) {
                // Factory f_temp = new Factory(Flist.get(j).getID(), Flist.get(j).getProduct(),
                // Flist.get(j).getLot(),
                // Mlist, Flist.get(i).getMaterialAL());
                // print_thread(f_temp.getProduct());
                // System.out.printf("this is buttons %d and this is zippers %d\n",
                // f_temp.getMaterial(0),
                // f_temp.getMaterial(1));
                // // public Factory(int id, String pro, int lot, ArrayList<OneShareMaterial>
                // osm,
                // // ArrayList<Integer> ma) {

                // Flist_temp.add(f_temp);
                // }

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

                print_thread(Thread.currentThread().getName());
                System.out.println("Day " + i);

                for (int j = 0; j < Mlist.size(); j++) {
                    Mlist.get(j).put(n_material);
                }

                System.out.println();

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

                for (int j = 0; j < Flist.size(); j++) {
                    try {
                        Thread T = new Thread(Flist.get(j));
                        T.setName(Flist.get(j).getName());
                        T.start();
                        Flist_temp.add(T);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                for (int j = 0; j < Flist_temp.size(); j++) {
                    try {
                        Flist_temp.get(j).join();
                    } catch (InterruptedException e) {
                        // TODO: handle exception
                        System.out.println(e);
                    }
                }
                Flist_temp.clear();
                System.out.println();

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

                for (int j = 0; j < Flist.size(); j++) {
                    try {
                        Flist.get(j).join();
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }

                Flist_temp.clear();
                System.out.println("---------This is the end of the day------------");
                System.out.println();
            }

            Collections.sort(Flist);
            print_thread(Thread.currentThread().getName());
            System.out.println("Summary");

            for (int i = 0; i < Flist.size(); i++) {
                print_thread(Thread.currentThread().getName());
                System.out.printf("Total %-8s Lots = %3d\n", Flist.get(i).getProduct(), Flist.get(i).getTotalCLot());
                // print_thread(Thread.currentThread().getName());
                // System.out.println("");

            }

        }
    }
}