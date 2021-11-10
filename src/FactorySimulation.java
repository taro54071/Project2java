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
        synchronized (this.OSM) {
            try {
                for (int i = 0; i < material.size(); i++) {
                    if (stored_material.get(i) != 0) {
                        int get_material = 0;
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

                        stored_material.set(i, stored_material.get(i) - get_material);
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
        int filename_pass = 0, n_material = 0, n_day = 0, row = 0;
        ArrayList<Factory> Flist = new ArrayList<Factory>();
        ArrayList<Thread> Flist_temp = new ArrayList<Thread>();
        ArrayList<OneShareMaterial> Mlist = new ArrayList<OneShareMaterial>();

        String filename = "";
        CyclicBarrier finish = new CyclicBarrier(3);
        Scanner keyboard = new Scanner(System.in);
        while (filename_pass != 1) {
            try {
                while (filename.equals("spec.txt") == false) {
                    print_thread(Thread.currentThread().getName());
                    System.out.println("Enter product specification file = ");
                    filename = keyboard.nextLine();
                }
                System.out.println();
                Scanner readfile = new Scanner(new File(filename));
                while (readfile.hasNext()) {
                    String line = readfile.nextLine();
                    String[] buf = line.split(",");
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

                        Factory f = new Factory(Integer.parseInt(buf[0].trim()), buf[1].trim(),
                                Integer.parseInt(buf[2].trim()), Mlist, numsofmaterial);

                        f.setCyclicBarrier(finish);
                        print_thread(Thread.currentThread().getName());
                        System.out.printf("%-9s factory \t%6s units per lot\t materials per lot = %5d %s, %5d %s\n",
                                f.getProduct(), f.getLot(), numsofmaterial.get(0), Mlist.get(0).getNameM(),
                                numsofmaterial.get(1), Mlist.get(1).getNameM());
                        Flist.add(f);
                    }
                }
                filename_pass = 1;
            } catch (FileNotFoundException e) {
                System.out.println(e);
                System.out.println("=== Cannot find input file Please enter again ===");
            } catch (NumberFormatException e) { // For Debug only
                System.out.println(e);
                continue;
            }

            System.out.println();

            while (n_material <= 0 || n_day <= 0) {
                try {
                    print_thread(Thread.currentThread().getName());
                    System.out.println("Enter amount of material per day =");
                    n_material = Integer.parseInt(keyboard.nextLine());
                    print_thread(Thread.currentThread().getName());
                    System.out.println("Enter nubmer of Day =");
                    n_day = Integer.parseInt(keyboard.nextLine());
                    if (n_material <= 0 || n_day <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                    System.out.println("Please enter material and day again");
                }
            }

            keyboard.close();
            System.out.println();

            for (int i = 1; i <= n_day; i++) {

                print_thread(Thread.currentThread().getName());
                System.out.println("Day " + i);

                for (int j = 0; j < Mlist.size(); j++) {
                    Mlist.get(j).put(n_material);
                }

                System.out.println();

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
            }
        }
    }
}