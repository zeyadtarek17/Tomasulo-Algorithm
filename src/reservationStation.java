public class reservationStation {
    public String opcode;
    public int Vi;
    public int Vj;
    public String Qi;
    public String Qj;
    public String tag;
    public boolean busy;
    public int time;
    public instruction instruction;

    public reservationStation(String opcode, int Vi, int Vj, String Qi, String Qj, String tag, boolean busy) {
        this.opcode = opcode;
        this.Vi = Vi;
        this.Vj = Vj;
        this.Qi = Qi;
        this.Qj = Qj;
        this.tag = tag;
        this.busy = busy;
    }

    public reservationStation() {
        this.opcode = null;
        this.Vi = 0;
        this.Vj = 0;
        this.Qi = null;
        this.Qj = null;
        this.tag = null;
        this.busy = false;
    }

    public void deleteStation() {
        this.opcode = null;
        this.Vi = 0;
        this.Vj = 0;
        this.Qi = null;
        this.Qj = null;
        this.busy = false;
        this.time=0;
    }
    @Override
    public String toString() {
        return "Station{" +
                "opcode='" + opcode + '\'' +
                ", Vi=" + Vi +
                ", Vj=" + Vj +
                ", Qi='" + Qi + '\'' +
                ", Qj='" + Qj + '\'' +
                ", tag=" + tag +
                ", busy=" + busy +
                ", time=" + time +
                '}';
    }

    public boolean isempty() {
        if (this.busy == false) {
            return true;
        } else {
            return false;
        }
    }
}
