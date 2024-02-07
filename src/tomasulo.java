import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
// import java.util.Scanner;
import java.util.Scanner;

public class tomasulo {
    // arraylist of instructions to add instrucyions read from file
    public ArrayList<instruction> program = new ArrayList<instruction>();
    ArrayList<instruction> issued = new ArrayList<instruction>();
    ArrayList<instruction> executed = new ArrayList<instruction>();
    ArrayList<instruction> toBeWritten = new ArrayList<instruction>();

    int cycle = 0;
    int addLatency = 4;
    int subLatency = 5;
    int mulLatency = 6;
    int divLatency = 5;
    int ldLatency = 5;
    int sdLatency = 5;
    int numaddBuffers = 3;
    int nummulBuffers = 2;
    int numLoadBuffers = 5;
    int numStoreBuffers = 5;
    int  cacheSize = 100;
    reservationStation[] addBuffers;
    reservationStation[] mulBuffers;
    loadBuffer[] loadBuffers;
    storeBuffer[] storeBuffers;
    cache cache;
    registerFile registerFile = new registerFile();
    int ADDILatency = 1;
    int BNEZLatency = 1;
    int pc = 0;
    boolean branch = false;
    ArrayList<reservationStation> toBeDeleted = new ArrayList<reservationStation>();

    public void readInstructions() {
        String fileName = "src\\instructions.txt";
        String line = null;

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                String type = tokens[0];
                String i = tokens[1];

                if (type.equals("L.D") || type.equals("S.D") || type.equals("BNEZ")) {
                    int value = Integer.parseInt(tokens[2]);
                    instruction inst = new instruction(type, i, value);
                    program.add(inst);
                } else if (type.equals("ADDI") || type.equals("SUBI") || type.equals("DADDI") || type.equals("DSUBI")) {
                    String j = tokens[2];
                    int value = Integer.parseInt(tokens[3]);
                    instruction inst = new instruction(type, i, j, value);
                    program.add(inst);
                } else {
                    String j = tokens[2];
                    String k = tokens[3];
                    instruction inst = new instruction(type, i, j, k);
                    program.add(inst);
                }
            }
            bufferedReader.close();
        } catch (IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
    }

    public tomasulo() {
        readInstructions();
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the latency of ADD instruction: ");
        int addLatency = sc.nextInt();
        this.addLatency = addLatency;
        System.out.println("Enter the latency of SUB instruction: ");
        int subLatency = sc.nextInt();
        this.subLatency = subLatency;
        System.out.println("Enter the latency of MUL instruction: ");
        int mulLatency = sc.nextInt();
        this.mulLatency = mulLatency;
        System.out.println("Enter the latency of DIV instruction: ");
        int divLatency = sc.nextInt();
        this.divLatency = divLatency;
        System.out.println("Enter the latency of LD instruction: ");
        int ldLatency = sc.nextInt();
        this.ldLatency = ldLatency;
        System.out.println("Enter the latency of SD instruction: ");
        int sdLatency = sc.nextInt();
        this.sdLatency = sdLatency;
        System.out.println("Enter the number of ADD reservation stations: ");
        int numaddBuffers = sc.nextInt();
        this.numaddBuffers = numaddBuffers;
        this.addBuffers = new reservationStation[numaddBuffers];
        for (int i = 0; i < numaddBuffers; i++) {
            addBuffers[i] = new reservationStation();
            addBuffers[i].tag = "A" + i + 1;
        }
        System.out.println("Enter the number of MUL reservation stations: ");
        int nummulBuffers = sc.nextInt();
        this.nummulBuffers = nummulBuffers;
        this.mulBuffers = new reservationStation[nummulBuffers];
        for (int i = 0; i < nummulBuffers; i++) {
            mulBuffers[i] = new reservationStation();
            mulBuffers[i].tag = "M" + i + 1;
        }
        System.out.println("Enter the number of LD reservation stations: ");
        int numLoadBuffers = sc.nextInt();
        this.numLoadBuffers = numLoadBuffers;
        this.loadBuffers = new loadBuffer[numLoadBuffers];
        for (int i = 0; i < numLoadBuffers; i++) {
            loadBuffers[i] = new loadBuffer();
            loadBuffers[i].tag = "L" + i + 1;
        }
        System.out.println("Enter the number of SD reservation stations: ");
        int numStoreBuffers = sc.nextInt();
        this.numStoreBuffers = numStoreBuffers;
        this.storeBuffers = new storeBuffer[numStoreBuffers];
        for (int i = 0; i < numStoreBuffers; i++) {
            storeBuffers[i] = new storeBuffer();
            storeBuffers[i].tag = "S" + i + 1;
        }
        System.out.println("Enter the size of cache: ");
        int cacheSize = sc.nextInt();
        this.cacheSize = cacheSize;
        this.cache = new cache(cacheSize);
        sc.close();
    }

    public void printTomasuloDetails() {

        System.out.println("Instructions:");
        for (instruction inst : program) {
            System.out.println(inst);
        }
        System.out.println("Issued:");
        for (instruction inst : issued) {
            System.out.println(inst);
        }

        System.out.println("Executing instructions:");
        for (instruction inst : executed) {
            System.out.println(inst);
        }
        System.out.println("To be written:");
        for (instruction inst : toBeWritten) {
            System.out.println(inst);
        }
        System.out.println("Add buffers:");
        for (reservationStation buffer : addBuffers) {
            System.out.println(buffer);
        }
        System.out.println("Mul buffers:");
        for (reservationStation buffer : mulBuffers) {
            System.out.println(buffer);
        }
        System.out.println("Load buffers:");
        for (loadBuffer buffer : loadBuffers) {
            System.out.println(buffer);
        }
        System.out.println("Store buffers:");
        for (storeBuffer buffer : storeBuffers) {
            System.out.println(buffer);
        }
        System.out.println("Register file:");
        for (registerFile.register register : registerFile.registers) {
            System.out.println(register);
        }
    }

    public void issue() {
        instruction inst;
        try {
            inst = program.get(pc);
        } catch (Exception e) {
            System.out.println("program is in branch or finished");
            return;
        }
        if (branch) {
            return;
        }
        String type = inst.type;
        if (type.equals("ADD.D") || type.equals("SUB.D") || type.equals("ADDI") || type.equals("SUBI")
                || type.equals("DADD") || type.equals("DSUB")
                || type.equals("DADDI") || type.equals("DSUBI")) {
            for (int i = 0; i < numaddBuffers; i++) {
                if (addBuffers[i].isempty()) {
                    if (!type.equals("ADDI") && !type.equals("SUBI") && !type.equals("DADDI")
                            && !type.equals("DSUBI")) {
                        String firstOperand = inst.j;
                        String secondOperand = inst.k;
                        int firstOperandIndex = Integer.parseInt(firstOperand.substring(1));
                        int secondOperandIndex = Integer.parseInt(secondOperand.substring(1));
                        int resultIndex = Integer.parseInt(inst.i.substring(1));
                        if (registerFile.registers[firstOperandIndex].busy == false) {
                            addBuffers[i].Qi = null;
                            addBuffers[i].Vi = Integer.parseInt(registerFile.registers[firstOperandIndex].Qi);
                        } else {
                            addBuffers[i].Qi = registerFile.registers[firstOperandIndex].Qi;
                            addBuffers[i].Vi = 0;
                        }
                        if (registerFile.registers[secondOperandIndex].busy == false) {
                            addBuffers[i].Qj = null;
                            addBuffers[i].Vj = Integer.parseInt(registerFile.registers[secondOperandIndex].Qi);
                        } else {
                            addBuffers[i].Qj = registerFile.registers[secondOperandIndex].Qi;
                            addBuffers[i].Vj = 0;
                        }
                        addBuffers[i].opcode = type;
                        addBuffers[i].busy = true;
                        addBuffers[i].instruction = inst;
                        addBuffers[i].time = type.equals("ADD.D") ? addLatency : subLatency;
                        registerFile.registers[resultIndex].Qi = addBuffers[i].tag;
                        registerFile.registers[resultIndex].busy = true;
                        inst.issue = cycle;
                        inst.tag = addBuffers[i].tag;
                        pc++;
                        break;
                    } else {
                        // ADDI or SUBI or DADDI or DSUBI
                        String firstOperand = inst.j;
                        int firstOperandIndex = Integer.parseInt(firstOperand.substring(1));
                        int resultIndex = Integer.parseInt(inst.i.substring(1));
                        int value = Integer.parseInt(inst.k);
                        if (registerFile.registers[firstOperandIndex].busy == false) {
                            addBuffers[i].Qi = null;
                            addBuffers[i].Vi = Integer.parseInt(registerFile.registers[firstOperandIndex].Qi);
                        } else {
                            addBuffers[i].Qi = registerFile.registers[firstOperandIndex].Qi;
                            addBuffers[i].Vi = 0;
                        }
                        addBuffers[i].opcode = type;
                        addBuffers[i].Qj = null;
                        addBuffers[i].Vj = value;
                        registerFile.registers[resultIndex].Qi = addBuffers[i].tag;
                        addBuffers[i].busy = true;
                        addBuffers[i].time = ADDILatency;
                        addBuffers[i].instruction = inst;
                        registerFile.registers[resultIndex].busy = true;
                        inst.issue = cycle;
                        inst.tag = addBuffers[i].tag;
                        pc++;
                        break;
                    }
                } else {
                    System.out.println("add " + addBuffers[i].tag + "buffer is busy");
                    continue;
                }
            }
        }
        if (type.equals("MUL.D") || type.equals("DIV.D")) {
            for (int i = 0; i < nummulBuffers; i++) {
                if (mulBuffers[i].isempty()) {
                    String firstOperand = inst.j;
                    String secondOperand = inst.k;
                    int firstOperandIndex = Integer.parseInt(firstOperand.substring(1));
                    int secondOperandIndex = Integer.parseInt(secondOperand.substring(1));
                    int resultIndex = Integer.parseInt(inst.i.substring(1));
                    if (registerFile.registers[firstOperandIndex].busy == false) {
                        mulBuffers[i].Qi = null;
                        mulBuffers[i].Vi = Integer.parseInt(registerFile.registers[firstOperandIndex].Qi);
                    } else {
                        mulBuffers[i].Qi = registerFile.registers[firstOperandIndex].Qi;
                        mulBuffers[i].Vi = 0;
                    }
                    if (registerFile.registers[secondOperandIndex].busy == false) {
                        mulBuffers[i].Qj = null;
                        mulBuffers[i].Vj = Integer.parseInt(registerFile.registers[secondOperandIndex].Qi);
                    } else {
                        mulBuffers[i].Qj = registerFile.registers[secondOperandIndex].Qi;
                        mulBuffers[i].Vj = 0;
                    }
                    mulBuffers[i].opcode = type;
                    mulBuffers[i].busy = true;
                    mulBuffers[i].instruction = inst;
                    registerFile.registers[resultIndex].Qi = mulBuffers[i].tag;
                    registerFile.registers[resultIndex].busy = true;
                    mulBuffers[i].time = type.equals("MUL.D") ? mulLatency : divLatency;
                    inst.issue = cycle;
                    inst.tag = mulBuffers[i].tag;
                    pc++;
                    break;
                } else {
                    System.out.println("mul " + mulBuffers[i].tag + "buffer is busy");
                    continue;
                }
            }
        }
        if (type.equals("L.D")) {
            for (int i = 0; i < numLoadBuffers; i++) {
                if (loadBuffers[i].isempty()) {
                    String firstOperand = inst.i;
                    int address = Integer.parseInt(inst.k);
                    loadBuffers[i].address = address;
                    loadBuffers[i].busy = true;
                    loadBuffers[i].time = ldLatency;
                    loadBuffers[i].instruction = inst;
                    registerFile.registers[Integer.parseInt(firstOperand.substring(1))].Qi = loadBuffers[i].tag;
                    registerFile.registers[Integer.parseInt(firstOperand.substring(1))].busy = true;
                    inst.issue = cycle;
                    inst.tag = loadBuffers[i].tag;
                    if (!cache.isBusy(address)) {
                        cache.makeBusy(address);
                        cache.getCacheCell(address).tag = loadBuffers[i].tag;
                    }
                    pc++;
                    break;
                } else {
                    System.out.println("load " + loadBuffers[i].tag + "buffer is busy");
                    continue;
                }
            }
        }
        if (type.equals("S.D")) {
            for (int i = 0; i < numStoreBuffers; i++) {
                if (storeBuffers[i].isempty()) {
                    String firstOperand = inst.i;
                    int address = Integer.parseInt(inst.k);
                    storeBuffers[i].address = address;
                    storeBuffers[i].busy = true;
                    storeBuffers[i].time = sdLatency;
                    storeBuffers[i].instruction = inst;
                    if (registerFile.registers[Integer.parseInt(firstOperand.substring(1))].busy == true) {
                        storeBuffers[i].Q = registerFile.registers[Integer.parseInt(firstOperand.substring(1))].Qi;

                    } else {
                        storeBuffers[i].V = Integer
                                .parseInt(registerFile.registers[Integer.parseInt(firstOperand.substring(1))].Qi);
                    }
                    inst.issue = cycle;
                    inst.tag = storeBuffers[i].tag;
                    if (!cache.isBusy(address)) {
                        cache.makeBusy(address);
                        cache.getCacheCell(address).tag = storeBuffers[i].tag;
                    }
                    pc++;
                    break;
                } else {
                    System.out.println("store " + storeBuffers[i].tag + "buffer is busy");
                    continue;
                }
            }
        }

        if (type.equals("BNEZ")) {
            for (int i = 0; i < numaddBuffers; i++) {
                if (addBuffers[i].isempty()) {
                    addBuffers[i].opcode = type;
                    addBuffers[i].instruction = inst;
                    branch = true;
                    String firstOperand = inst.i;
                    int firstOperandIndex = Integer.parseInt(firstOperand.substring(1));
                    // branch will read from register file
                    if (registerFile.registers[firstOperandIndex].busy == false) {
                        addBuffers[i].Qi = null;
                        addBuffers[i].Vi = Integer.parseInt(registerFile.registers[firstOperandIndex].Qi);
                    } else {
                        addBuffers[i].Qi = registerFile.registers[firstOperandIndex].Qi;
                        addBuffers[i].Vi = 0;
                    }
                    addBuffers[i].busy = true;
                    addBuffers[i].time = BNEZLatency;
                    inst.issue = cycle;
                    inst.tag = addBuffers[i].tag;
                    pc++;
                    break;
                } else {
                    System.out.println("add " + addBuffers[i].tag + "buffer is busy");
                    continue;
                }
            }
        }
        // todo: BNEZ
        issued.add(inst);
    }

    public void execute() {
        for (reservationStation buffer : addBuffers) {
            if (buffer.time == -1) {
                instruction instruction = buffer.instruction;
                toBeWritten.add(instruction);
                buffer.time--;
                continue;
            }
            if (buffer.busy == true) {
                if (buffer.time == 0) {
                    instruction instruction = buffer.instruction;
                    if (instruction.tag.equals(buffer.tag)) {
                        instruction.executionComplete = cycle;
                        instruction.writeResult = cycle + 1;
                        executed.add(instruction);
                        logicExecute(buffer, instruction);
                        buffer.time--;
                    }

                } else {
                    if (buffer.Qi == null && buffer.Qj == null) {
                        instruction instruction = buffer.instruction;
                        buffer.time--;
                        executed.add(instruction);
                    }
                }
            }
        }
        for (reservationStation buffer : mulBuffers) {
            if (buffer.time == -1) {
                instruction instruction = buffer.instruction;
                toBeWritten.add(instruction);
                buffer.time--;
                continue;
            }
            if (buffer.busy == true) {
                if (buffer.time == 0) {
                    // get the instruction from the executed array7
                    instruction instruction = buffer.instruction;
                    if (instruction.tag.equals(buffer.tag)) {
                        instruction.executionComplete = cycle;
                        instruction.writeResult = cycle + 1;
                        logicExecute(buffer, instruction);
                        buffer.time--;
                    }
                } else {
                    if (buffer.Qi == null && buffer.Qj == null) {
                        instruction instruction = buffer.instruction;
                        buffer.time--;
                        executed.add(instruction);
                    }
                }
            }
        }
        for (loadBuffer buffer : loadBuffers) {
            if (buffer.busy == true) {
                if (buffer.time == -1) {
                    instruction instruction = buffer.instruction;
                    toBeWritten.add(instruction);
                    buffer.time--;
                    continue;
                }
                if (buffer.time == 0) {
                    instruction instruction = buffer.instruction;
                    if (instruction.tag.equals(buffer.tag)) {
                        instruction.executionComplete = cycle;
                        instruction.writeResult = cycle + 1;
                        instruction.value = cache.read(buffer.address);
                        buffer.time--;
                    }
                } else if ((cache.isBusy(buffer.address) && cache.getCacheCell(buffer.address).tag.equals(buffer.tag))
                        || (!cache.isBusy(buffer.address)) && checkOrderLoadInst(buffer.instruction)) {
                    instruction instruction = buffer.instruction;
                    buffer.time--;
                    executed.add(instruction);
                    cache.makeBusy(buffer.address);
                    cache.getCacheCell(buffer.address).tag = buffer.tag;
                } else {
                    System.out.println(
                            "there is another instruction storing in this address so the value is dirty and is waiting for the correct value");
                }
            }
        }

        for (storeBuffer buffer : storeBuffers) {
            if (buffer.busy == true) {
                if (buffer.time == -1) {
                    buffer.time--;
                    continue;
                }
                if (buffer.time == 0) {
                    instruction instruction = buffer.instruction;
                    if (instruction.tag.equals(buffer.tag)) {
                        instruction.executionComplete = cycle;
                        instruction.writeResult = cycle + 1;
                        cache.write(buffer.address, buffer.V);
                        toBeWritten.add(instruction);
                        buffer.time--;

                    }
                } else if (buffer.Q == null
                        && ((cache.isBusy(buffer.address) && cache.getCacheCell(buffer.address).tag.equals(buffer.tag))
                                || (!cache.isBusy(buffer.address)) && checkOrderStoreInst(buffer.instruction))) {
                    instruction instruction = buffer.instruction;
                    buffer.time--;
                    executed.add(instruction);
                    cache.makeBusy(buffer.address);
                    cache.getCacheCell(buffer.address).tag = buffer.tag;
                } else {
                    System.out.println(
                            "there is another instruction loading in this address so the value is dirty and is waiting for the correct value");
                }
            }
        }
    }

    public void writeBack() {
        ArrayList<instruction> writingBack = new ArrayList<instruction>();
        int issuecycle = 1000000000;
        for (instruction instruction : toBeWritten) {
            if (instruction.issue <= issuecycle) {
                issuecycle = instruction.issue;
            }
        }
        for (instruction instruction : toBeWritten) {
            if (instruction.writeResult == cycle && instruction.issue == issuecycle) {
                writingBack.add(instruction);
                toBeWritten.remove(instruction);
                break;
            }
        }
        for (instruction inst : toBeWritten) {
            inst.writeResult = cycle + 1;
        }

        for (reservationStation buffer : addBuffers) {
            if (buffer.time == -2) {
                buffer.deleteStation();
                continue;
            }
            if (buffer.time == -1) {
                instruction inst = buffer.instruction;
                if (registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi == buffer.tag) {
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi = inst.value + "";
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].busy = false;
                }
                // buffer.deleteStation();
            }
            if (buffer.Qi != null) {
                for (instruction inst : writingBack) {
                    if (inst.tag.equals(buffer.Qi)) {
                        buffer.Vi = inst.value;
                        buffer.Qi = null;
                        if (buffer.Qj == null) {
                            buffer.time = getLatency(buffer.opcode) - 1;
                        }
                    }
                }
            }
            if (buffer.Qj != null) {
                for (instruction inst : writingBack) {
                    if (inst.tag.equals(buffer.Qj)) {
                        buffer.Vj = inst.value;
                        buffer.Qj = null;
                        if (buffer.Qi == null)
                            buffer.time = getLatency(buffer.opcode) - 1;
                    }
                }
            }
        }
        for (reservationStation buffer : mulBuffers) {
            if (buffer.time == -2) {
                buffer.deleteStation();
            }
            if (buffer.time == -1) {
                instruction inst = buffer.instruction;
                if (registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi == buffer.tag) {
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi = inst.value + "";
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].busy = false;
                }
                // buffer.deleteStation();
            }
            if (buffer.Qi != null) {
                for (instruction inst : writingBack) {
                    if (inst.tag.equals(buffer.Qi)) {
                        buffer.Vi = inst.value;
                        buffer.Qi = null;
                        if (buffer.Qj == null)
                            buffer.time = getLatency(buffer.opcode) - 1;
                    }
                }
            }
            if (buffer.Qj != null) {
                for (instruction inst : writingBack) {
                    if (inst.tag.equals(buffer.Qj)) {
                        buffer.Vj = inst.value;
                        buffer.Qj = null;
                        if (buffer.Qi == null)
                            buffer.time = getLatency(buffer.opcode) - 1;
                    }
                }
            }
        }
        for (loadBuffer buffer : loadBuffers) {
            if (buffer.time == -2) {
                buffer.deleteBuffer();
            }
            if (buffer.time == -1) {
                instruction inst = buffer.instruction;
                if (registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi == buffer.tag) {
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].Qi = inst.value + "";
                    registerFile.registers[Integer.parseInt(inst.i.substring(1))].busy = false;
                }
                cache.makeNotBusy(buffer.address);
            }
        }
        for (storeBuffer buffer : storeBuffers) {
            if (buffer.time == -2) {
                buffer.deleteBuffer();
            }
            if (buffer.time == -1) {
                cache.makeNotBusy(buffer.address);
            }
            if (buffer.Q != null) {
                for (instruction inst : writingBack) {
                    if (inst.tag.equals(buffer.Q)) {
                        buffer.V = inst.value;
                        buffer.Q = null;
                        buffer.time = sdLatency ;
                    }
                }
            }
        }
        writingBack.clear();
        toBeWritten.removeAll(writingBack);
    }

    private int getLatency(String opcode) {
        int latency = 0;
        if (opcode.equals("ADD.D") || opcode.equals("SUB.D")) {
            latency = opcode.equals("ADD.D") ? addLatency : subLatency;
        } else if (opcode.equals("MUL.D") || opcode.equals("DIV.D")) {
            latency = opcode.equals("MUL.D") ? mulLatency : divLatency;
        } else if (opcode.equals("L.D")) {
            latency = ldLatency;
        } else if (opcode.equals("S.D")) {
            latency = sdLatency;
        } else {
            latency = 1;
        }
        System.out.println("latency is " + latency + " for " + opcode);
        return latency;

    }

    private boolean checkOrderLoadInst(instruction s){
        int issue = s.issue;
        for(storeBuffer buffer: storeBuffers){
            if(buffer.isBusy()&& buffer.instruction.issue < issue && buffer.time > 0){
                return false;
            }
        }
        return true;
    }

    private boolean checkOrderStoreInst(instruction s){
        int issue = s.issue;
        System.out.println("issue is " + issue);
        for(loadBuffer buffer: loadBuffers){
            if(buffer.isBusy()&& buffer.instruction.issue < issue && buffer.time > 0){
                System.out.println(" loadbuffer issue is " + buffer.instruction.issue);
                return false;
            }
        }
        return true;
    }

    private void logicExecute(reservationStation s, instruction inst) {
        if (s.opcode.equals("ADD.D")) {
            inst.value = s.Vi + s.Vj;
        }
        if (s.opcode.equals("SUB.D")) {
            inst.value = s.Vi - s.Vj;
        }
        if (s.opcode.equals("ADDI")) {
            inst.value = s.Vi + s.Vj;
        }
        if (s.opcode.equals("SUBI")) {
            inst.value = s.Vi - s.Vj;
        }
        if (s.opcode.equals("DADD")) {
            inst.value = s.Vi + s.Vj;
        }
        if (s.opcode.equals("DSUB")) {
            inst.value = s.Vi - s.Vj;
        }
        if (s.opcode.equals("DADDI")) {
            inst.value = s.Vi + s.Vj;
        }
        if (s.opcode.equals("DSUBI")) {
            inst.value = s.Vi - s.Vj;
        }
        if (s.opcode.equals("DIV.D")) {
            inst.value = s.Vi / s.Vj;
        }
        if (s.opcode.equals("MUL.D")) {
            inst.value = s.Vi * s.Vj;
        }
        if (s.opcode.equals("BNEZ")) {
            if (s.Vi != 0) {
                branchTaken(0);
                System.out.println("branch taken=============================");
                branch = false;
                // s.deleteStation();
            } else {
                System.out.println("branch not taken=============================");
                branch = false;
                // s.deleteStation();
            }
        }
    }

    private void branchTaken(int address) {
        // create new instructions from the address untill a branch is found and add
        // them to the program
        int i = address;
        while (!program.get(address).type.equals("BNEZ")) {
            System.out.println("adding instruction " + program.get(address).type);
            instruction newInst;
            instruction inst = program.get(address);
            if (inst.type.equals("L.D") || inst.type.equals("S.D")) {
                newInst = new instruction(inst.type, inst.i, inst.value);
            } else if (inst.type.equals("ADDI") || inst.type.equals("SUBI") || inst.type.equals("DADDI")
                    || inst.type.equals("DSUBI")) {
                newInst = new instruction(inst.type, inst.i, inst.j, inst.k);
            } else if (inst.type.equals("ADD.D") || inst.type.equals("SUB.D") || inst.type.equals("DADD")
                    || inst.type.equals("DSUB")) {
                newInst = new instruction(inst.type, inst.i, inst.j, inst.k);
            } else
                newInst = new instruction(inst.type, inst.i, inst.j, inst.k);

            program.add(newInst);
            address++;
            i++;
        }
        instruction branch = new instruction(program.get(address).type, program.get(address).i,
                program.get(address).value);
        program.add(branch);
    }

    private boolean checkEmptyBuffers() {
        boolean empty = true;
        for (reservationStation buffer : addBuffers) {
            if (!buffer.isempty()) {
                empty = false;
                break;
            }
        }
        for (reservationStation buffer : mulBuffers) {
            if (!buffer.isempty()) {
                empty = false;
                break;
            }
        }
        for (loadBuffer buffer : loadBuffers) {
            if (!buffer.isempty()) {
                empty = false;
                break;
            }
        }
        for (storeBuffer buffer : storeBuffers) {
            if (!buffer.isempty()) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    public void run() {
        while (true) {
            cycle++;
            System.out.println("Cycle: " + cycle);
            System.out.println("pc=" + pc);
            issue();
            execute();
            writeBack();
            printTomasuloDetails();
            executed.clear();
            issued.clear();

            // the program will finish when
            try {
                program.get(pc);
            } catch (Exception e) {
                if (checkEmptyBuffers()) {
                    break;
                }
            }

            // if (cycle == 12)
            // break;

        }
        System.out.println("--------------------------------------------------");
        // print the first issued instruction
        int cycles = 0;
        for (instruction inst : program) {
            if (inst.writeResult > cycles) {
                cycles = inst.writeResult;
            }
        }
        System.out.println("the program is finished in " + cycles + " cycles");
        // print each instruction issue, execution complete, and write result times
        for (instruction inst : program) {
            System.out.println(
                    inst.type + " The instruction is issued on cycle " + inst.issue + " and finished executing on "
                            + inst.executionComplete + " then wrote back in cycle " + inst.writeResult);
        }
    }
}
