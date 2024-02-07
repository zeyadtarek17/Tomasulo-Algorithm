public class registerFile {
    public static class register {
        public String name;
        public String Qi;
        public boolean busy;

        public register(int number) {
            name = "F" + number;
            Qi = "2";
            busy = false;
        }

        @Override
        public String toString() {
            return "Register{" +
                    "name='" + name + '\'' +
                    ", Qi='" + Qi + '\'' +
                    '}';
        }
    }

    public register[] registers = new register[32];

    public registerFile() {
        for (int i = 0; i < 32; i++) {
            registers[i] = new register(i);
        }
    }

    public void updateQi(int number, String Qi) {
        registers[number].Qi = Qi;
    }

    public void print() {
        for (int i = 0; i < 32; i++) {
            System.out.println(registers[i]);
        }
    }

    

    
}
