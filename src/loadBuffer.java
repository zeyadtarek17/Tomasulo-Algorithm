
public class loadBuffer {
    public int address;
    public boolean busy;
    public String tag;
    public int time;
    public instruction instruction;


    public loadBuffer(int address) {
        this.address = address;
        this.busy = true;
    }

    public void deleteBuffer() {
        this.address = 0;
        this.busy = false;
        this.time=0;


    }

    public int getAddress() {
        return address;
    }

    public boolean isBusy() {
        return busy;
    }



    public void setAddress(int address) {
        this.address = address;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }


    public boolean isempty() {
        if (this.busy == false) {
            return true;
        } else {
            return false;
        }
    }

    public loadBuffer() {
        this.address = 0;
        this.busy = false;
        this.tag = null;
    }



    // toString method for debugging
    @Override
    public String toString() {
        return "loadBuffer{" +
                "address=" + address +
                ", busy=" + busy +
                ", tag=" + tag +
                ", time="+ time+
                '}';
    }
}
